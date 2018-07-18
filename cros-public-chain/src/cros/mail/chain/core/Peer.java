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

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.misc.ListenerRegister;
import cros.mail.chain.misc.Threading;
import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Peer extends PeerHandler {
	private static final Logger log = LoggerFactory.getLogger(Peer.class);

	protected final ReentrantLock lock = Threading.lock("peer");

	private final NetworkParams params;
	private final AbstractChain blockChain;
	private final Context context;

	static class PeerListenerRegistration extends ListenerRegister<PeerEventListener> {
		boolean callOnDisconnect = true;

		public PeerListenerRegistration(PeerEventListener listener, Executor executor) {
			super(listener, executor);
		}

		public PeerListenerRegistration(PeerEventListener listener, Executor executor, boolean callOnDisconnect) {
			this(listener, executor);
			this.callOnDisconnect = callOnDisconnect;
		}
	}

	private final CopyOnWriteArrayList<PeerListenerRegistration> eventListeners;

	private volatile boolean vDownloadData;

	private final VersionMessage versionMessage;

	private volatile boolean vDownloadTxDependencies;

	private final AtomicInteger blocksAnnounced = new AtomicInteger();

	private final CopyOnWriteArrayList<Wallet> wallets;

	@GuardedBy("lock")
	private long fastCatchupTimeSecs;

	@GuardedBy("lock")
	private boolean downloadBlockBodies = true;

	@GuardedBy("lock")
	private boolean useFilteredBlocks = false;

	private volatile BloomFilter vBloomFilter;

	private LightBlock currentFilteredBlock = null;

	private int filteredBlocksReceived;

	@GuardedBy("lock")
	@Nullable
	private List<Sha256Hash> awaitingFreshFilter;

	private static final int RESEND_BLOOM_FILTER_BLOCK_COUNT = 25000;

	private final HashSet<Sha256Hash> pendingBlockDownloads = new HashSet<Sha256Hash>();

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final HashSet<TransactionDegree> pendingTxDownloads = new HashSet<TransactionDegree>();

	private volatile int vMinProtocolVersion = Pong.MIN_PROTOCOL_VERSION;

	private static class GetDataRequest {
		Sha256Hash hash;
		SettableFuture future;
	}

	private final CopyOnWriteArrayList<GetDataRequest> getDataFutures;
	@GuardedBy("getAddrFutures")
	private final LinkedList<SettableFuture<AddressMessage>> getAddrFutures;
	@Nullable
	@GuardedBy("lock")
	private LinkedList<SettableFuture<UnspentMessage>> getutxoFutures;

	private final ReentrantLock lastPingTimesLock = new ReentrantLock();
	@GuardedBy("lastPingTimesLock")
	private long[] lastPingTimes = null;
	private final CopyOnWriteArrayList<PendingPing> pendingPings;
	private static final int PING_MOVING_AVERAGE_WINDOW = 20;

	private volatile VersionMessage vPeerVersionMessage;
	private boolean isAcked;

	private final SettableFuture<Peer> connectionOpenFuture = SettableFuture.create();
	private final SettableFuture<Peer> versionHandshakeFuture = SettableFuture.create();

	public Peer(NetworkParams params, VersionMessage ver, @Nullable AbstractChain chain, PeerAddress remoteAddress) {
		this(params, ver, remoteAddress, chain);
	}

	public Peer(NetworkParams params, VersionMessage ver, PeerAddress remoteAddress, @Nullable AbstractChain chain) {
		this(params, ver, remoteAddress, chain, true);
	}

	public Peer(NetworkParams params, VersionMessage ver, PeerAddress remoteAddress, @Nullable AbstractChain chain,
			boolean downloadTxDependencies) {
		super(params, remoteAddress);
		this.params = Preconditions.checkNotNull(params);
		this.versionMessage = Preconditions.checkNotNull(ver);
		this.vDownloadTxDependencies = chain != null && downloadTxDependencies;
		this.blockChain = chain;
		this.vDownloadData = chain != null;
		this.getDataFutures = new CopyOnWriteArrayList<GetDataRequest>();
		this.eventListeners = new CopyOnWriteArrayList<PeerListenerRegistration>();
		this.getAddrFutures = new LinkedList<SettableFuture<AddressMessage>>();
		this.fastCatchupTimeSecs = params.getGenesisBlock().getTimeSeconds();
		this.isAcked = false;
		this.pendingPings = new CopyOnWriteArrayList<PendingPing>();
		this.wallets = new CopyOnWriteArrayList<Wallet>();
		this.context = Context.get();
	}

	public Peer(NetworkParams params, AbstractChain blockChain, PeerAddress peerAddress, String thisSoftwareName,
			String thisSoftwareVersion) {
		this(params, new VersionMessage(params, blockChain.getBestChainHeight()), blockChain, peerAddress);
		this.versionMessage.appendToSubVer(thisSoftwareName, thisSoftwareVersion, null);
	}

	public void addEventListener(PeerEventListener listener) {
		addEventListener(listener, Threading.USER_THREAD);
	}

	public void addEventListener(PeerEventListener listener, Executor executor) {
		eventListeners.add(new PeerListenerRegistration(listener, executor));
	}

	void addEventListenerWithoutOnDisconnect(PeerEventListener listener, Executor executor) {
		eventListeners.add(new PeerListenerRegistration(listener, executor, false));
	}

	public boolean removeEventListener(PeerEventListener listener) {
		return ListenerRegister.removeFromList(listener, eventListeners);
	}

	@Override
	public String toString() {
		PeerAddress addr = getAddress();

		return addr == null ? "Peer()" : addr.toString();
	}

	@Override
	protected void timeoutOccurred() {
		super.timeoutOccurred();
		if (!connectionOpenFuture.isDone()) {
			connectionClosed();
		}
	}

	@Override
	public void connectionClosed() {
		for (final PeerListenerRegistration registration : eventListeners) {
			if (registration.callOnDisconnect)
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						registration.listener.onPeerDisconnected(Peer.this, 0);
					}
				});
		}
	}

	@Override
	public void connectionOpened() {

		PeerAddress address = getAddress();
		log.info("Announcing to {} as: {}", address == null ? "Peer" : address.toSocketAddress(),
				versionMessage.subVer);
		sendMessage(versionMessage);
		connectionOpenFuture.set(this);

	}

	public ListenableFuture<Peer> getConnectionOpenFuture() {
		return connectionOpenFuture;
	}

	public ListenableFuture<Peer> getVersionHandshakeFuture() {
		return versionHandshakeFuture;
	}

	@Override
	protected void processMessage(Message m) throws Exception {

		for (ListenerRegister<PeerEventListener> registration : eventListeners) {

			if (registration.executor == Threading.SAME_THREAD) {
				m = registration.listener.onPreMessageReceived(this, m);
				if (m == null)
					break;
			}
		}
		if (m == null)
			return;

		if (currentFilteredBlock != null && !(m instanceof Transaction)) {
			endFilteredBlock(currentFilteredBlock);
			currentFilteredBlock = null;
		}

		if (m instanceof Ping) {
			if (((Ping) m).hasNonce())
				sendMessage(new Pong(((Ping) m).getNonce()));
		} else if (m instanceof Pong) {
			processPong((Pong) m);
		} else if (m instanceof NotFoundMessage) {

			processNotFoundMessage((NotFoundMessage) m);
		} else if (m instanceof InventoryMessage) {
			processInv((InventoryMessage) m);
		} else if (m instanceof Block) {
			processBlock((Block) m);
		} else if (m instanceof LightBlock) {
			startFilteredBlock((LightBlock) m);
		} else if (m instanceof Transaction) {
			processTransaction((Transaction) m);
		} else if (m instanceof GetDataMessage) {
			processGetData((GetDataMessage) m);
		} else if (m instanceof AddressMessage) {

			processAddressMessage((AddressMessage) m);
		} else if (m instanceof HeadersMessage) {
			processHeaders((HeadersMessage) m);
		} else if (m instanceof AlertMessage) {
			processAlert((AlertMessage) m);
		} else if (m instanceof VersionMessage) {
			processVersionMessage((VersionMessage) m);
		} else if (m instanceof Version) {
			if (vPeerVersionMessage == null) {
				throw new ProtocolException("got a version ack before version");
			}
			if (isAcked) {
				throw new ProtocolException("got more than one version ack");
			}
			isAcked = true;
			this.setTimeoutEnabled(false);
			for (final ListenerRegister<PeerEventListener> registration : eventListeners) {
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						registration.listener.onPeerConnected(Peer.this, 1);
					}
				});
			}

			final int version = vMinProtocolVersion;
			if (vPeerVersionMessage.clientVersion < version) {
				log.warn("Connected to a peer speaking protocol version {} but need {}, closing",
						vPeerVersionMessage.clientVersion, version);
				close();
			}
		} else if (m instanceof UnspentMessage) {
			processUTXOMessage((UnspentMessage) m);
		} else if (m instanceof RejectMessage) {
			log.error("{} {}: Received {}", this, getPeerVersionMessage().subVer, m);
		} else {
			log.warn("{}: Received unhandled message: {}", this, m);
		}
	}

	private void processUTXOMessage(UnspentMessage m) {
		SettableFuture<UnspentMessage> future = null;
		lock.lock();
		try {
			if (getutxoFutures != null)
				future = getutxoFutures.pollFirst();
		} finally {
			lock.unlock();
		}
		if (future != null)
			future.set(m);
	}

	private void processAddressMessage(AddressMessage m) {
		SettableFuture<AddressMessage> future;
		synchronized (getAddrFutures) {
			future = getAddrFutures.poll();
			if (future == null)
				return;
		}
		future.set(m);
	}

	private void processVersionMessage(VersionMessage m) throws ProtocolException {
		if (vPeerVersionMessage != null)
			throw new ProtocolException("Got two version messages from peer");
		vPeerVersionMessage = m;

		long peerTime = vPeerVersionMessage.time * 1000;
		log.info("{}: Got version={}, subVer='{}', services=0x{}, time={}, blocks={}", this,
				vPeerVersionMessage.clientVersion, vPeerVersionMessage.subVer, vPeerVersionMessage.localServices,
				String.format(Locale.US, "%tF %tT", peerTime, peerTime), vPeerVersionMessage.bestHeight);

		sendMessage(new Version());

		if (!vPeerVersionMessage.hasBlockChain()
				|| (!params.allowEmptyPeerChain() && vPeerVersionMessage.bestHeight <= 0)) {

			throw new ProtocolException("Peer does not have a copy of the block chain.");
		}
		versionHandshakeFuture.set(this);
	}

	private void startFilteredBlock(LightBlock m) {

		currentFilteredBlock = m;

		filteredBlocksReceived++;
		if (filteredBlocksReceived % RESEND_BLOOM_FILTER_BLOCK_COUNT == RESEND_BLOOM_FILTER_BLOCK_COUNT - 1) {
			sendMessage(vBloomFilter);
		}
	}

	private void processNotFoundMessage(NotFoundMessage m) {

		for (GetDataRequest req : getDataFutures) {
			for (InventoryItem item : m.getItems()) {
				if (item.hash.equals(req.hash)) {
					log.info("{}: Bottomed out dep tree at {}", this, req.hash);
					req.future.cancel(true);
					getDataFutures.remove(req);
					break;
				}
			}
		}
	}

	private void processAlert(AlertMessage m) {
		try {
			if (m.isSignatureValid()) {
				log.info("Received alert from peer {}: {}", this, m.getStatusBar());
			} else {
				log.warn("Received alert with invalid signature from peer {}: {}", this, m.getStatusBar());
			}
		} catch (Throwable t) {

			log.error("Failed to check signature: bug in platform libraries?", t);
		}
	}

	private void processHeaders(HeadersMessage m) throws ProtocolException {

		boolean downloadBlockBodies;
		long fastCatchupTimeSecs;

		lock.lock();
		try {
			if (blockChain == null) {

				log.warn("Received headers when Peer is not configured with a chain.");
				return;
			}
			fastCatchupTimeSecs = this.fastCatchupTimeSecs;
			downloadBlockBodies = this.downloadBlockBodies;
		} finally {
			lock.unlock();
		}

		try {
			checkState(!downloadBlockBodies, toString());
			for (int i = 0; i < m.getBlockHeaders().size(); i++) {
				Block header = m.getBlockHeaders().get(i);

				boolean passedTime = header.getTimeSeconds() >= fastCatchupTimeSecs;
				boolean reachedTop = blockChain.getBestChainHeight() >= vPeerVersionMessage.bestHeight;
				if (!passedTime && !reachedTop) {
					if (!vDownloadData) {

						log.info("Lost download peer status, throwing away downloaded headers.");
						return;
					}
					if (blockChain.add(header)) {

						invokeOnBlocksDownloaded(header, null);
					} else {

						throw new ProtocolException("Got unconnected header from peer: " + header.getHashAsString());
					}
				} else {
					lock.lock();
					try {
						log.info("Passed the fast catchup time, discarding {} headers and requesting full blocks",
								m.getBlockHeaders().size() - i);
						this.downloadBlockBodies = true;

						this.lastGetBlocksBegin = Sha256Hash.ZERO_HASH;
						blockChainDownloadLocked(Sha256Hash.ZERO_HASH);
					} finally {
						lock.unlock();
					}
					return;
				}
			}

			if (m.getBlockHeaders().size() >= HeadersMessage.MAX_HEADERS) {
				lock.lock();
				try {
					blockChainDownloadLocked(Sha256Hash.ZERO_HASH);
				} finally {
					lock.unlock();
				}
			}
		} catch (VeriException e) {
			log.warn("Block header verification failed", e);
		} catch (PartialException e) {

			throw new RuntimeException(e);
		}
	}

	private void processGetData(GetDataMessage getdata) {
		log.info("{}: Received getdata message: {}", getAddress(), getdata.toString());
		ArrayList<Message> items = new ArrayList<Message>();
		for (ListenerRegister<PeerEventListener> registration : eventListeners) {
			if (registration.executor != Threading.SAME_THREAD)
				continue;
			List<Message> listenerItems = registration.listener.getData(this, getdata);
			if (listenerItems == null)
				continue;
			items.addAll(listenerItems);
		}
		if (items.size() == 0) {
			return;
		}
		log.info("{}: Sending {} items gathered from listeners to peer", getAddress(), items.size());
		for (Message item : items) {
			sendMessage(item);
		}
	}

	private void processTransaction(final Transaction tx) throws VeriException {

		tx.verify();
		lock.lock();
		try {
			log.debug("{}: Received tx {}", getAddress(), tx.getHashAsString());

			TransactionDegree confidence = tx.getConfidence();
			confidence.setSource(TransactionDegree.Source.NETWORK);
			pendingTxDownloads.remove(confidence);
			if (maybeHandleRequestedData(tx)) {
				return;
			}
			if (currentFilteredBlock != null) {
				if (!currentFilteredBlock.provideTransaction(tx)) {

					endFilteredBlock(currentFilteredBlock);
					currentFilteredBlock = null;
				}

				return;
			}

			for (final Wallet wallet : wallets) {
				try {
					if (wallet.isPendingTransactionRelevant(tx)) {
						if (vDownloadTxDependencies) {

							Futures.addCallback(downloadDependencies(tx), new FutureCallback<List<Transaction>>() {
								@Override
								public void onSuccess(List<Transaction> dependencies) {
									try {
										log.info("{}: Dependency download complete!", getAddress());
										wallet.receivePending(tx, dependencies);
									} catch (VeriException e) {
										log.error("{}: Wallet failed to process pending transaction {}", getAddress(),
												tx.getHash());
										log.error("Error was: ", e);

									}
								}

								@Override
								public void onFailure(Throwable throwable) {
									log.error("Could not download dependencies of tx {}", tx.getHashAsString());
									log.error("Error was: ", throwable);

								}
							});
						} else {
							wallet.receivePending(tx, null);
						}
					}
				} catch (VeriException e) {
					log.error("Wallet failed to verify tx", e);

				}
			}
		} finally {
			lock.unlock();
		}

		for (final ListenerRegister<PeerEventListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onTransaction(Peer.this, tx);
				}
			});
		}
	}

	public ListenableFuture<List<Transaction>> downloadDependencies(Transaction tx) {
		TransactionDegree.ConfidenceType txConfidence = tx.getConfidence().getConfidenceType();
		Preconditions.checkArgument(txConfidence != TransactionDegree.ConfidenceType.BUILDING);
		log.info("{}: Downloading dependencies of {}", getAddress(), tx.getHashAsString());
		final LinkedList<Transaction> results = new LinkedList<Transaction>();

		final ListenableFuture<Object> future = downloadDependenciesInternal(tx, new Object(), results);
		final SettableFuture<List<Transaction>> resultFuture = SettableFuture.create();
		Futures.addCallback(future, new FutureCallback<Object>() {
			@Override
			public void onSuccess(Object ignored) {
				resultFuture.set(results);
			}

			@Override
			public void onFailure(Throwable throwable) {
				resultFuture.setException(throwable);
			}
		});
		return resultFuture;
	}

	private ListenableFuture<Object> downloadDependenciesInternal(final Transaction tx, final Object marker,
			final List<Transaction> results) {
		final SettableFuture<Object> resultFuture = SettableFuture.create();
		final Sha256Hash rootTxHash = tx.getHash();

		Set<Sha256Hash> needToRequest = new CopyOnWriteArraySet<Sha256Hash>();
		for (TxInput input : tx.getInputs()) {

			needToRequest.add(input.getOutpoint().getHash());
		}
		lock.lock();
		try {

			List<ListenableFuture<Transaction>> futures = Lists.newArrayList();
			GetDataMessage getdata = new GetDataMessage(params);
			if (needToRequest.size() > 1)
				log.info("{}: Requesting {} transactions for dep resolution", getAddress(), needToRequest.size());
			for (Sha256Hash hash : needToRequest) {
				getdata.addTransaction(hash);
				GetDataRequest req = new GetDataRequest();
				req.hash = hash;
				req.future = SettableFuture.create();
				futures.add(req.future);
				getDataFutures.add(req);
			}
			ListenableFuture<List<Transaction>> successful = Futures.successfulAsList(futures);
			Futures.addCallback(successful, new FutureCallback<List<Transaction>>() {
				@Override
				public void onSuccess(List<Transaction> transactions) {

					List<ListenableFuture<Object>> childFutures = Lists.newLinkedList();
					for (Transaction tx : transactions) {
						if (tx == null)
							continue;
						log.info("{}: Downloaded dependency of {}: {}", getAddress(), rootTxHash, tx.getHashAsString());
						results.add(tx);

						childFutures.add(downloadDependenciesInternal(tx, marker, results));
					}
					if (childFutures.size() == 0) {

						resultFuture.set(marker);
					} else {

						Futures.addCallback(Futures.successfulAsList(childFutures), new FutureCallback<List<Object>>() {
							@Override
							public void onSuccess(List<Object> objects) {
								resultFuture.set(marker);
							}

							@Override
							public void onFailure(Throwable throwable) {
								resultFuture.setException(throwable);
							}
						});
					}
				}

				@Override
				public void onFailure(Throwable throwable) {
					resultFuture.setException(throwable);
				}
			});

			sendMessage(getdata);
		} catch (Exception e) {
			log.error("{}: Couldn't send getdata in downloadDependencies({})", this, tx.getHash());
			resultFuture.setException(e);
			return resultFuture;
		} finally {
			lock.unlock();
		}
		return resultFuture;
	}

	private void processBlock(Block m) {
		if (log.isDebugEnabled()) {
			log.debug("{}: Received broadcast block {}", getAddress(), m.getHashAsString());
		}

		if (maybeHandleRequestedData(m))
			return;
		if (blockChain == null) {
			log.warn("Received block but was not configured with an AbstractChain");
			return;
		}

		if (!vDownloadData) {
			log.debug("{}: Received block we did not ask for: {}", getAddress(), m.getHashAsString());
			return;
		}
		pendingBlockDownloads.remove(m.getHash());
		try {

			if (blockChain.add(m)) {

				invokeOnBlocksDownloaded(m, null);
			} else {

				lock.lock();
				try {
					if (downloadBlockBodies) {
						final Block orphanRoot = checkNotNull(blockChain.getOrphanRoot(m.getHash()));
						blockChainDownloadLocked(orphanRoot.getHash());
					} else {
						log.info("Did not start chain download on solved block due to in-flight header download.");
					}
				} finally {
					lock.unlock();
				}
			}
		} catch (VeriException e) {

			log.warn("{}: Block verification failed", getAddress(), e);
		} catch (PartialException e) {

			throw new RuntimeException(e);
		}
	}

	private void endFilteredBlock(LightBlock m) {
		if (log.isDebugEnabled())
			log.debug("{}: Received broadcast filtered block {}", getAddress(), m.getHash().toString());
		if (!vDownloadData) {
			log.debug("{}: Received block we did not ask for: {}", getAddress(), m.getHash().toString());
			return;
		}
		if (blockChain == null) {
			log.warn("Received filtered block but was not configured with an AbstractChain");
			return;
		}

		pendingBlockDownloads.remove(m.getBlockHeader().getHash());
		try {

			lock.lock();
			try {
				if (awaitingFreshFilter != null) {
					log.info("Discarding block {} because we're still waiting for a fresh filter", m.getHash());

					awaitingFreshFilter.add(m.getHash());
					return;
				} else if (checkForFilterExhaustion(m)) {

					log.info("Bloom filter exhausted whilst processing block {}, discarding", m.getHash());
					awaitingFreshFilter = new LinkedList<Sha256Hash>();
					awaitingFreshFilter.add(m.getHash());
					awaitingFreshFilter.addAll(blockChain.drainOrphanBlocks());
					return;
				}
			} finally {
				lock.unlock();
			}

			if (blockChain.add(m)) {

				invokeOnBlocksDownloaded(m.getBlockHeader(), m);
			} else {

				lock.lock();
				try {
					final Block orphanRoot = checkNotNull(blockChain.getOrphanRoot(m.getHash()));
					blockChainDownloadLocked(orphanRoot.getHash());
				} finally {
					lock.unlock();
				}
			}
		} catch (VeriException e) {

			log.warn("{}: LightBlock verification failed", getAddress(), e);
		} catch (PartialException e) {

			throw new RuntimeException(e);
		}
	}

	private boolean checkForFilterExhaustion(LightBlock m) {
		boolean exhausted = false;
		for (Wallet wallet : wallets) {
			exhausted |= wallet.checkForFilterExhaustion(m);
		}
		return exhausted;
	}

	private boolean maybeHandleRequestedData(Message m) {
		boolean found = false;
		Sha256Hash hash = m.getHash();
		for (GetDataRequest req : getDataFutures) {
			if (hash.equals(req.hash)) {
				req.future.set(m);
				getDataFutures.remove(req);
				found = true;

			}
		}
		return found;
	}

	private void invokeOnBlocksDownloaded(final Block block, @Nullable final LightBlock fb) {

		final int blocksLeft = Math.max(0,
				(int) vPeerVersionMessage.bestHeight - checkNotNull(blockChain).getBestChainHeight());
		for (final ListenerRegister<PeerEventListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onBlocksDownloaded(Peer.this, block, fb, blocksLeft);
				}
			});
		}
	}

	private void processInv(InventoryMessage inv) {
		List<InventoryItem> items = inv.getItems();

		List<InventoryItem> transactions = new LinkedList<InventoryItem>();
		List<InventoryItem> blocks = new LinkedList<InventoryItem>();

		for (InventoryItem item : items) {
			switch (item.type) {
			case Transaction:
				transactions.add(item);
				break;
			case Block:
				blocks.add(item);
				break;
			default:
				throw new IllegalStateException("Not implemented: " + item.type);
			}
		}

		final boolean downloadData = this.vDownloadData;

		if (transactions.size() == 0 && blocks.size() == 1) {

			if (downloadData && blockChain != null) {
				if (!blockChain.isOrphan(blocks.get(0).hash)) {
					blocksAnnounced.incrementAndGet();
				}
			} else {
				blocksAnnounced.incrementAndGet();
			}
		}

		GetDataMessage getdata = new GetDataMessage(params);

		Iterator<InventoryItem> it = transactions.iterator();
		while (it.hasNext()) {
			InventoryItem item = it.next();

			TransactionDegree conf = context.getConfidenceTable().seen(item.hash, this.getAddress());
			if (conf.numBroadcastPeers() > 1) {

				it.remove();
			} else if (conf.getSource().equals(TransactionDegree.Source.SELF)) {

				it.remove();
			} else {
				log.debug("{}: getdata on tx {}", getAddress(), item.hash);
				getdata.addItem(item);

				pendingTxDownloads.add(conf);
			}
		}

		boolean pingAfterGetData = false;

		lock.lock();
		try {
			if (blocks.size() > 0 && downloadData && blockChain != null) {

				for (InventoryItem item : blocks) {
					if (blockChain.isOrphan(item.hash) && downloadBlockBodies) {

						final Block orphanRoot = checkNotNull(blockChain.getOrphanRoot(item.hash));
						blockChainDownloadLocked(orphanRoot.getHash());
					} else {

						if (!pendingBlockDownloads.contains(item.hash)) {
							if (vPeerVersionMessage.isBloomFilteringSupported() && useFilteredBlocks) {
								getdata.addFilteredBlock(item.hash);
								pingAfterGetData = true;
							} else {
								getdata.addItem(item);
							}
							pendingBlockDownloads.add(item.hash);
						}
					}
				}

			}
		} finally {
			lock.unlock();
		}

		if (!getdata.getItems().isEmpty()) {

			sendMessage(getdata);
		}

		if (pingAfterGetData)
			sendMessage(new Ping((long) (Math.random() * Long.MAX_VALUE)));
	}

	@SuppressWarnings("unchecked")

	public ListenableFuture<Block> getBlock(Sha256Hash blockHash) {

		log.info("Request to fetch block {}", blockHash);
		GetDataMessage getdata = new GetDataMessage(params);
		getdata.addBlock(blockHash);
		return sendSingleGetData(getdata);
	}

	@SuppressWarnings("unchecked")

	public ListenableFuture<Transaction> getPeerMempoolTransaction(Sha256Hash hash) {

		log.info("Request to fetch peer mempool tx  {}", hash);
		GetDataMessage getdata = new GetDataMessage(params);
		getdata.addTransaction(hash);
		return sendSingleGetData(getdata);
	}

	private ListenableFuture sendSingleGetData(GetDataMessage getdata) {

		Preconditions.checkArgument(getdata.getItems().size() == 1);
		GetDataRequest req = new GetDataRequest();
		req.future = SettableFuture.create();
		req.hash = getdata.getItems().get(0).hash;
		getDataFutures.add(req);
		sendMessage(getdata);
		return req.future;
	}

	public ListenableFuture<AddressMessage> getAddr() {
		SettableFuture<AddressMessage> future = SettableFuture.create();
		synchronized (getAddrFutures) {
			getAddrFutures.add(future);
		}
		sendMessage(new GetAddrMessage(params));
		return future;
	}

	public void setDownloadParameters(long secondsSinceEpoch, boolean useFilteredBlocks) {
		lock.lock();
		try {
			if (secondsSinceEpoch == 0) {
				fastCatchupTimeSecs = params.getGenesisBlock().getTimeSeconds();
				downloadBlockBodies = true;
			} else {
				fastCatchupTimeSecs = secondsSinceEpoch;

				if (blockChain != null && fastCatchupTimeSecs > blockChain.getChainHead().getHeader().getTimeSeconds())
					downloadBlockBodies = false;
			}
			this.useFilteredBlocks = useFilteredBlocks;
		} finally {
			lock.unlock();
		}
	}

	public void addWallet(Wallet wallet) {
		wallets.add(wallet);
	}

	public void removeWallet(Wallet wallet) {
		wallets.remove(wallet);
	}

	@GuardedBy("lock")
	private Sha256Hash lastGetBlocksBegin, lastGetBlocksEnd;

	@GuardedBy("lock")
	private void blockChainDownloadLocked(Sha256Hash toHash) {
		checkState(lock.isHeldByCurrentThread());

		List<Sha256Hash> blockLocator = new ArrayList<Sha256Hash>(51);

		BlockData store = checkNotNull(blockChain).getBlockStore();
		StoredDataBlock chainHead = blockChain.getChainHead();
		Sha256Hash chainHeadHash = chainHead.getHeader().getHash();

		if (Objects.equal(lastGetBlocksBegin, chainHeadHash) && Objects.equal(lastGetBlocksEnd, toHash)) {
			log.info("blockChainDownloadLocked({}): ignoring duplicated request: {}", toHash, chainHeadHash);
			for (Sha256Hash hash : pendingBlockDownloads)
				log.info("Pending block download: {}", hash);
			log.info(Throwables.getStackTraceAsString(new Throwable()));
			return;
		}
		if (log.isDebugEnabled())
			log.debug("{}: blockChainDownloadLocked({}) current head = {}", this, toHash,
					chainHead.getHeader().getHashAsString());
		StoredDataBlock cursor = chainHead;
		for (int i = 100; cursor != null && i > 0; i--) {
			blockLocator.add(cursor.getHeader().getHash());
			try {
				cursor = cursor.getPrev(store);
			} catch (BlockDataException e) {
				log.error("Failed to walk the block chain whilst constructing a locator");
				throw new RuntimeException(e);
			}
		}

		if (cursor != null)
			blockLocator.add(params.getGenesisBlock().getHash());

		lastGetBlocksBegin = chainHeadHash;
		lastGetBlocksEnd = toHash;

		if (downloadBlockBodies) {
			GetBlocksMessage message = new GetBlocksMessage(params, blockLocator, toHash);
			sendMessage(message);
		} else {

			GetHeaderMessage message = new GetHeaderMessage(params, blockLocator, toHash);
			sendMessage(message);
		}
	}

	public void startBlockChainDownload() {
		setDownloadData(true);

		final int blocksLeft = getPeerBlockHeightDifference();
		if (blocksLeft >= 0) {
			for (final ListenerRegister<PeerEventListener> registration : eventListeners) {
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						registration.listener.onChainDownloadStarted(Peer.this, blocksLeft);
					}
				});
			}

			lock.lock();
			try {
				blockChainDownloadLocked(Sha256Hash.ZERO_HASH);
			} finally {
				lock.unlock();
			}
		}
	}

	private class PendingPing {

		public SettableFuture<Long> future;

		public final long nonce;

		public final long startTimeMsec;

		public PendingPing(long nonce) {
			future = SettableFuture.create();
			this.nonce = nonce;
			startTimeMsec = Utils.currentTimeMillis();
		}

		public void complete() {
			if (!future.isDone()) {
				Long elapsed = Utils.currentTimeMillis() - startTimeMsec;
				Peer.this.addPingTimeData(elapsed);
				log.debug("{}: ping time is {} msec", Peer.this.toString(), elapsed);
				future.set(elapsed);
			}
		}
	}

	private void addPingTimeData(long sample) {
		lastPingTimesLock.lock();
		try {
			if (lastPingTimes == null) {
				lastPingTimes = new long[PING_MOVING_AVERAGE_WINDOW];

				Arrays.fill(lastPingTimes, sample);
			} else {

				System.arraycopy(lastPingTimes, 1, lastPingTimes, 0, lastPingTimes.length - 1);

				lastPingTimes[lastPingTimes.length - 1] = sample;
			}
		} finally {
			lastPingTimesLock.unlock();
		}
	}

	public ListenableFuture<Long> ping() throws ProtocolException {
		return ping((long) (Math.random() * Long.MAX_VALUE));
	}

	protected ListenableFuture<Long> ping(long nonce) throws ProtocolException {
		final VersionMessage ver = vPeerVersionMessage;
		if (!ver.isPingPongSupported())
			throw new ProtocolException("Peer version is too low for measurable pings: " + ver);
		PendingPing pendingPing = new PendingPing(nonce);
		pendingPings.add(pendingPing);
		sendMessage(new Ping(pendingPing.nonce));
		return pendingPing.future;
	}

	public long getLastPingTime() {
		lastPingTimesLock.lock();
		try {
			if (lastPingTimes == null)
				return Long.MAX_VALUE;
			return lastPingTimes[lastPingTimes.length - 1];
		} finally {
			lastPingTimesLock.unlock();
		}
	}

	public long getPingTime() {
		lastPingTimesLock.lock();
		try {
			if (lastPingTimes == null)
				return Long.MAX_VALUE;
			long sum = 0;
			for (long i : lastPingTimes)
				sum += i;
			return (long) ((double) sum / lastPingTimes.length);
		} finally {
			lastPingTimesLock.unlock();
		}
	}

	private void processPong(Pong m) {

		for (PendingPing ping : pendingPings) {
			if (m.getNonce() == ping.nonce) {
				pendingPings.remove(ping);

				ping.complete();
				return;
			}
		}
	}

	public int getPeerBlockHeightDifference() {
		checkNotNull(blockChain, "No block chain configured");

		int chainHeight = (int) getBestHeight();

		checkState(params.allowEmptyPeerChain() || chainHeight > 0, "Connected to peer with zero/negative chain height",
				chainHeight);
		return chainHeight - blockChain.getBestChainHeight();
	}

	private boolean isNotFoundMessageSupported() {
		return vPeerVersionMessage.clientVersion >= NotFoundMessage.MIN_PROTOCOL_VERSION;
	}

	public boolean getDownloadData() {
		return vDownloadData;
	}

	public void setDownloadData(boolean downloadData) {
		this.vDownloadData = downloadData;
	}

	public VersionMessage getPeerVersionMessage() {
		return vPeerVersionMessage;
	}

	public VersionMessage getVersionMessage() {
		return versionMessage;
	}

	public long getBestHeight() {
		return vPeerVersionMessage.bestHeight + blocksAnnounced.get();
	}

	public boolean setMinProtocolVersion(int minProtocolVersion) {
		this.vMinProtocolVersion = minProtocolVersion;
		if (getVersionMessage().clientVersion < minProtocolVersion) {
			log.warn("{}: Disconnecting due to new min protocol version {}", this, minProtocolVersion);
			close();
			return true;
		}
		return false;
	}

	public void setBloomFilter(BloomFilter filter) {
		setBloomFilter(filter, true);
	}

	public void setBloomFilter(BloomFilter filter, boolean andQueryMemPool) {
		checkNotNull(filter, "Clearing filters is not currently supported");
		final VersionMessage ver = vPeerVersionMessage;
		if (ver == null || !ver.isBloomFilteringSupported())
			return;
		vBloomFilter = filter;
		log.debug("{}: Sending Bloom filter{}", this, andQueryMemPool ? " and querying mempool" : "");
		sendMessage(filter);
		if (andQueryMemPool)
			sendMessage(new PoolMessage());
		maybeRestartChainDownload();
	}

	private void maybeRestartChainDownload() {
		lock.lock();
		try {
			if (awaitingFreshFilter == null)
				return;
			if (!vDownloadData) {

				log.warn("Lost download peer status whilst awaiting fresh filter.");
				return;
			}

			ping().addListener(new Runnable() {
				@Override
				public void run() {
					lock.lock();
					checkNotNull(awaitingFreshFilter);
					GetDataMessage getdata = new GetDataMessage(params);
					for (Sha256Hash hash : awaitingFreshFilter)
						getdata.addFilteredBlock(hash);
					awaitingFreshFilter = null;
					lock.unlock();

					log.info("Restarting chain download");
					sendMessage(getdata);

					sendMessage(new Ping((long) (Math.random() * Long.MAX_VALUE)));
				}
			}, Threading.SAME_THREAD);
		} finally {
			lock.unlock();
		}
	}

	public BloomFilter getBloomFilter() {
		return vBloomFilter;
	}

	public ListenableFuture<UnspentMessage> getUTXOs(List<TxOutPoint> outPoints) {
		lock.lock();
		try {
			VersionMessage peerVer = getPeerVersionMessage();
			if (peerVer.clientVersion < GetUTXOsMessage.MIN_PROTOCOL_VERSION)
				throw new ProtocolException("Peer does not support getutxos protocol version");
			if ((peerVer.localServices
					& GetUTXOsMessage.SERVICE_FLAGS_REQUIRED) != GetUTXOsMessage.SERVICE_FLAGS_REQUIRED)
				throw new ProtocolException("Peer does not support getutxos protocol flag: find Bitcoin XT nodes.");
			SettableFuture<UnspentMessage> future = SettableFuture.create();

			if (getutxoFutures == null)
				getutxoFutures = new LinkedList<SettableFuture<UnspentMessage>>();
			getutxoFutures.add(future);
			sendMessage(new GetUTXOsMessage(params, outPoints, true));
			return future;
		} finally {
			lock.unlock();
		}

	}

	public boolean getDownloadTxDependencies() {
		return vDownloadTxDependencies;
	}

	public void setDownloadTxDependencies(boolean value) {
		vDownloadTxDependencies = value;
	}
}
