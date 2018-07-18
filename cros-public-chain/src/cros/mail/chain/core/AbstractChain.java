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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.misc.ListenerRegister;
import cros.mail.chain.misc.Threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.*;

public abstract class AbstractChain {
	private static final Logger log = LoggerFactory.getLogger(AbstractChain.class);
	protected final ReentrantLock lock = Threading.lock("blockchain");

	private final BlockData blockData;

	protected StoredDataBlock chainHead;

	private final Object chainHeadLock = new Object();

	protected final NetworkParams params;
	private final CopyOnWriteArrayList<ListenerRegister<CrosChainListener>> listeners;

	class OrphanBlock {
		final Block block;
		final List<Sha256Hash> filteredTxHashes;
		final Map<Sha256Hash, Transaction> filteredTxn;

		OrphanBlock(Block block, @Nullable List<Sha256Hash> filteredTxHashes,
				@Nullable Map<Sha256Hash, Transaction> filteredTxn) {
			final boolean filtered = filteredTxHashes != null && filteredTxn != null;
			Preconditions.checkArgument(
					(block.transactions == null && filtered) || (block.transactions != null && !filtered));
			this.block = block;
			this.filteredTxHashes = filteredTxHashes;
			this.filteredTxn = filteredTxn;
		}
	}

	private final LinkedHashMap<Sha256Hash, OrphanBlock> orphanBlocks = new LinkedHashMap<Sha256Hash, OrphanBlock>();

	public static final double FP_ESTIMATOR_ALPHA = 0.0001;

	public static final double FP_ESTIMATOR_BETA = 0.01;

	private double falsePositiveRate;
	private double falsePositiveTrend;
	private double previousFalsePositiveRate;

	public AbstractChain(NetworkParams params, List<CrosChainListener> listeners, BlockData blockData)
			throws BlockDataException {
		this(Context.getOrCreate(params), listeners, blockData);
	}

	public AbstractChain(Context context, List<CrosChainListener> listeners, BlockData blockData)
			throws BlockDataException {
		this.blockData = blockData;
		chainHead = blockData.getChainHead();
		log.info("chain head is at height {}:\n{}", chainHead.getHeight(), chainHead.getHeader());
		this.params = context.getParams();
		this.listeners = new CopyOnWriteArrayList<ListenerRegister<CrosChainListener>>();
		for (CrosChainListener l : listeners)
			addListener(l, Threading.SAME_THREAD);
	}

	public void addWallet(Wallet wallet) {
		addListener(wallet, Threading.SAME_THREAD);
		int walletHeight = wallet.getLastBlockSeenHeight();
		int chainHeight = getBestChainHeight();
		if (walletHeight != chainHeight) {
			log.warn("Wallet/chain height mismatch: {} vs {}", walletHeight, chainHeight);
			log.warn("Hashes: {} vs {}", wallet.getLastBlockSeenHash(), getChainHead().getHeader().getHash());

			if (walletHeight < chainHeight && walletHeight > 0) {
				try {
					rollbackBlockStore(walletHeight);
					log.info("Rolled back block store to height {}.", walletHeight);
				} catch (BlockDataException x) {
					log.warn(
							"Rollback of block store failed, continuing with mismatched heights. This can happen due to a replay.");
				}
			}
		}
	}

	public void removeWallet(Wallet wallet) {
		removeListener(wallet);
	}

	public void addListener(CrosChainListener listener) {
		addListener(listener, Threading.USER_THREAD);
	}

	public void addListener(CrosChainListener listener, Executor executor) {
		listeners.add(new ListenerRegister<CrosChainListener>(listener, executor));
	}

	public void removeListener(CrosChainListener listener) {
		ListenerRegister.removeFromList(listener, listeners);
	}

	public BlockData getBlockStore() {
		return blockData;
	}

	protected abstract StoredDataBlock addToBlockStore(StoredDataBlock storedPrev, Block block)
			throws BlockDataException, VeriException;

	protected abstract StoredDataBlock addToBlockStore(StoredDataBlock storedPrev, Block header,
			@Nullable TxOutputChanges txOutputChanges) throws BlockDataException, VeriException;

	protected abstract void rollbackBlockStore(int height) throws BlockDataException;

	protected abstract void doSetChainHead(StoredDataBlock chainHead) throws BlockDataException;

	protected abstract void notSettingChainHead() throws BlockDataException;

	protected abstract StoredDataBlock getStoredBlockInCurrentScope(Sha256Hash hash) throws BlockDataException;

	public boolean add(Block block) throws VeriException, PartialException {
		try {
			return add(block, true, null, null);
		} catch (BlockDataException e) {

			throw new RuntimeException(e);
		} catch (VeriException e) {
			try {
				notSettingChainHead();
			} catch (BlockDataException e1) {
				throw new RuntimeException(e1);
			}
			throw new VeriException("Could not verify block:\n" + block.toString(), e);
		}
	}

	public boolean add(LightBlock block) throws VeriException, PartialException {
		try {

			return add(block.getBlockHeader(), true, block.getTransactionHashes(), block.getAssociatedTransactions());
		} catch (BlockDataException e) {

			throw new RuntimeException(e);
		} catch (VeriException e) {
			try {
				notSettingChainHead();
			} catch (BlockDataException e1) {
				throw new RuntimeException(e1);
			}
			throw new VeriException("Could not verify block " + block.getHash().toString() + "\n" + block.toString(),
					e);
		}
	}

	protected abstract boolean shouldVerifyTransactions();

	protected abstract TxOutputChanges connectTransactions(int height, Block block)
			throws VeriException, BlockDataException;

	protected abstract TxOutputChanges connectTransactions(StoredDataBlock newBlock)
			throws VeriException, BlockDataException, PartialException;

	private boolean add(Block block, boolean tryConnecting, @Nullable List<Sha256Hash> filteredTxHashList,
			@Nullable Map<Sha256Hash, Transaction> filteredTxn)
			throws BlockDataException, VeriException, PartialException {

		lock.lock();
		try {

			if (block.equals(getChainHead().getHeader())) {
				return true;
			}
			if (tryConnecting && orphanBlocks.containsKey(block.getHash())) {
				return false;
			}

			if (shouldVerifyTransactions() && block.transactions == null)
				throw new VeriException("Got a block header while running in full-block mode");

			if (shouldVerifyTransactions() && blockData.get(block.getHash()) != null) {
				return true;
			}

			boolean contentsImportant = shouldVerifyTransactions();
			if (block.transactions != null) {
				contentsImportant = contentsImportant || containsRelevantTransactions(block);
			}

			try {
				block.verifyHeader();
				if (contentsImportant)
					block.verifyTransactions();
			} catch (VeriException e) {
				log.error("Failed to verify block: ", e);
				log.error(block.getHashAsString());
				throw e;
			}

			StoredDataBlock storedPrev = getStoredBlockInCurrentScope(block.getPrevBlockHash());

			if (storedPrev == null) {

				checkState(tryConnecting, "bug in tryConnectingOrphans");
				log.warn("Block does not connect: {} prev {}", block.getHashAsString(), block.getPrevBlockHash());
				orphanBlocks.put(block.getHash(), new OrphanBlock(block, filteredTxHashList, filteredTxn));
				return false;
			} else {
				checkState(lock.isHeldByCurrentThread());

				params.checkDifficultyTransitions(storedPrev, block, blockData);
				connectBlock(block, storedPrev, shouldVerifyTransactions(), filteredTxHashList, filteredTxn);
			}

			if (tryConnecting)
				tryConnectingOrphans();

			return true;
		} finally {
			lock.unlock();
		}
	}

	public Set<Sha256Hash> drainOrphanBlocks() {
		lock.lock();
		try {
			Set<Sha256Hash> hashes = new HashSet<Sha256Hash>(orphanBlocks.keySet());
			orphanBlocks.clear();
			return hashes;
		} finally {
			lock.unlock();
		}
	}

	private void connectBlock(final Block block, StoredDataBlock storedPrev, boolean expensiveChecks,
			@Nullable final List<Sha256Hash> filteredTxHashList,
			@Nullable final Map<Sha256Hash, Transaction> filteredTxn)
			throws BlockDataException, VeriException, PartialException {
		checkState(lock.isHeldByCurrentThread());
		boolean filtered = filteredTxHashList != null && filteredTxn != null;

		if (!params.passesCheckpoint(storedPrev.getHeight() + 1, block.getHash()))
			throw new VeriException("Block failed checkpoint lockin at " + (storedPrev.getHeight() + 1));
		if (shouldVerifyTransactions()) {
			checkNotNull(block.transactions);
			for (Transaction tx : block.transactions)
				if (!tx.isFinal(storedPrev.getHeight() + 1, block.getTimeSeconds()))
					throw new VeriException("Block contains non-final transaction");
		}

		StoredDataBlock head = getChainHead();
		if (storedPrev.equals(head)) {
			if (filtered && filteredTxn.size() > 0) {
				log.debug("Block {} connects to top of best chain with {} transaction(s) of which we were sent {}",
						block.getHashAsString(), filteredTxHashList.size(), filteredTxn.size());
				for (Sha256Hash hash : filteredTxHashList)
					log.debug("  matched tx {}", hash);
			}
			if (expensiveChecks && block.getTimeSeconds() <= getMedianTimestampOfRecentBlocks(head, blockData))
				throw new VeriException("Block's timestamp is too early");

			TxOutputChanges txOutChanges = null;
			if (shouldVerifyTransactions())
				txOutChanges = connectTransactions(storedPrev.getHeight() + 1, block);
			StoredDataBlock newStoredBlock = addToBlockStore(storedPrev,
					block.transactions == null ? block : block.cloneAsHeader(), txOutChanges);
			setChainHead(newStoredBlock);
			log.debug("Chain is now {} blocks high, running listeners", newStoredBlock.getHeight());
			informListenersForNewBlock(block, NewBlockType.BEST_CHAIN, filteredTxHashList, filteredTxn, newStoredBlock);
		} else {

			StoredDataBlock newBlock = storedPrev.build(block);
			boolean haveNewBestChain = newBlock.moreWorkThan(head);
			if (haveNewBestChain) {
				log.info("Block is causing a re-organize");
			} else {
				StoredDataBlock splitPoint = findSplit(newBlock, head, blockData);
				if (splitPoint != null && splitPoint.equals(newBlock)) {

					log.warn("Saw duplicated block in main chain at height {}: {}", newBlock.getHeight(),
							newBlock.getHeader().getHash());
					return;
				}
				if (splitPoint == null) {

					throw new VeriException("Block forks the chain but splitPoint is null");
				} else {

					addToBlockStore(storedPrev, block);
					int splitPointHeight = splitPoint.getHeight();
					String splitPointHash = splitPoint.getHeader().getHashAsString();
					log.info("Block forks the chain at height {}/block {}, but it did not cause a reorganize:\n{}",
							splitPointHeight, splitPointHash, newBlock.getHeader().getHashAsString());
				}
			}

			if (block.transactions != null || filtered) {
				informListenersForNewBlock(block, NewBlockType.SIDE_CHAIN, filteredTxHashList, filteredTxn, newBlock);
			}

			if (haveNewBestChain)
				handleNewBestChain(storedPrev, newBlock, block, expensiveChecks);
		}
	}

	private void informListenersForNewBlock(final Block block, final NewBlockType newBlockType,
			@Nullable final List<Sha256Hash> filteredTxHashList,
			@Nullable final Map<Sha256Hash, Transaction> filteredTxn, final StoredDataBlock newStoredBlock)
			throws VeriException {

		boolean first = true;
		Set<Sha256Hash> falsePositives = Sets.newHashSet();
		if (filteredTxHashList != null)
			falsePositives.addAll(filteredTxHashList);
		for (final ListenerRegister<CrosChainListener> registration : listeners) {
			if (registration.executor == Threading.SAME_THREAD) {
				informListenerForNewTransactions(block, newBlockType, filteredTxHashList, filteredTxn, newStoredBlock,
						first, registration.listener, falsePositives);
				if (newBlockType == NewBlockType.BEST_CHAIN)
					registration.listener.notifyNewBestBlock(newStoredBlock);
			} else {

				final boolean notFirst = !first;
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						try {

							Set<Sha256Hash> ignoredFalsePositives = Sets.newHashSet();
							informListenerForNewTransactions(block, newBlockType, filteredTxHashList, filteredTxn,
									newStoredBlock, notFirst, registration.listener, ignoredFalsePositives);
							if (newBlockType == NewBlockType.BEST_CHAIN)
								registration.listener.notifyNewBestBlock(newStoredBlock);
						} catch (VeriException e) {
							log.error("Block chain listener threw exception: ", e);

						}
					}
				});
			}
			first = false;
		}

		trackFalsePositives(falsePositives.size());
	}

	private static void informListenerForNewTransactions(Block block, NewBlockType newBlockType,
			@Nullable List<Sha256Hash> filteredTxHashList, @Nullable Map<Sha256Hash, Transaction> filteredTxn,
			StoredDataBlock newStoredBlock, boolean first, CrosChainListener listener, Set<Sha256Hash> falsePositives)
			throws VeriException {
		if (block.transactions != null) {

			sendTransactionsToListener(newStoredBlock, newBlockType, listener, 0, block.transactions, !first,
					falsePositives);
		} else if (filteredTxHashList != null) {
			checkNotNull(filteredTxn);

			int relativityOffset = 0;
			for (Sha256Hash hash : filteredTxHashList) {
				Transaction tx = filteredTxn.get(hash);
				if (tx != null) {
					sendTransactionsToListener(newStoredBlock, newBlockType, listener, relativityOffset,
							Collections.singletonList(tx), !first, falsePositives);
				} else {
					if (listener.notifyTransactionIsInBlock(hash, newStoredBlock, newBlockType, relativityOffset)) {
						falsePositives.remove(hash);
					}
				}
				relativityOffset++;
			}
		}
	}

	private static long getMedianTimestampOfRecentBlocks(StoredDataBlock storedDataBlock, BlockData store)
			throws BlockDataException {
		long[] timestamps = new long[11];
		int unused = 9;
		timestamps[10] = storedDataBlock.getHeader().getTimeSeconds();
		while (unused >= 0 && (storedDataBlock = storedDataBlock.getPrev(store)) != null)
			timestamps[unused--] = storedDataBlock.getHeader().getTimeSeconds();

		Arrays.sort(timestamps, unused + 1, 11);
		return timestamps[unused + (11 - unused) / 2];
	}

	protected abstract void disconnectTransactions(StoredDataBlock block) throws PartialException, BlockDataException;

	private void handleNewBestChain(StoredDataBlock storedPrev, StoredDataBlock newChainHead, Block block,
			boolean expensiveChecks) throws BlockDataException, VeriException, PartialException {
		checkState(lock.isHeldByCurrentThread());

		StoredDataBlock head = getChainHead();
		final StoredDataBlock splitPoint = findSplit(newChainHead, head, blockData);
		log.info("Re-organize after split at height {}", splitPoint.getHeight());
		log.info("Old chain head: {}", head.getHeader().getHashAsString());
		log.info("New chain head: {}", newChainHead.getHeader().getHashAsString());
		log.info("Split at block: {}", splitPoint.getHeader().getHashAsString());

		final LinkedList<StoredDataBlock> oldBlocks = getPartialChain(head, splitPoint, blockData);
		final LinkedList<StoredDataBlock> newBlocks = getPartialChain(newChainHead, splitPoint, blockData);

		StoredDataBlock storedNewHead = splitPoint;
		if (shouldVerifyTransactions()) {
			for (StoredDataBlock oldBlock : oldBlocks) {
				try {
					disconnectTransactions(oldBlock);
				} catch (PartialException e) {

					throw e;
				}
			}
			StoredDataBlock cursor;

			for (Iterator<StoredDataBlock> it = newBlocks.descendingIterator(); it.hasNext();) {
				cursor = it.next();
				Block cursorBlock = cursor.getHeader();
				if (expensiveChecks && cursorBlock
						.getTimeSeconds() <= getMedianTimestampOfRecentBlocks(cursor.getPrev(blockData), blockData))
					throw new VeriException("Block's timestamp is too early during reorg");
				TxOutputChanges txOutChanges;
				if (cursor != newChainHead || block == null)
					txOutChanges = connectTransactions(cursor);
				else
					txOutChanges = connectTransactions(newChainHead.getHeight(), block);
				storedNewHead = addToBlockStore(storedNewHead, cursorBlock.cloneAsHeader(), txOutChanges);
			}
		} else {

			storedNewHead = addToBlockStore(storedPrev, newChainHead.getHeader());
		}

		for (final ListenerRegister<CrosChainListener> registration : listeners) {
			if (registration.executor == Threading.SAME_THREAD) {

				registration.listener.reorganize(splitPoint, oldBlocks, newBlocks);
			} else {
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							registration.listener.reorganize(splitPoint, oldBlocks, newBlocks);
						} catch (VeriException e) {
							log.error("Block chain listener threw exception during reorg", e);
						}
					}
				});
			}
		}

		setChainHead(storedNewHead);
	}

	private static LinkedList<StoredDataBlock> getPartialChain(StoredDataBlock higher, StoredDataBlock lower,
			BlockData store) throws BlockDataException {
		checkArgument(higher.getHeight() > lower.getHeight(), "higher and lower are reversed");
		LinkedList<StoredDataBlock> results = new LinkedList<StoredDataBlock>();
		StoredDataBlock cursor = higher;
		while (true) {
			results.add(cursor);
			cursor = checkNotNull(cursor.getPrev(store), "Ran off the end of the chain");
			if (cursor.equals(lower))
				break;
		}
		return results;
	}

	private static StoredDataBlock findSplit(StoredDataBlock newChainHead, StoredDataBlock oldChainHead,
			BlockData store) throws BlockDataException {
		StoredDataBlock currentChainCursor = oldChainHead;
		StoredDataBlock newChainCursor = newChainHead;

		while (!currentChainCursor.equals(newChainCursor)) {
			if (currentChainCursor.getHeight() > newChainCursor.getHeight()) {
				currentChainCursor = currentChainCursor.getPrev(store);
				checkNotNull(currentChainCursor, "Attempt to follow an orphan chain");
			} else {
				newChainCursor = newChainCursor.getPrev(store);
				checkNotNull(newChainCursor, "Attempt to follow an orphan chain");
			}
		}
		return currentChainCursor;
	}

	public int getBestChainHeight() {
		return getChainHead().getHeight();
	}

	public enum NewBlockType {
		BEST_CHAIN, SIDE_CHAIN
	}

	private static void sendTransactionsToListener(StoredDataBlock block, NewBlockType blockType,
			CrosChainListener listener, int relativityOffset, List<Transaction> transactions, boolean clone,
			Set<Sha256Hash> falsePositives) throws VeriException {
		for (Transaction tx : transactions) {
			try {
				if (listener.isTransactionRelevant(tx)) {
					falsePositives.remove(tx.getHash());
					if (clone)
						tx = new Transaction(tx.params, tx.bitcoinSerialize());
					listener.receiveFromBlock(tx, block, blockType, relativityOffset++);
				}
			} catch (ScriptException e) {

				log.warn("Failed to parse a script: " + e.toString());
			} catch (ProtocolException e) {

				throw new RuntimeException(e);
			}
		}
	}

	protected void setChainHead(StoredDataBlock chainHead) throws BlockDataException {
		doSetChainHead(chainHead);
		synchronized (chainHeadLock) {
			this.chainHead = chainHead;
		}
	}

	private void tryConnectingOrphans() throws VeriException, BlockDataException, PartialException {
		checkState(lock.isHeldByCurrentThread());

		int blocksConnectedThisRound;
		do {
			blocksConnectedThisRound = 0;
			Iterator<OrphanBlock> iter = orphanBlocks.values().iterator();
			while (iter.hasNext()) {
				OrphanBlock orphanBlock = iter.next();

				StoredDataBlock prev = getStoredBlockInCurrentScope(orphanBlock.block.getPrevBlockHash());
				if (prev == null) {

					log.debug("Orphan block {} is not connectable right now", orphanBlock.block.getHash());
					continue;
				}

				log.info("Connected orphan {}", orphanBlock.block.getHash());
				add(orphanBlock.block, false, orphanBlock.filteredTxHashes, orphanBlock.filteredTxn);
				iter.remove();
				blocksConnectedThisRound++;
			}
			if (blocksConnectedThisRound > 0) {
				log.info("Connected {} orphan blocks.", blocksConnectedThisRound);
			}
		} while (blocksConnectedThisRound > 0);
	}

	private boolean containsRelevantTransactions(Block block) {

		for (Transaction tx : block.transactions) {
			try {
				for (final ListenerRegister<CrosChainListener> registration : listeners) {
					if (registration.executor != Threading.SAME_THREAD)
						continue;
					if (registration.listener.isTransactionRelevant(tx))
						return true;
				}
			} catch (ScriptException e) {

				log.warn("Failed to parse a script: " + e.toString());
			}
		}
		return false;
	}

	public StoredDataBlock getChainHead() {
		synchronized (chainHeadLock) {
			return chainHead;
		}
	}

	@Nullable
	public Block getOrphanRoot(Sha256Hash from) {
		lock.lock();
		try {
			OrphanBlock cursor = orphanBlocks.get(from);
			if (cursor == null)
				return null;
			OrphanBlock tmp;
			while ((tmp = orphanBlocks.get(cursor.block.getPrevBlockHash())) != null) {
				cursor = tmp;
			}
			return cursor.block;
		} finally {
			lock.unlock();
		}
	}

	public boolean isOrphan(Sha256Hash block) {
		lock.lock();
		try {
			return orphanBlocks.containsKey(block);
		} finally {
			lock.unlock();
		}
	}

	public Date estimateBlockTime(int height) {
		synchronized (chainHeadLock) {
			long offset = height - chainHead.getHeight();
			long headTime = chainHead.getHeader().getTimeSeconds();
			long estimated = (headTime * 1000) + (1000L * 60L * 10L * offset);
			return new Date(estimated);
		}
	}

	public ListenableFuture<StoredDataBlock> getHeightFuture(final int height) {
		final SettableFuture<StoredDataBlock> result = SettableFuture.create();
		addListener(new AbstractChainListener() {
			@Override
			public void notifyNewBestBlock(StoredDataBlock block) throws VeriException {
				if (block.getHeight() >= height) {
					removeListener(this);
					result.set(block);
				}
			}
		}, Threading.SAME_THREAD);
		return result;
	}

	public double getFalsePositiveRate() {
		return falsePositiveRate;
	}

	protected void trackFilteredTransactions(int count) {

		double alphaDecay = Math.pow(1 - FP_ESTIMATOR_ALPHA, count);

		falsePositiveRate = alphaDecay * falsePositiveRate;

		double betaDecay = Math.pow(1 - FP_ESTIMATOR_BETA, count);

		falsePositiveTrend = FP_ESTIMATOR_BETA * count * (falsePositiveRate - previousFalsePositiveRate)
				+ betaDecay * falsePositiveTrend;

		falsePositiveRate += alphaDecay * falsePositiveTrend;

		previousFalsePositiveRate = falsePositiveRate;
	}

	void trackFalsePositives(int count) {

		falsePositiveRate += FP_ESTIMATOR_ALPHA * count;
		if (count > 0)
			log.debug("{} false positives, current rate = {} trend = {}", count, falsePositiveRate, falsePositiveTrend);
	}

	public void resetFalsePositiveEstimate() {
		falsePositiveRate = 0;
		falsePositiveTrend = 0;
		previousFalsePositiveRate = 0;
	}
}
