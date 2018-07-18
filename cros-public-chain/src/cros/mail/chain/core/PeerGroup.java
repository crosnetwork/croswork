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

import cros.mail.chain.encrypt.*;
import cros.mail.chain.misc.*;
import cros.mail.chain.misc.Threading;
import cros.mail.chain.netWork.*;
import cros.mail.chain.netWork.discovery.*;
import cros.mail.chain.script.*;
import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.net.*;
import com.google.common.primitives.*;
import com.google.common.util.concurrent.*;
import com.squareup.okhttp.*;
import com.subgraph.orchid.*;
import net.jcip.annotations.*;

import org.slf4j.*;

import javax.annotation.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import static com.google.common.base.Preconditions.*;

public class PeerGroup implements TransactionSender {
	private static final Logger log = LoggerFactory.getLogger(PeerGroup.class);

	public static final int DEFAULT_CONNECTIONS = 12;
	private static final int TOR_TIMEOUT_SECONDS = 60;
	private volatile int vMaxPeersToDiscoverCount = 100;
	private static final long DEFAULT_PEER_DISCOVERY_TIMEOUT_MILLIS = 5000;
	private volatile long vPeerDiscoveryTimeoutMillis = DEFAULT_PEER_DISCOVERY_TIMEOUT_MILLIS;

	protected final ReentrantLock lock = Threading.lock("peergroup");

	private final NetworkParams params;
	@Nullable
	private final AbstractChain chain;

	protected final ListeningScheduledExecutorService executor;

	private volatile boolean vRunning;

	private volatile boolean vUsedUp;

	@GuardedBy("lock")
	private final PriorityQueue<PeerAddress> inactives;
	@GuardedBy("lock")
	private final Map<PeerAddress, LargeBackoff> backoffMap;

	private final CopyOnWriteArrayList<Peer> peers;

	private final CopyOnWriteArrayList<Peer> pendingPeers;
	private final ConnectionManager channels;
	@Nullable
	private final TorClient torClient;

	@GuardedBy("lock")
	private Peer downloadPeer;

	@Nullable
	@GuardedBy("lock")
	private PeerEventListener downloadListener;

	private final CopyOnWriteArrayList<ListenerRegister<PeerEventListener>> peerEventListeners;

	private final CopyOnWriteArraySet<NetworkPeerDiscovery> peerDiscoverers;

	@GuardedBy("lock")
	private VersionMessage versionMessage;

	@GuardedBy("lock")
	private boolean downloadTxDependencies;

	@GuardedBy("lock")
	private int maxConnections;

	private volatile int vMinRequiredProtocolVersion = LightBlock.MIN_PROTOCOL_VERSION;

	public static final long DEFAULT_PING_INTERVAL_MSEC = 2000;
	@GuardedBy("lock")
	private long pingIntervalMsec = DEFAULT_PING_INTERVAL_MSEC;

	@GuardedBy("lock")
	private boolean useLocalhostPeerWhenPossible = true;
	@GuardedBy("lock")
	private boolean ipv6Unreachable = false;

	@GuardedBy("lock")
	private long fastCatchupTimeSecs;
	private final CopyOnWriteArrayList<Wallet> wallets;
	private final CopyOnWriteArrayList<PeerProvider> peerProviders;

	private final AbstractPeerListener peerListener = new AbstractPeerListener() {
		@Override
		public List<Message> getData(Peer peer, GetDataMessage m) {
			return handleGetData(m);
		}

		@Override
		public void onBlocksDownloaded(Peer peer, Block block, @Nullable LightBlock lightBlock, int blocksLeft) {
			if (chain == null)
				return;
			final double rate = chain.getFalsePositiveRate();
			final double target = bloomFilterMerger.getBloomFilterFPRate() * MAX_FP_RATE_INCREASE;
			if (rate > target) {

				if (log.isDebugEnabled())
					log.debug("Force update Bloom filter due to high false positive rate ({} vs {})", rate, target);
				recalculateFastCatchupAndFilter(FilterRecalculateMode.FORCE_SEND_FOR_REFRESH);
			}
		}
	};

	private int minBroadcastConnections = 0;
	private final AbstractWalletListener walletEventListener = new AbstractWalletListener() {
		@Override
		public void onScriptsChanged(Wallet wallet, List<ChainScript> chainScripts, boolean isAddingScripts) {
			recalculateFastCatchupAndFilter(FilterRecalculateMode.SEND_IF_CHANGED);
		}

		@Override
		public void onKeysAdded(List<ECKey> keys) {
			recalculateFastCatchupAndFilter(FilterRecalculateMode.SEND_IF_CHANGED);
		}

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {

			for (TxOutput output : tx.getOutputs()) {
				if (output.getScriptPubKey().isSentToRawPubKey() && output.isMine(wallet)) {
					if (tx.getConfidence().getConfidenceType() == TransactionDegree.ConfidenceType.BUILDING)
						recalculateFastCatchupAndFilter(FilterRecalculateMode.SEND_IF_CHANGED);
					else
						recalculateFastCatchupAndFilter(FilterRecalculateMode.DONT_SEND);
					return;
				}
			}
		}
	};

	private final LargeBackoff.Params peerBackoffParams = new LargeBackoff.Params(1000, 1.5f, 10 * 60 * 1000);

	@GuardedBy("lock")
	private LargeBackoff groupBackoff = new LargeBackoff(new LargeBackoff.Params(1000, 1.5f, 10 * 1000));

	private final Set<TransactionSend> runningBroadcasts;

	private class PeerStartupListener extends AbstractPeerListener {
		@Override
		public void onPeerConnected(Peer peer, int peerCount) {
			handleNewPeer(peer);
		}

		@Override
		public void onPeerDisconnected(Peer peer, int peerCount) {

			handlePeerDeath(peer, null);
		}
	}

	private final PeerEventListener startupListener = new PeerStartupListener();

	public static final double DEFAULT_BLOOM_FILTER_FP_RATE = 0.00001;

	public static final double MAX_FP_RATE_INCREASE = 10.0f;

	private final BlockFilterMerger bloomFilterMerger;

	public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5000;
	private volatile int vConnectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;

	private volatile boolean vBloomFilteringEnabled = true;

	public PeerGroup(NetworkParams params) {
		this(params, null);
	}

	public PeerGroup(Context context) {
		this(context, null);
	}

	public PeerGroup(NetworkParams params, @Nullable AbstractChain chain) {
		this(Context.getOrCreate(params), chain, new CrosClientManager());
	}

	public PeerGroup(Context context, @Nullable AbstractChain chain) {
		this(context, chain, new CrosClientManager());
	}

	public static PeerGroup newWithTor(NetworkParams params, @Nullable AbstractChain chain, TorClient torClient)
			throws TimeoutException {
		return newWithTor(Context.getOrCreate(params), chain, torClient);
	}

	public static PeerGroup newWithTor(Context context, @Nullable AbstractChain chain, TorClient torClient)
			throws TimeoutException {
		return newWithTor(context, chain, torClient, true);
	}

	public static PeerGroup newWithTor(Context context, @Nullable AbstractChain chain, TorClient torClient,
			boolean doDiscovery) throws TimeoutException {
		checkNotNull(torClient);
		DRMHack.maybeDisableExportControls();
		BlockClientManager manager = new BlockClientManager(torClient.getSocketFactory());
		final int CONNECT_TIMEOUT_MSEC = TOR_TIMEOUT_SECONDS * 1000;
		manager.setConnectTimeoutMillis(CONNECT_TIMEOUT_MSEC);
		PeerGroup result = new PeerGroup(context, chain, manager, torClient);
		result.setConnectTimeoutMillis(CONNECT_TIMEOUT_MSEC);

		if (doDiscovery) {
			NetworkParams params = context.getParams();
			HttpServerDiscovery.Details[] httpSeeds = params.getHttpSeeds();
			if (httpSeeds.length > 0) {

				OkHttpClient httpClient = new OkHttpClient();
				httpClient.setSocketFactory(torClient.getSocketFactory());
				List<NetworkPeerDiscovery> discoveries = Lists.newArrayList();
				for (HttpServerDiscovery.Details httpSeed : httpSeeds)
					discoveries.add(new HttpServerDiscovery(params, httpSeed, httpClient));
				result.addPeerDiscovery(new MultipleDiscovery(params, discoveries));
			} else {
				result.addPeerDiscovery(new CrosPeerDiscovery(params, torClient));
			}
		}
		return result;
	}

	public PeerGroup(NetworkParams params, @Nullable AbstractChain chain, ConnectionManager connectionManager) {
		this(Context.getOrCreate(params), chain, connectionManager, null);
	}

	public PeerGroup(Context context, @Nullable AbstractChain chain, ConnectionManager connectionManager) {
		this(context, chain, connectionManager, null);
	}

	private PeerGroup(Context context, @Nullable AbstractChain chain, ConnectionManager connectionManager,
			@Nullable TorClient torClient) {
		checkNotNull(context);
		this.params = context.getParams();
		this.chain = chain;
		fastCatchupTimeSecs = params.getGenesisBlock().getTimeSeconds();
		wallets = new CopyOnWriteArrayList<Wallet>();
		peerProviders = new CopyOnWriteArrayList<PeerProvider>();
		this.torClient = torClient;

		executor = createPrivateExecutor();

		maxConnections = 0;

		int height = chain == null ? 0 : chain.getBestChainHeight();
		versionMessage = new VersionMessage(params, height);

		versionMessage.relayTxesBeforeFilter = true;

		downloadTxDependencies = true;

		inactives = new PriorityQueue<PeerAddress>(1, new Comparator<PeerAddress>() {
			@SuppressWarnings("FieldAccessNotGuarded")
			@Override
			public int compare(PeerAddress a, PeerAddress b) {
				checkState(lock.isHeldByCurrentThread());
				int result = backoffMap.get(a).compareTo(backoffMap.get(b));

				if (result == 0)
					result = Ints.compare(a.getPort(), b.getPort());
				return result;
			}
		});
		backoffMap = new HashMap<PeerAddress, LargeBackoff>();
		peers = new CopyOnWriteArrayList<Peer>();
		pendingPeers = new CopyOnWriteArrayList<Peer>();
		channels = connectionManager;
		peerDiscoverers = new CopyOnWriteArraySet<NetworkPeerDiscovery>();
		peerEventListeners = new CopyOnWriteArrayList<ListenerRegister<PeerEventListener>>();
		runningBroadcasts = Collections.synchronizedSet(new HashSet<TransactionSend>());
		bloomFilterMerger = new BlockFilterMerger(DEFAULT_BLOOM_FILTER_FP_RATE);
	}

	private CountDownLatch executorStartupLatch = new CountDownLatch(1);

	protected ListeningScheduledExecutorService createPrivateExecutor() {
		ListeningScheduledExecutorService result = MoreExecutors
				.listeningDecorator(new ScheduledThreadPoolExecutor(1, new ContextThreadFactory("PeerGroup Thread")));

		result.execute(new Runnable() {
			@Override
			public void run() {
				Uninterruptibles.awaitUninterruptibly(executorStartupLatch);
			}
		});
		return result;
	}

	public void setPeerDiscoveryTimeoutMillis(long peerDiscoveryTimeoutMillis) {
		this.vPeerDiscoveryTimeoutMillis = peerDiscoveryTimeoutMillis;
	}

	public void setMaxConnections(int maxConnections) {
		int adjustment;
		lock.lock();
		try {
			this.maxConnections = maxConnections;
			if (!isRunning())
				return;
		} finally {
			lock.unlock();
		}

		adjustment = maxConnections - channels.getConnectedClientCount();
		if (adjustment > 0)
			triggerConnections();

		if (adjustment < 0)
			channels.closeConnections(-adjustment);
	}

	public void setDownloadTxDependencies(boolean downloadTxDependencies) {
		lock.lock();
		try {
			this.downloadTxDependencies = downloadTxDependencies;
		} finally {
			lock.unlock();
		}
	}

	private Runnable triggerConnectionsJob = new Runnable() {
		private boolean firstRun = true;

		@Override
		public void run() {
			try {
				go();
			} catch (Throwable e) {
				log.error("Exception when trying to build connections", e);
			}
		}

		public void go() {
			if (!vRunning)
				return;

			boolean doDiscovery = false;
			long now = Utils.currentTimeMillis();
			lock.lock();
			try {

				if (!Utils.isAndroidRuntime() && useLocalhostPeerWhenPossible && maybeCheckForLocalhostPeer()
						&& firstRun) {
					log.info("Localhost peer detected, trying to use it instead of P2P discovery");
					maxConnections = 0;
					connectToLocalHost();
					return;
				}

				boolean havePeerWeCanTry = !inactives.isEmpty()
						&& backoffMap.get(inactives.peek()).getRetryTime() <= now;
				doDiscovery = !havePeerWeCanTry;
			} finally {
				firstRun = false;
				lock.unlock();
			}

			boolean discoverySuccess = false;
			if (doDiscovery) {
				try {
					discoverySuccess = discoverPeers() > 0;
				} catch (NetworkDiscoveryException e) {
					log.error("Peer discovery failure", e);
				}
			}

			long retryTime;
			PeerAddress addrToTry;
			lock.lock();
			try {
				if (doDiscovery) {
					if (discoverySuccess) {
						groupBackoff.trackSuccess();
					} else {
						groupBackoff.trackFailure();
					}
				}

				if (inactives.isEmpty()) {
					if (countConnectedAndPendingPeers() < getMaxConnections()) {
						log.info("Peer discovery didn't provide us any more peers, will try again later.");
						executor.schedule(this, groupBackoff.getRetryTime() - now, TimeUnit.MILLISECONDS);
					} else {

					}
					return;
				} else {
					do {
						addrToTry = inactives.poll();
					} while (ipv6Unreachable && addrToTry.getAddr() instanceof Inet6Address);
					retryTime = backoffMap.get(addrToTry).getRetryTime();
				}
				retryTime = Math.max(retryTime, groupBackoff.getRetryTime());
				if (retryTime > now) {
					long delay = retryTime - now;
					log.info("Waiting {} msec before next connect attempt {}", delay,
							addrToTry == null ? "" : "to " + addrToTry);
					inactives.add(addrToTry);
					executor.schedule(this, delay, TimeUnit.MILLISECONDS);
					return;
				}
				connectTo(addrToTry, false, vConnectTimeoutMillis);
			} finally {
				lock.unlock();
			}
			if (countConnectedAndPendingPeers() < getMaxConnections()) {
				executor.execute(this);
			}
		}
	};

	private void triggerConnections() {

		if (!executor.isShutdown())
			executor.execute(triggerConnectionsJob);
	}

	public int getMaxConnections() {
		lock.lock();
		try {
			return maxConnections;
		} finally {
			lock.unlock();
		}
	}

	private List<Message> handleGetData(GetDataMessage m) {

		lock.lock();
		try {
			LinkedList<Message> transactions = new LinkedList<Message>();
			LinkedList<InventoryItem> items = new LinkedList<InventoryItem>(m.getItems());
			Iterator<InventoryItem> it = items.iterator();
			while (it.hasNext()) {
				InventoryItem item = it.next();

				for (Wallet w : wallets) {
					Transaction tx = w.getTransaction(item.hash);
					if (tx == null)
						continue;
					transactions.add(tx);
					it.remove();
					break;
				}
			}
			return transactions;
		} finally {
			lock.unlock();
		}
	}

	public void setVersionMessage(VersionMessage ver) {
		lock.lock();
		try {
			versionMessage = ver;
		} finally {
			lock.unlock();
		}
	}

	public VersionMessage getVersionMessage() {
		lock.lock();
		try {
			return versionMessage;
		} finally {
			lock.unlock();
		}
	}

	public void setUserAgent(String name, String version, @Nullable String comments) {

		int height = chain == null ? 0 : chain.getBestChainHeight();
		VersionMessage ver = new VersionMessage(params, height);
		ver.relayTxesBeforeFilter = false;
		updateVersionMessageRelayTxesBeforeFilter(ver);
		ver.appendToSubVer(name, version, comments);
		setVersionMessage(ver);
	}

	private void updateVersionMessageRelayTxesBeforeFilter(VersionMessage ver) {

		lock.lock();
		try {
			boolean spvMode = chain != null && !chain.shouldVerifyTransactions();
			boolean willSendFilter = spvMode && peerProviders.size() > 0 && vBloomFilteringEnabled;
			ver.relayTxesBeforeFilter = !willSendFilter;
		} finally {
			lock.unlock();
		}
	}

	public void setUserAgent(String name, String version) {
		setUserAgent(name, version, null);
	}

	public void addEventListener(PeerEventListener listener, Executor executor) {
		peerEventListeners.add(new ListenerRegister<PeerEventListener>(checkNotNull(listener), executor));
		for (Peer peer : getConnectedPeers())
			peer.addEventListener(listener, executor);
		for (Peer peer : getPendingPeers())
			peer.addEventListener(listener, executor);
	}

	public void addEventListener(PeerEventListener listener) {
		addEventListener(listener, Threading.USER_THREAD);
	}

	public boolean removeEventListener(PeerEventListener listener) {
		boolean result = ListenerRegister.removeFromList(listener, peerEventListeners);
		for (Peer peer : getConnectedPeers())
			peer.removeEventListener(listener);
		for (Peer peer : getPendingPeers())
			peer.removeEventListener(listener);
		return result;
	}

	public void clearEventListeners() {
		peerEventListeners.clear();
	}

	public List<Peer> getConnectedPeers() {
		lock.lock();
		try {
			return new ArrayList<Peer>(peers);
		} finally {
			lock.unlock();
		}
	}

	public List<Peer> getPendingPeers() {
		lock.lock();
		try {
			return new ArrayList<Peer>(pendingPeers);
		} finally {
			lock.unlock();
		}
	}

	public void addAddress(PeerAddress peerAddress) {
		int newMax;
		lock.lock();
		try {
			addInactive(peerAddress);
			newMax = getMaxConnections() + 1;
		} finally {
			lock.unlock();
		}
		setMaxConnections(newMax);
	}

	private void addInactive(PeerAddress peerAddress) {
		lock.lock();
		try {

			if (backoffMap.containsKey(peerAddress))
				return;
			backoffMap.put(peerAddress, new LargeBackoff(peerBackoffParams));
			inactives.offer(peerAddress);
		} finally {
			lock.unlock();
		}
	}

	public void addAddress(InetAddress address) {
		addAddress(new PeerAddress(address, params.getPort()));
	}

	public void addPeerDiscovery(NetworkPeerDiscovery networkPeerDiscovery) {
		lock.lock();
		try {
			if (getMaxConnections() == 0)
				setMaxConnections(DEFAULT_CONNECTIONS);
			peerDiscoverers.add(networkPeerDiscovery);
		} finally {
			lock.unlock();
		}
	}

	protected int discoverPeers() throws NetworkDiscoveryException {

		checkState(!lock.isHeldByCurrentThread());
		int maxPeersToDiscoverCount = this.vMaxPeersToDiscoverCount;
		long peerDiscoveryTimeoutMillis = this.vPeerDiscoveryTimeoutMillis;
		long start = System.currentTimeMillis();
		final List<PeerAddress> addressList = Lists.newLinkedList();
		for (NetworkPeerDiscovery networkPeerDiscovery : peerDiscoverers) {
			InetSocketAddress[] addresses;
			addresses = networkPeerDiscovery.getPeers(peerDiscoveryTimeoutMillis, TimeUnit.MILLISECONDS);
			for (InetSocketAddress address : addresses)
				addressList.add(new PeerAddress(address));
			if (addressList.size() >= maxPeersToDiscoverCount)
				break;
		}
		if (!addressList.isEmpty()) {
			for (PeerAddress address : addressList) {
				addInactive(address);
			}
			final ImmutableSet<PeerAddress> peersDiscoveredSet = ImmutableSet.copyOf(addressList);
			for (final ListenerRegister<PeerEventListener> registration : peerEventListeners) {
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						registration.listener.onPeersDiscovered(peersDiscoveredSet);
					}
				});
			}
		}
		log.info("Peer discovery took {}ms and returned {} items", System.currentTimeMillis() - start,
				addressList.size());
		return addressList.size();
	}

	@VisibleForTesting
	void waitForJobQueue() {
		Futures.getUnchecked(executor.submit(Runnables.doNothing()));
	}

	private int countConnectedAndPendingPeers() {
		lock.lock();
		try {
			return peers.size() + pendingPeers.size();
		} finally {
			lock.unlock();
		}
	}

	private enum LocalhostCheckState {
		NOT_TRIED, FOUND, FOUND_AND_CONNECTED, NOT_THERE
	}

	private LocalhostCheckState localhostCheckState = LocalhostCheckState.NOT_TRIED;

	private boolean maybeCheckForLocalhostPeer() {
		checkState(lock.isHeldByCurrentThread());
		if (localhostCheckState == LocalhostCheckState.NOT_TRIED) {

			Socket socket = null;
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(InetAddresses.forString("127.0.0.1"), params.getPort()),
						vConnectTimeoutMillis);
				localhostCheckState = LocalhostCheckState.FOUND;
				return true;
			} catch (IOException e) {
				log.info("Localhost peer not detected.");
				localhostCheckState = LocalhostCheckState.NOT_THERE;
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {

					}
				}
			}
		}
		return false;
	}

	public ListenableFuture startAsync() {

		if (chain == null) {

			log.warn("Starting up with no attached block chain. Did you forget to pass one to the constructor?");
		}
		checkState(!vUsedUp, "Cannot start a peer group twice");
		vRunning = true;
		vUsedUp = true;
		executorStartupLatch.countDown();

		return executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Starting ...");
					if (torClient != null) {
						log.info("Starting Tor/Orchid ...");
						torClient.start();
						try {
							torClient.waitUntilReady(TOR_TIMEOUT_SECONDS * 1000);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						log.info("Tor ready");
					}
					channels.startAsync();
					channels.awaitRunning();
					triggerConnections();
					setupPinging();
				} catch (Throwable e) {
					log.error("Exception when starting up", e);
				}
			}
		});
	}

	public void start() {
		Futures.getUnchecked(startAsync());
	}

	@Deprecated
	public void awaitRunning() {
		waitForJobQueue();
	}

	public ListenableFuture stopAsync() {
		checkState(vRunning);
		vRunning = false;
		ListenableFuture future = executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Stopping ...");

					channels.stopAsync();
					channels.awaitTerminated();
					for (NetworkPeerDiscovery networkPeerDiscovery : peerDiscoverers) {
						networkPeerDiscovery.shutdown();
					}
					if (torClient != null) {
						torClient.stop();
					}
					vRunning = false;
					log.info("Stopped.");
				} catch (Throwable e) {
					log.error("Exception when shutting down", e);
				}
			}
		});
		executor.shutdown();
		return future;
	}

	public void stop() {
		try {
			stopAsync();
			log.info("Awaiting PeerGroup shutdown ...");
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public void awaitTerminated() {
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void addWallet(Wallet wallet) {
		lock.lock();
		try {
			checkNotNull(wallet);
			checkState(!wallets.contains(wallet));
			wallets.add(wallet);
			wallet.setTransactionBroadcaster(this);
			wallet.addEventListener(walletEventListener, Threading.SAME_THREAD);
			addPeerFilterProvider(wallet);
			for (Peer peer : peers) {
				peer.addWallet(wallet);
			}
		} finally {
			lock.unlock();
		}
	}

	public ListenableFuture<BloomFilter> addPeerFilterProvider(PeerProvider provider) {
		lock.lock();
		try {
			checkNotNull(provider);
			checkState(!peerProviders.contains(provider));

			peerProviders.add(0, provider);

			ListenableFuture<BloomFilter> future = recalculateFastCatchupAndFilter(
					FilterRecalculateMode.SEND_IF_CHANGED);
			updateVersionMessageRelayTxesBeforeFilter(getVersionMessage());
			return future;
		} finally {
			lock.unlock();
		}
	}

	public void removePeerFilterProvider(PeerProvider provider) {
		lock.lock();
		try {
			checkNotNull(provider);
			checkArgument(peerProviders.remove(provider));
		} finally {
			lock.unlock();
		}
	}

	public void removeWallet(Wallet wallet) {
		wallets.remove(checkNotNull(wallet));
		peerProviders.remove(wallet);
		wallet.removeEventListener(walletEventListener);
		wallet.setTransactionBroadcaster(null);
		for (Peer peer : peers) {
			peer.removeWallet(wallet);
		}
	}

	public enum FilterRecalculateMode {
		SEND_IF_CHANGED, FORCE_SEND_FOR_REFRESH, DONT_SEND,
	}

	private final Map<FilterRecalculateMode, SettableFuture<BloomFilter>> inFlightRecalculations = Maps.newHashMap();

	public ListenableFuture<BloomFilter> recalculateFastCatchupAndFilter(final FilterRecalculateMode mode) {
		final SettableFuture<BloomFilter> future = SettableFuture.create();
		synchronized (inFlightRecalculations) {
			if (inFlightRecalculations.get(mode) != null)
				return inFlightRecalculations.get(mode);
			inFlightRecalculations.put(mode, future);
		}
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
					go();
				} catch (Throwable e) {
					log.error("Exception when trying to recalculate Bloom filter", e);
				}
			}

			public void go() {
				checkState(!lock.isHeldByCurrentThread());

				if ((chain != null && chain.shouldVerifyTransactions()) || !vBloomFilteringEnabled)
					return;

				BlockFilterMerger.Result result = bloomFilterMerger.calculate(ImmutableList.copyOf(peerProviders));
				boolean send;
				switch (mode) {
				case SEND_IF_CHANGED:
					send = result.changed;
					break;
				case DONT_SEND:
					send = false;
					break;
				case FORCE_SEND_FOR_REFRESH:
					send = true;
					break;
				default:
					throw new UnsupportedOperationException();
				}
				if (send) {
					for (Peer peer : peers) {

						peer.setBloomFilter(result.filter, mode != FilterRecalculateMode.FORCE_SEND_FOR_REFRESH);
					}

					if (chain != null)
						chain.resetFalsePositiveEstimate();
				}

				setFastCatchupTimeSecs(result.earliestKeyTimeSecs);
				synchronized (inFlightRecalculations) {
					inFlightRecalculations.put(mode, null);
				}
				future.set(result.filter);
			}
		};
		try {
			executor.execute(command);
		} catch (RejectedExecutionException e) {

		}
		return future;
	}

	public void setBloomFilterFalsePositiveRate(double bloomFilterFPRate) {
		lock.lock();
		try {
			bloomFilterMerger.setBloomFilterFPRate(bloomFilterFPRate);
			recalculateFastCatchupAndFilter(FilterRecalculateMode.SEND_IF_CHANGED);
		} finally {
			lock.unlock();
		}
	}

	public int numConnectedPeers() {
		return peers.size();
	}

	@Nullable
	public Peer connectTo(InetSocketAddress address) {
		lock.lock();
		try {
			PeerAddress peerAddress = new PeerAddress(address);
			backoffMap.put(peerAddress, new LargeBackoff(peerBackoffParams));
			return connectTo(peerAddress, true, vConnectTimeoutMillis);
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public Peer connectToLocalHost() {
		lock.lock();
		try {
			final PeerAddress localhost = PeerAddress.localhost(params);
			backoffMap.put(localhost, new LargeBackoff(peerBackoffParams));
			return connectTo(localhost, true, vConnectTimeoutMillis);
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	@GuardedBy("lock")
	protected Peer connectTo(PeerAddress address, boolean incrementMaxConnections, int connectTimeoutMillis) {
		checkState(lock.isHeldByCurrentThread());
		VersionMessage ver = getVersionMessage().duplicate();
		ver.bestHeight = chain == null ? 0 : chain.getBestChainHeight();
		ver.time = Utils.currentTimeSeconds();

		Peer peer = new Peer(params, ver, address, chain, downloadTxDependencies);
		peer.addEventListener(startupListener, Threading.SAME_THREAD);
		peer.setMinProtocolVersion(vMinRequiredProtocolVersion);
		pendingPeers.add(peer);

		try {
			log.info("Attempting connection to {}     ({} connected, {} pending, {} max)", address, peers.size(),
					pendingPeers.size(), maxConnections);
			ListenableFuture<SocketAddress> future = channels.openConnection(address.toSocketAddress(), peer);
			if (future.isDone())
				Uninterruptibles.getUninterruptibly(future);
		} catch (ExecutionException e) {
			Throwable cause = Throwables.getRootCause(e);
			log.warn("Failed to connect to " + address + ": " + cause.getMessage());
			handlePeerDeath(peer, cause);
			return null;
		}
		peer.setSocketTimeout(connectTimeoutMillis);

		if (incrementMaxConnections) {

			maxConnections++;
		}
		return peer;
	}

	public void setConnectTimeoutMillis(int connectTimeoutMillis) {
		this.vConnectTimeoutMillis = connectTimeoutMillis;
	}

	public void startBlockChainDownload(PeerEventListener listener) {
		lock.lock();
		try {
			if (downloadPeer != null && this.downloadListener != null)
				downloadPeer.removeEventListener(this.downloadListener);
			if (downloadPeer != null && listener != null)
				downloadPeer.addEventListener(listener);
			this.downloadListener = listener;

			if (!peers.isEmpty()) {
				startBlockChainDownloadFromPeer(peers.iterator().next());
			}
		} finally {
			lock.unlock();
		}
	}

	public void downloadBlockChain() {
		ProgressTracker listener = new ProgressTracker();
		startBlockChainDownload(listener);
		try {
			listener.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected void handleNewPeer(final Peer peer) {
		int newSize = -1;
		lock.lock();
		try {
			groupBackoff.trackSuccess();
			backoffMap.get(peer.getAddress()).trackSuccess();

			pendingPeers.remove(peer);
			peers.add(peer);
			newSize = peers.size();
			log.info("{}: New peer      ({} connected, {} pending, {} max)", peer, newSize, pendingPeers.size(),
					maxConnections);

			if (bloomFilterMerger.getLastFilter() != null)
				peer.setBloomFilter(bloomFilterMerger.getLastFilter());
			peer.setDownloadData(false);

			for (Wallet wallet : wallets)
				peer.addWallet(wallet);
			if (downloadPeer == null) {

				setDownloadPeer(selectDownloadPeer(peers));
				boolean shouldDownloadChain = downloadListener != null && chain != null;
				if (shouldDownloadChain) {
					startBlockChainDownloadFromPeer(downloadPeer);
				}
			}

			peer.addEventListener(peerListener, Threading.SAME_THREAD);

			for (ListenerRegister<PeerEventListener> registration : peerEventListeners) {
				peer.addEventListenerWithoutOnDisconnect(registration.listener, registration.executor);
			}
		} finally {
			lock.unlock();
		}

		final int fNewSize = newSize;
		for (final ListenerRegister<PeerEventListener> registration : peerEventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onPeerConnected(peer, fNewSize);
				}
			});
		}
	}

	@Nullable
	private volatile ListenableScheduledFuture<?> vPingTask;

	@SuppressWarnings("NonAtomicOperationOnVolatileField")
	private void setupPinging() {
		if (getPingIntervalMsec() <= 0)
			return;

		vPingTask = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (getPingIntervalMsec() <= 0) {
						ListenableScheduledFuture<?> task = vPingTask;
						if (task != null) {
							task.cancel(false);
							vPingTask = null;
						}
						return;
					}
					for (Peer peer : getConnectedPeers()) {
						if (peer.getPeerVersionMessage().clientVersion < Pong.MIN_PROTOCOL_VERSION)
							continue;
						peer.ping();
					}
				} catch (Throwable e) {
					log.error("Exception in ping loop", e);
				}
			}
		}, getPingIntervalMsec(), getPingIntervalMsec(), TimeUnit.MILLISECONDS);
	}

	private void setDownloadPeer(@Nullable Peer peer) {
		lock.lock();
		try {
			if (downloadPeer == peer)
				return;
			if (downloadPeer != null) {
				log.info("Unsetting download peer: {}", downloadPeer);
				if (downloadListener != null)
					downloadPeer.removeEventListener(downloadListener);
				downloadPeer.setDownloadData(false);
			}
			downloadPeer = peer;
			if (downloadPeer != null) {
				log.info("Setting download peer: {}", downloadPeer);
				if (downloadListener != null)
					peer.addEventListener(downloadListener, Threading.SAME_THREAD);
				downloadPeer.setDownloadData(true);
				if (chain != null)
					downloadPeer.setDownloadParameters(fastCatchupTimeSecs, bloomFilterMerger.getLastFilter() != null);
			}
		} finally {
			lock.unlock();
		}
	}

	@Deprecated
	@Nullable
	public TxDegreeTable getMemoryPool() {
		return Context.get().getConfidenceTable();
	}

	public void setFastCatchupTimeSecs(long secondsSinceEpoch) {
		lock.lock();
		try {
			checkState(chain == null || !chain.shouldVerifyTransactions(),
					"Fast catchup is incompatible with fully verifying");
			fastCatchupTimeSecs = secondsSinceEpoch;
			if (downloadPeer != null) {
				downloadPeer.setDownloadParameters(secondsSinceEpoch, bloomFilterMerger.getLastFilter() != null);
			}
		} finally {
			lock.unlock();
		}
	}

	public long getFastCatchupTimeSecs() {
		lock.lock();
		try {
			return fastCatchupTimeSecs;
		} finally {
			lock.unlock();
		}
	}

	protected void handlePeerDeath(final Peer peer, @Nullable Throwable exception) {

		if (!isRunning())
			return;

		int numPeers;
		int numConnectedPeers = 0;
		lock.lock();
		try {
			pendingPeers.remove(peer);
			peers.remove(peer);

			PeerAddress address = peer.getAddress();

			log.info("{}: Peer died      ({} connected, {} pending, {} max)", address, peers.size(),
					pendingPeers.size(), maxConnections);
			if (peer == downloadPeer) {
				log.info("Download peer died. Picking a new one.");
				setDownloadPeer(null);

				final Peer newDownloadPeer = selectDownloadPeer(peers);
				if (newDownloadPeer != null) {
					setDownloadPeer(newDownloadPeer);
					if (downloadListener != null) {
						startBlockChainDownloadFromPeer(newDownloadPeer);
					}
				}
			}
			numPeers = peers.size() + pendingPeers.size();
			numConnectedPeers = peers.size();

			groupBackoff.trackFailure();

			if (exception instanceof NoRouteToHostException) {
				if (address.getAddr() instanceof Inet6Address && !ipv6Unreachable) {
					ipv6Unreachable = true;
					log.warn("IPv6 peer connect failed due to routing failure, ignoring IPv6 addresses from now on");
				}
			} else {
				backoffMap.get(address).trackFailure();

				inactives.offer(address);
			}

			if (numPeers < getMaxConnections()) {
				triggerConnections();
			}
		} finally {
			lock.unlock();
		}

		peer.removeEventListener(peerListener);
		for (Wallet wallet : wallets) {
			peer.removeWallet(wallet);
		}

		final int fNumConnectedPeers = numConnectedPeers;
		for (final ListenerRegister<PeerEventListener> registration : peerEventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onPeerDisconnected(peer, fNumConnectedPeers);
				}
			});
			peer.removeEventListener(registration.listener);
		}
	}

	@GuardedBy("lock")
	private int stallPeriodSeconds = 10;
	@GuardedBy("lock")
	private int stallMinSpeedBytesSec = Block.HEADER_SIZE * 20;

	public void setStallThreshold(int periodSecs, int bytesPerSecond) {
		lock.lock();
		try {
			stallPeriodSeconds = periodSecs;
			stallMinSpeedBytesSec = bytesPerSecond;
		} finally {
			lock.unlock();
		}
	}

	private class ChainDownloadSpeedCalculator extends AbstractPeerListener implements Runnable {
		private int blocksInLastSecond, txnsInLastSecond, origTxnsInLastSecond;
		private long bytesInLastSecond;

		private int maxStalls = 3;

		private int warmupSeconds = -1;

		private long[] samples;
		private int cursor;

		private boolean syncDone;

		@Override
		public synchronized void onBlocksDownloaded(Peer peer, Block block, @Nullable LightBlock lightBlock,
				int blocksLeft) {
			blocksInLastSecond++;
			bytesInLastSecond += 80;
			List<Transaction> blockTransactions = block.getTransactions();

			int txCount = (blockTransactions != null ? countAndMeasureSize(blockTransactions) : 0)
					+ (lightBlock != null ? countAndMeasureSize(lightBlock.getAssociatedTransactions().values()) : 0);
			txnsInLastSecond = txnsInLastSecond + txCount;
			if (lightBlock != null)
				origTxnsInLastSecond += lightBlock.getTransactionCount();
		}

		private int countAndMeasureSize(Collection<Transaction> transactions) {
			for (Transaction transaction : transactions)
				bytesInLastSecond += transaction.getMessageSize();
			return transactions.size();
		}

		@Override
		public void run() {
			try {
				calculate();
			} catch (Throwable e) {
				log.error("Error in speed calculator", e);
			}
		}

		private void calculate() {
			int minSpeedBytesPerSec;
			int period;

			lock.lock();
			try {
				minSpeedBytesPerSec = stallMinSpeedBytesSec;
				period = stallPeriodSeconds;
			} finally {
				lock.unlock();
			}

			synchronized (this) {
				if (samples == null || samples.length != period) {
					samples = new long[period];

					Arrays.fill(samples, minSpeedBytesPerSec * 2);
					warmupSeconds = 15;
				}

				boolean behindPeers = chain != null && chain.getBestChainHeight() < getMostCommonChainHeight();
				if (!behindPeers)
					syncDone = true;
				if (!syncDone) {
					if (warmupSeconds < 0) {

						samples[cursor++] = bytesInLastSecond;
						if (cursor == samples.length)
							cursor = 0;
						long average = 0;
						for (long sample : samples)
							average += sample;
						average /= samples.length;

						log.info(String.format(Locale.US,
								"%d blocks/sec, %d tx/sec, %d pre-filtered tx/sec, avg/last %.2f/%.2f kilobytes per sec (stall threshold <%.2f KB/sec for %d seconds)",
								blocksInLastSecond, txnsInLastSecond, origTxnsInLastSecond, average / 1024.0,
								bytesInLastSecond / 1024.0, minSpeedBytesPerSec / 1024.0, samples.length));

						if (average < minSpeedBytesPerSec && maxStalls > 0) {
							maxStalls--;
							if (maxStalls == 0) {

								log.warn(
										"This network seems to be slower than the requested stall threshold - won't do stall disconnects any more.");
							} else {
								Peer peer = getDownloadPeer();
								log.warn(String.format(Locale.US,
										"Chain download stalled: received %.2f KB/sec for %d seconds, require average of %.2f KB/sec, disconnecting %s",
										average / 1024.0, samples.length, minSpeedBytesPerSec / 1024.0, peer));
								peer.close();

								samples = null;
								warmupSeconds = period;
							}
						}
					} else {
						warmupSeconds--;
						if (bytesInLastSecond > 0)
							log.info(String.format(Locale.US,
									"%d blocks/sec, %d tx/sec, %d pre-filtered tx/sec, last %.2f kilobytes per sec",
									blocksInLastSecond, txnsInLastSecond, origTxnsInLastSecond,
									bytesInLastSecond / 1024.0));
					}
				}
				blocksInLastSecond = 0;
				txnsInLastSecond = 0;
				origTxnsInLastSecond = 0;
				bytesInLastSecond = 0;
			}
		}
	}

	@Nullable
	private ChainDownloadSpeedCalculator chainDownloadSpeedCalculator;

	private void startBlockChainDownloadFromPeer(Peer peer) {
		lock.lock();
		try {
			setDownloadPeer(peer);

			if (chainDownloadSpeedCalculator == null) {

				chainDownloadSpeedCalculator = new ChainDownloadSpeedCalculator();
				executor.scheduleAtFixedRate(chainDownloadSpeedCalculator, 1, 1, TimeUnit.SECONDS);
			}
			peer.addEventListener(chainDownloadSpeedCalculator, Threading.SAME_THREAD);

			peer.startBlockChainDownload();
		} finally {
			lock.unlock();
		}
	}

	public ListenableFuture<List<Peer>> waitForPeers(final int numPeers) {
		return waitForPeersOfVersion(numPeers, 0);
	}

	public ListenableFuture<List<Peer>> waitForPeersOfVersion(final int numPeers, final long protocolVersion) {
		List<Peer> foundPeers = findPeersOfAtLeastVersion(protocolVersion);
		if (foundPeers.size() >= numPeers) {
			return Futures.immediateFuture(foundPeers);
		}
		final SettableFuture<List<Peer>> future = SettableFuture.create();
		addEventListener(new AbstractPeerListener() {
			@Override
			public void onPeerConnected(Peer peer, int peerCount) {
				final List<Peer> peers = findPeersOfAtLeastVersion(protocolVersion);
				if (peers.size() >= numPeers) {
					future.set(peers);
					removeEventListener(this);
				}
			}
		});
		return future;
	}

	public List<Peer> findPeersOfAtLeastVersion(long protocolVersion) {
		lock.lock();
		try {
			ArrayList<Peer> results = new ArrayList<Peer>(peers.size());
			for (Peer peer : peers)
				if (peer.getPeerVersionMessage().clientVersion >= protocolVersion)
					results.add(peer);
			return results;
		} finally {
			lock.unlock();
		}
	}

	public ListenableFuture<List<Peer>> waitForPeersWithServiceMask(final int numPeers, final int mask) {
		lock.lock();
		try {
			List<Peer> foundPeers = findPeersWithServiceMask(mask);
			if (foundPeers.size() >= numPeers)
				return Futures.immediateFuture(foundPeers);
			final SettableFuture<List<Peer>> future = SettableFuture.create();
			addEventListener(new AbstractPeerListener() {
				@Override
				public void onPeerConnected(Peer peer, int peerCount) {
					final List<Peer> peers = findPeersWithServiceMask(mask);
					if (peers.size() >= numPeers) {
						future.set(peers);
						removeEventListener(this);
					}
				}
			});
			return future;
		} finally {
			lock.unlock();
		}
	}

	public List<Peer> findPeersWithServiceMask(int mask) {
		lock.lock();
		try {
			ArrayList<Peer> results = new ArrayList<Peer>(peers.size());
			for (Peer peer : peers)
				if ((peer.getPeerVersionMessage().localServices & mask) == mask)
					results.add(peer);
			return results;
		} finally {
			lock.unlock();
		}
	}

	public int getMinBroadcastConnections() {
		lock.lock();
		try {
			if (minBroadcastConnections == 0) {
				int max = getMaxConnections();
				if (max <= 1)
					return max;
				else
					return (int) Math.round(getMaxConnections() * 0.8);
			}
			return minBroadcastConnections;
		} finally {
			lock.unlock();
		}
	}

	public void setMinBroadcastConnections(int value) {
		lock.lock();
		try {
			minBroadcastConnections = value;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public TransactionSend broadcastTransaction(final Transaction tx) {
		return broadcastTransaction(tx, Math.max(1, getMinBroadcastConnections()));
	}

	public TransactionSend broadcastTransaction(final Transaction tx, final int minConnections) {

		if (tx.getConfidence().getSource().equals(TransactionDegree.Source.UNKNOWN)) {
			log.info("Transaction source unknown, setting to SELF: {}", tx.getHashAsString());
			tx.getConfidence().setSource(TransactionDegree.Source.SELF);
		}
		final TransactionSend broadcast = new TransactionSend(this, tx);
		broadcast.setMinConnections(minConnections);

		Futures.addCallback(broadcast.future(), new FutureCallback<Transaction>() {
			@Override
			public void onSuccess(Transaction transaction) {
				runningBroadcasts.remove(broadcast);

				for (Wallet wallet : wallets) {

					try {
						wallet.receivePending(transaction, null);
					} catch (VeriException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public void onFailure(Throwable throwable) {

				runningBroadcasts.remove(broadcast);
			}
		});

		runningBroadcasts.add(broadcast);
		broadcast.broadcast();
		return broadcast;
	}

	public long getPingIntervalMsec() {
		lock.lock();
		try {
			return pingIntervalMsec;
		} finally {
			lock.unlock();
		}
	}

	public void setPingIntervalMsec(long pingIntervalMsec) {
		lock.lock();
		try {
			this.pingIntervalMsec = pingIntervalMsec;
			ListenableScheduledFuture<?> task = vPingTask;
			if (task != null)
				task.cancel(false);
			setupPinging();
		} finally {
			lock.unlock();
		}
	}

	public void setMinRequiredProtocolVersion(int minRequiredProtocolVersion) {
		this.vMinRequiredProtocolVersion = minRequiredProtocolVersion;
	}

	public int getMinRequiredProtocolVersion() {
		return vMinRequiredProtocolVersion;
	}

	public int getMostCommonChainHeight() {
		lock.lock();
		try {
			return getMostCommonChainHeight(this.peers);
		} finally {
			lock.unlock();
		}
	}

	public static int getMostCommonChainHeight(final List<Peer> peers) {
		if (peers.isEmpty())
			return 0;
		List<Integer> heights = new ArrayList<Integer>(peers.size());
		for (Peer peer : peers)
			heights.add((int) peer.getBestHeight());
		return Utils.maxOfMostFreq(heights);
	}

	@Nullable
	protected Peer selectDownloadPeer(List<Peer> peers) {

		if (peers.isEmpty())
			return null;

		int mostCommonChainHeight = getMostCommonChainHeight(peers);
		List<Peer> candidates = new ArrayList<Peer>();
		for (Peer peer : peers) {
			if (peer.getBestHeight() == mostCommonChainHeight)
				candidates.add(peer);
		}

		int highestVersion = 0, preferredVersion = 0;

		final int PREFERRED_VERSION = LightBlock.MIN_PROTOCOL_VERSION;
		for (Peer peer : candidates) {
			highestVersion = Math.max(peer.getPeerVersionMessage().clientVersion, highestVersion);
			preferredVersion = Math.min(highestVersion, PREFERRED_VERSION);
		}
		ArrayList<Peer> candidates2 = new ArrayList<Peer>(candidates.size());
		for (Peer peer : candidates) {
			if (peer.getPeerVersionMessage().clientVersion >= preferredVersion) {
				candidates2.add(peer);
			}
		}
		int index = (int) (Math.random() * candidates2.size());
		return candidates2.get(index);
	}

	public Peer getDownloadPeer() {
		lock.lock();
		try {
			return downloadPeer;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public TorClient getTorClient() {
		return torClient;
	}

	public int getMaxPeersToDiscoverCount() {
		return vMaxPeersToDiscoverCount;
	}

	public void setMaxPeersToDiscoverCount(int maxPeersToDiscoverCount) {
		this.vMaxPeersToDiscoverCount = maxPeersToDiscoverCount;
	}

	public boolean getUseLocalhostPeerWhenPossible() {
		lock.lock();
		try {
			return useLocalhostPeerWhenPossible;
		} finally {
			lock.unlock();
		}
	}

	public void setUseLocalhostPeerWhenPossible(boolean useLocalhostPeerWhenPossible) {
		lock.lock();
		try {
			this.useLocalhostPeerWhenPossible = useLocalhostPeerWhenPossible;
		} finally {
			lock.unlock();
		}
	}

	public boolean isRunning() {
		return vRunning;
	}

	public void setBloomFilteringEnabled(boolean bloomFilteringEnabled) {
		this.vBloomFilteringEnabled = bloomFilteringEnabled;
	}

	public boolean isBloomFilteringEnabled() {
		return vBloomFilteringEnabled;
	}
}
