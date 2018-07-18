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
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;

import cros.mail.chain.misc.ListenerRegister;
import cros.mail.chain.core.PeerAddress;
import cros.mail.chain.wallet.Protos.Transaction;

import javax.annotation.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.*;

public class TransactionDegree implements Serializable {
	private static final long serialVersionUID = 4577920141400556444L;

	private CopyOnWriteArrayList<PeerAddress> broadcastBy;

	private final Sha256Hash hash;

	private transient CopyOnWriteArrayList<ListenerRegister<Listener>> listeners;

	private int depth;

	public enum ConfidenceType {

		BUILDING(1),

		PENDING(2),

		DEAD(4),

		UNKNOWN(0);

		private int value;

		ConfidenceType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private ConfidenceType confidenceType = ConfidenceType.UNKNOWN;
	private int appearedAtChainHeight = -1;

	private Transaction overridingTransaction;

	public enum Source {

		UNKNOWN,

		NETWORK,

		SELF
	}

	private Source source = Source.UNKNOWN;

	public TransactionDegree(Sha256Hash hash) {

		broadcastBy = new CopyOnWriteArrayList<PeerAddress>();
		listeners = new CopyOnWriteArrayList<ListenerRegister<Listener>>();
		this.hash = hash;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		listeners = new CopyOnWriteArrayList<ListenerRegister<Listener>>();
	}

	public interface Listener {

		enum ChangeReason {

			TYPE,

			DEPTH,

			SEEN_PEERS,
		}

		void onConfidenceChanged(TransactionDegree confidence, ChangeReason reason);
	}

	private static final Set<TransactionDegree> pinnedConfidenceObjects = Collections
			.synchronizedSet(new HashSet<TransactionDegree>());

	public void addEventListener(Listener listener, Executor executor) {
		checkNotNull(listener);
		listeners.addIfAbsent(new ListenerRegister<Listener>(listener, executor));
		pinnedConfidenceObjects.add(this);
	}

	public void addEventListener(Listener listener) {
		addEventListener(listener, Threading.USER_THREAD);
	}

	public boolean removeEventListener(Listener listener) {
		checkNotNull(listener);
		boolean removed = ListenerRegister.removeFromList(listener, listeners);
		if (listeners.isEmpty())
			pinnedConfidenceObjects.remove(this);
		return removed;
	}

	public synchronized int getAppearedAtChainHeight() {
		if (getConfidenceType() != ConfidenceType.BUILDING)
			throw new IllegalStateException("Confidence type is " + getConfidenceType() + ", not BUILDING");
		return appearedAtChainHeight;
	}

	public synchronized void setAppearedAtChainHeight(int appearedAtChainHeight) {
		if (appearedAtChainHeight < 0)
			throw new IllegalArgumentException("appearedAtChainHeight out of range");
		this.appearedAtChainHeight = appearedAtChainHeight;
		this.depth = 1;
		setConfidenceType(ConfidenceType.BUILDING);
	}

	public synchronized ConfidenceType getConfidenceType() {
		return confidenceType;
	}

	public synchronized void setConfidenceType(ConfidenceType confidenceType) {
		if (confidenceType == this.confidenceType)
			return;
		this.confidenceType = confidenceType;
		if (confidenceType != ConfidenceType.DEAD) {
			overridingTransaction = null;
		}
		if (confidenceType == ConfidenceType.PENDING) {
			depth = 0;
			appearedAtChainHeight = -1;
		}
	}

	public boolean markBroadcastBy(PeerAddress address) {
		if (!broadcastBy.addIfAbsent(address))
			return false;
		synchronized (this) {
			if (getConfidenceType() == ConfidenceType.UNKNOWN) {
				this.confidenceType = ConfidenceType.PENDING;
			}
		}
		return true;
	}

	public int numBroadcastPeers() {
		return broadcastBy.size();
	}

	public Set<PeerAddress> getBroadcastBy() {
		ListIterator<PeerAddress> iterator = broadcastBy.listIterator();
		return Sets.newHashSet(iterator);
	}

	public boolean wasBroadcastBy(PeerAddress address) {
		return broadcastBy.contains(address);
	}

	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		int peers = numBroadcastPeers();
		if (peers > 0) {
			builder.append("Seen by ").append(peers).append(peers > 1 ? " peers. " : " peer. ");
		}
		switch (getConfidenceType()) {
		case UNKNOWN:
			builder.append("Unknown confidence level.");
			break;
		case DEAD:
			builder.append("Dead: overridden by double spend and will not confirm.");
			break;
		case PENDING:
			builder.append("Pending/unconfirmed.");
			break;
		case BUILDING:
			builder.append(String.format(Locale.US, "Appeared in best chain at height %d, depth %d.",
					getAppearedAtChainHeight(), getDepthInBlocks()));
			break;
		}
		return builder.toString();
	}

	public synchronized int incrementDepthInBlocks() {
		return ++this.depth;
	}

	public synchronized int getDepthInBlocks() {
		return depth;
	}

	public synchronized void setDepthInBlocks(int depth) {
		this.depth = depth;
	}

	public void clearBroadcastBy() {
		checkState(getConfidenceType() != ConfidenceType.PENDING);
		broadcastBy.clear();
	}

	public synchronized Transaction getOverridingTransaction() {
		if (getConfidenceType() != ConfidenceType.DEAD)
			throw new IllegalStateException(
					"Confidence type is " + getConfidenceType() + ", not OVERRIDDEN_BY_DOUBLE_SPEND");
		return overridingTransaction;
	}

	public synchronized void setOverridingTransaction(@Nullable Transaction overridingTransaction) {
		this.overridingTransaction = overridingTransaction;
		setConfidenceType(ConfidenceType.DEAD);
	}

	public TransactionDegree duplicate() {
		TransactionDegree c = new TransactionDegree(hash);
		c.broadcastBy.addAll(broadcastBy);
		synchronized (this) {
			c.confidenceType = confidenceType;
			c.overridingTransaction = overridingTransaction;
			c.appearedAtChainHeight = appearedAtChainHeight;
		}
		return c;
	}

	public void queueListeners(final Listener.ChangeReason reason) {
		for (final ListenerRegister<Listener> registration : listeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onConfidenceChanged(TransactionDegree.this, reason);
				}
			});
		}
	}

	public synchronized Source getSource() {
		return source;
	}

	public synchronized void setSource(Source source) {
		this.source = source;
	}

	public synchronized ListenableFuture<TransactionDegree> getDepthFuture(final int depth, Executor executor) {
		final SettableFuture<TransactionDegree> result = SettableFuture.create();
		if (getDepthInBlocks() >= depth) {
			result.set(this);
		}
		addEventListener(new Listener() {
			@Override
			public void onConfidenceChanged(TransactionDegree confidence, ChangeReason reason) {
				if (getDepthInBlocks() >= depth) {
					removeEventListener(this);
					result.set(confidence);
				}
			}
		}, executor);
		return result;
	}

	public synchronized ListenableFuture<TransactionDegree> getDepthFuture(final int depth) {
		return getDepthFuture(depth, Threading.USER_THREAD);
	}

	public Sha256Hash getTransactionHash() {
		return hash;
	}
}
