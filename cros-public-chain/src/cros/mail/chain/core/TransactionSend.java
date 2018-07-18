/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package cros.mail.chain.core;

import cros.mail.chain.misc.*;
import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.util.concurrent.*;

import org.slf4j.*;

import javax.annotation.*;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkState;

public class TransactionSend {
	private static final Logger log = LoggerFactory.getLogger(TransactionSend.class);

	private final SettableFuture<Transaction> future = SettableFuture.create();
	private final PeerGroup peerGroup;
	private final Transaction tx;
	private int minConnections;
	private int numWaitingFor;

	@VisibleForTesting
	public static Random random = new Random();

	private Map<Peer, RejectMessage> rejects = Collections.synchronizedMap(new HashMap<Peer, RejectMessage>());

	TransactionSend(PeerGroup peerGroup, Transaction tx) {
		this.peerGroup = peerGroup;
		this.tx = tx;
		this.minConnections = Math.max(1, peerGroup.getMinBroadcastConnections());
	}

	private TransactionSend(Transaction tx) {
		this.peerGroup = null;
		this.tx = tx;
	}

	@VisibleForTesting
	public static TransactionSend createMockBroadcast(Transaction tx, final SettableFuture<Transaction> future) {
		return new TransactionSend(tx) {
			@Override
			public ListenableFuture<Transaction> broadcast() {
				return future;
			}

			@Override
			public ListenableFuture<Transaction> future() {
				return future;
			}
		};
	}

	public ListenableFuture<Transaction> future() {
		return future;
	}

	public void setMinConnections(int minConnections) {
		this.minConnections = minConnections;
	}

	private PeerEventListener rejectionListener = new AbstractPeerListener() {
		@Override
		public Message onPreMessageReceived(Peer peer, Message m) {
			if (m instanceof RejectMessage) {
				RejectMessage rejectMessage = (RejectMessage) m;
				if (tx.getHash().equals(rejectMessage.getRejectedObjectHash())) {
					rejects.put(peer, rejectMessage);
					int size = rejects.size();
					long threshold = Math.round(numWaitingFor / 2.0);
					if (size > threshold) {
						log.warn("Threshold for considering broadcast rejected has been reached ({}/{})", size,
								threshold);
						future.setException(new RejectedException(tx, rejectMessage));
						peerGroup.removeEventListener(this);
					}
				}
			}
			return m;
		}
	};

	public ListenableFuture<Transaction> broadcast() {
		peerGroup.addEventListener(rejectionListener, Threading.SAME_THREAD);
		log.info("Waiting for {} peers required for broadcast, we have {} ...", minConnections,
				peerGroup.getConnectedPeers().size());
		peerGroup.waitForPeers(minConnections).addListener(new EnoughAvailablePeers(), Threading.SAME_THREAD);
		return future;
	}

	private class EnoughAvailablePeers implements Runnable {
		@Override
		public void run() {

			List<Peer> peers = peerGroup.getConnectedPeers();

			if (minConnections > 1)
				tx.getConfidence().addEventListener(new ConfidenceChange());

			int numConnected = peers.size();
			int numToBroadcastTo = (int) Math.max(1, Math.round(Math.ceil(peers.size() / 2.0)));
			numWaitingFor = (int) Math.ceil((peers.size() - numToBroadcastTo) / 2.0);
			Collections.shuffle(peers, random);
			peers = peers.subList(0, numToBroadcastTo);
			log.info("broadcastTransaction: We have {} peers, adding {} to the memory pool", numConnected,
					tx.getHashAsString());
			log.info("Sending to {} peers, will wait for {}, sending to: {}", numToBroadcastTo, numWaitingFor,
					Joiner.on(",").join(peers));
			for (Peer peer : peers) {
				try {
					peer.sendMessage(tx);

				} catch (Exception e) {
					log.error("Caught exception sending to {}", peer, e);
				}
			}

			if (minConnections == 1) {
				peerGroup.removeEventListener(rejectionListener);
				future.set(tx);
			}
		}
	}

	private int numSeemPeers;
	private boolean mined;

	private class ConfidenceChange implements TransactionDegree.Listener {
		@Override
		public void onConfidenceChanged(TransactionDegree conf, ChangeReason reason) {

			int numSeenPeers = conf.numBroadcastPeers() + rejects.size();
			boolean mined = tx.getAppearsInHashes() != null;
			log.info("broadcastTransaction: {}:  TX {} seen by {} peers{}", reason, tx.getHashAsString(), numSeenPeers,
					mined ? " and mined" : "");

			invokeAndRecord(numSeenPeers, mined);

			if (numSeenPeers >= numWaitingFor || mined) {

				log.info("broadcastTransaction: {} complete", tx.getHash());
				peerGroup.removeEventListener(rejectionListener);
				conf.removeEventListener(this);
				future.set(tx);
			}
		}
	}

	private void invokeAndRecord(int numSeenPeers, boolean mined) {
		synchronized (this) {
			this.numSeemPeers = numSeenPeers;
			this.mined = mined;
		}
		invokeProgressCallback(numSeenPeers, mined);
	}

	private void invokeProgressCallback(int numSeenPeers, boolean mined) {
		final ProgressCallback callback;
		Executor executor;
		synchronized (this) {
			callback = this.callback;
			executor = this.progressCallbackExecutor;
		}
		if (callback != null) {
			final double progress = Math.min(1.0, mined ? 1.0 : numSeenPeers / (double) numWaitingFor);
			checkState(progress >= 0.0 && progress <= 1.0, progress);
			try {
				if (executor == null)
					callback.onBroadcastProgress(progress);
				else
					executor.execute(new Runnable() {
						@Override
						public void run() {
							callback.onBroadcastProgress(progress);
						}
					});
			} catch (Throwable e) {
				log.error("Exception during progress callback", e);
			}
		}
	}

	public interface ProgressCallback {

		void onBroadcastProgress(double progress);
	}

	@Nullable
	private ProgressCallback callback;
	@Nullable
	private Executor progressCallbackExecutor;

	public void setProgressCallback(ProgressCallback callback) {
		setProgressCallback(callback, Threading.USER_THREAD);
	}

	public void setProgressCallback(ProgressCallback callback, @Nullable Executor executor) {
		boolean shouldInvoke;
		int num;
		boolean mined;
		synchronized (this) {
			this.callback = callback;
			this.progressCallbackExecutor = executor;
			num = this.numSeemPeers;
			mined = this.mined;
			shouldInvoke = numWaitingFor > 0;
		}
		if (shouldInvoke)
			invokeProgressCallback(num, mined);
	}
}
