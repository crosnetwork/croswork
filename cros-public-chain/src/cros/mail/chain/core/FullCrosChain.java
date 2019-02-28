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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.blockdata.FullPrunedBlockData;
import cros.mail.chain.misc.*;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScript.VerifyFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkState;
/**
 * 
 * @author CROS
 *
 */
public class FullCrosChain extends AbstractChain {
	private static final Logger log = LoggerFactory.getLogger(FullCrosChain.class);

	protected final FullPrunedBlockData blockStore;

	private boolean runScripts = true;

	public FullCrosChain(Context context, Wallet wallet, FullPrunedBlockData blockStore) throws BlockDataException {
		this(context, new ArrayList<CrosChainListener>(), blockStore);
		addWallet(wallet);
	}

	public FullCrosChain(NetworkParams params, Wallet wallet, FullPrunedBlockData blockStore)
			throws BlockDataException {
		this(Context.getOrCreate(params), wallet, blockStore);
	}

	public FullCrosChain(Context context, FullPrunedBlockData blockStore) throws BlockDataException {
		this(context, new ArrayList<CrosChainListener>(), blockStore);
	}

	public FullCrosChain(NetworkParams params, FullPrunedBlockData blockStore) throws BlockDataException {
		this(Context.getOrCreate(params), blockStore);
	}

	public FullCrosChain(Context context, List<CrosChainListener> listeners, FullPrunedBlockData blockStore)
			throws BlockDataException {
		super(context, listeners, blockStore);
		this.blockStore = blockStore;

		this.chainHead = blockStore.getVerifiedChainHead();
	}

	public FullCrosChain(NetworkParams params, List<CrosChainListener> listeners, FullPrunedBlockData blockStore)
			throws BlockDataException {
		this(Context.getOrCreate(params), listeners, blockStore);
	}

	@Override
	protected StoredDataBlock addToBlockStore(StoredDataBlock storedPrev, Block header, TxOutputChanges txOutChanges)
			throws BlockDataException, VeriException {
		StoredDataBlock newBlock = storedPrev.build(header);
		blockStore.put(newBlock, new StoredInvalidDataBlock(newBlock.getHeader().getHash(), txOutChanges));
		return newBlock;
	}

	@Override
	protected StoredDataBlock addToBlockStore(StoredDataBlock storedPrev, Block block)
			throws BlockDataException, VeriException {
		StoredDataBlock newBlock = storedPrev.build(block);
		blockStore.put(newBlock, new StoredInvalidDataBlock(newBlock.getHeader().getHash(), block.transactions));
		return newBlock;
	}

	@Override
	protected void rollbackBlockStore(int height) throws BlockDataException {
		throw new BlockDataException("Unsupported");
	}

	@Override
	protected boolean shouldVerifyTransactions() {
		return true;
	}

	public void setRunScripts(boolean value) {
		this.runScripts = value;
	}

	ExecutorService scriptVerificationExecutor = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors(), new ContextThreadFactory("ChainScript verification"));

	private static class Verifier implements Callable<VeriException> {
		final Transaction tx;
		final List<ChainScript> prevOutScripts;
		final Set<VerifyFlag> verifyFlags;

		public Verifier(final Transaction tx, final List<ChainScript> prevOutScripts,
				final Set<VerifyFlag> verifyFlags) {
			this.tx = tx;
			this.prevOutScripts = prevOutScripts;
			this.verifyFlags = verifyFlags;
		}

		@Nullable
		@Override
		public VeriException call() throws Exception {
			try {
				ListIterator<ChainScript> prevOutIt = prevOutScripts.listIterator();
				for (int index = 0; index < tx.getInputs().size(); index++) {
					tx.getInputs().get(index).getScriptSig().correctlySpends(tx, index, prevOutIt.next(), verifyFlags);
				}
			} catch (VeriException e) {
				return e;
			}
			return null;
		}
	}

	private ChainScript getScript(byte[] scriptBytes) {
		try {
			return new ChainScript(scriptBytes);
		} catch (Exception e) {
			return new ChainScript(new byte[0]);
		}
	}

	private String getScriptAddress(@Nullable ChainScript chainScript) {
		String address = "";
		try {
			if (chainScript != null) {
				address = chainScript.getToAddress(params, true).toString();
			}
		} catch (Exception e) {
		}
		return address;
	}

	@Override
	protected TxOutputChanges connectTransactions(int height, Block block) throws VeriException, BlockDataException {
		checkState(lock.isHeldByCurrentThread());
		if (block.transactions == null)
			throw new RuntimeException("connectTransactions called with Block that didn't have transactions!");
		if (!params.passesCheckpoint(height, block.getHash()))
			throw new VeriException("Block failed checkpoint lockin at " + height);

		blockStore.beginDatabaseBatchWrite();

		LinkedList<Unspent> txOutsSpent = new LinkedList<Unspent>();
		LinkedList<Unspent> txOutsCreated = new LinkedList<Unspent>();
		long sigOps = 0;
		final Set<VerifyFlag> verifyFlags = EnumSet.noneOf(VerifyFlag.class);
		if (block.getTimeSeconds() >= NetworkParams.BIP16_ENFORCE_TIME)
			verifyFlags.add(VerifyFlag.P2SH);

		if (scriptVerificationExecutor.isShutdown())
			scriptVerificationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		List<Future<VeriException>> listScriptVerificationResults = new ArrayList<Future<VeriException>>(
				block.transactions.size());
		try {
			if (!params.isCheckpoint(height)) {

				for (Transaction tx : block.transactions) {
					Sha256Hash hash = tx.getHash();

					if (blockStore.hasUnspentOutputs(hash, tx.getOutputs().size()))
						throw new VeriException("Block failed BIP30 test!");
					if (verifyFlags.contains(VerifyFlag.P2SH))
						sigOps += tx.getSigOpCount();
				}
			}
			Coin totalFees = Coin.ZERO;
			Coin coinbaseValue = null;
			for (final Transaction tx : block.transactions) {
				boolean isCoinBase = tx.isCoinBase();
				Coin valueIn = Coin.ZERO;
				Coin valueOut = Coin.ZERO;
				final List<ChainScript> prevOutScripts = new LinkedList<ChainScript>();
				if (!isCoinBase) {

					for (int index = 0; index < tx.getInputs().size(); index++) {
						TxInput in = tx.getInputs().get(index);
						Unspent prevOut = blockStore.getTransactionOutput(in.getOutpoint().getHash(),
								in.getOutpoint().getIndex());
						if (prevOut == null)
							throw new VeriException("Attempted to spend a non-existent or already spent output!");

						if (prevOut.isCoinbase()) {
							if (height - prevOut.getHeight() < params.getSpendableCoinbaseDepth()) {
								throw new VeriException(
										"Tried to spend coinbase at depth " + (height - prevOut.getHeight()));
							}
						}

						valueIn = valueIn.add(prevOut.getValue());
						if (verifyFlags.contains(VerifyFlag.P2SH)) {
							if (prevOut.getScript().isPayToScriptHash())
								sigOps += ChainScript.getP2SHSigOpCount(in.getScriptBytes());
							if (sigOps > Block.MAX_BLOCK_SIGOPS)
								throw new VeriException("Too many P2SH SigOps in block");
						}

						prevOutScripts.add(prevOut.getScript());
						blockStore.removeUnspentTransactionOutput(prevOut);
						txOutsSpent.add(prevOut);
					}
				}
				Sha256Hash hash = tx.getHash();
				for (TxOutput out : tx.getOutputs()) {
					valueOut = valueOut.add(out.getValue());

					ChainScript chainScript = getScript(out.getScriptBytes());
					Unspent newOut = new Unspent(hash, out.getIndex(), out.getValue(), height, isCoinBase, chainScript,
							getScriptAddress(chainScript));
					blockStore.addUnspentTransactionOutput(newOut);
					txOutsCreated.add(newOut);
				}

				if (valueOut.signum() < 0 || valueOut.compareTo(params.getMaxMoney()) > 0)
					throw new VeriException("Transaction output value out of range");
				if (isCoinBase) {
					coinbaseValue = valueOut;
				} else {
					if (valueIn.compareTo(valueOut) < 0 || valueIn.compareTo(params.getMaxMoney()) > 0)
						throw new VeriException("Transaction input value out of range");
					totalFees = totalFees.add(valueIn.subtract(valueOut));
				}

				if (!isCoinBase && runScripts) {

					FutureTask<VeriException> future = new FutureTask<VeriException>(
							new Verifier(tx, prevOutScripts, verifyFlags));
					scriptVerificationExecutor.execute(future);
					listScriptVerificationResults.add(future);
				}
			}
			if (totalFees.compareTo(params.getMaxMoney()) > 0
					|| block.getBlockInflation(height).add(totalFees).compareTo(coinbaseValue) < 0)
				throw new VeriException("Transaction fees out of range");
			for (Future<VeriException> future : listScriptVerificationResults) {
				VeriException e;
				try {
					e = future.get();
				} catch (InterruptedException thrownE) {
					throw new RuntimeException(thrownE);
				} catch (ExecutionException thrownE) {
					log.error("ChainScript.correctlySpends threw a non-normal exception: " + thrownE.getCause());
					throw new VeriException(
							"Bug in ChainScript.correctlySpends, likely script malformed in some new and interesting way.",
							thrownE);
				}
				if (e != null)
					throw e;
			}
		} catch (VeriException e) {
			scriptVerificationExecutor.shutdownNow();
			blockStore.abortDatabaseBatchWrite();
			throw e;
		} catch (BlockDataException e) {
			scriptVerificationExecutor.shutdownNow();
			blockStore.abortDatabaseBatchWrite();
			throw e;
		}
		return new TxOutputChanges(txOutsCreated, txOutsSpent);
	}

	@Override

	protected synchronized TxOutputChanges connectTransactions(StoredDataBlock newBlock)
			throws VeriException, BlockDataException, PartialException {
		checkState(lock.isHeldByCurrentThread());
		if (!params.passesCheckpoint(newBlock.getHeight(), newBlock.getHeader().getHash()))
			throw new VeriException("Block failed checkpoint lockin at " + newBlock.getHeight());

		blockStore.beginDatabaseBatchWrite();
		StoredInvalidDataBlock block = blockStore.getUndoBlock(newBlock.getHeader().getHash());
		if (block == null) {

			blockStore.abortDatabaseBatchWrite();
			throw new PartialException(newBlock.getHeader().getHash());
		}
		TxOutputChanges txOutChanges;
		try {
			List<Transaction> transactions = block.getTransactions();
			if (transactions != null) {
				LinkedList<Unspent> txOutsSpent = new LinkedList<Unspent>();
				LinkedList<Unspent> txOutsCreated = new LinkedList<Unspent>();
				long sigOps = 0;
				final Set<VerifyFlag> verifyFlags = EnumSet.noneOf(VerifyFlag.class);
				if (newBlock.getHeader().getTimeSeconds() >= NetworkParams.BIP16_ENFORCE_TIME)
					verifyFlags.add(VerifyFlag.P2SH);
				if (!params.isCheckpoint(newBlock.getHeight())) {
					for (Transaction tx : transactions) {
						Sha256Hash hash = tx.getHash();
						if (blockStore.hasUnspentOutputs(hash, tx.getOutputs().size()))
							throw new VeriException("Block failed BIP30 test!");
					}
				}
				Coin totalFees = Coin.ZERO;
				Coin coinbaseValue = null;

				if (scriptVerificationExecutor.isShutdown())
					scriptVerificationExecutor = Executors
							.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				List<Future<VeriException>> listScriptVerificationResults = new ArrayList<Future<VeriException>>(
						transactions.size());
				for (final Transaction tx : transactions) {
					boolean isCoinBase = tx.isCoinBase();
					Coin valueIn = Coin.ZERO;
					Coin valueOut = Coin.ZERO;
					final List<ChainScript> prevOutScripts = new LinkedList<ChainScript>();
					if (!isCoinBase) {
						for (int index = 0; index < tx.getInputs().size(); index++) {
							final TxInput in = tx.getInputs().get(index);
							final Unspent prevOut = blockStore.getTransactionOutput(in.getOutpoint().getHash(),
									in.getOutpoint().getIndex());
							if (prevOut == null)
								throw new VeriException("Attempted spend of a non-existent or already spent output!");
							if (prevOut.isCoinbase()
									&& newBlock.getHeight() - prevOut.getHeight() < params.getSpendableCoinbaseDepth())
								throw new VeriException("Tried to spend coinbase at depth "
										+ (newBlock.getHeight() - prevOut.getHeight()));
							valueIn = valueIn.add(prevOut.getValue());
							if (verifyFlags.contains(VerifyFlag.P2SH)) {
								if (prevOut.getScript().isPayToScriptHash())
									sigOps += ChainScript.getP2SHSigOpCount(in.getScriptBytes());
								if (sigOps > Block.MAX_BLOCK_SIGOPS)
									throw new VeriException("Too many P2SH SigOps in block");
							}

							prevOutScripts.add(prevOut.getScript());

							blockStore.removeUnspentTransactionOutput(prevOut);
							txOutsSpent.add(prevOut);
						}
					}
					Sha256Hash hash = tx.getHash();
					for (TxOutput out : tx.getOutputs()) {
						valueOut = valueOut.add(out.getValue());
						ChainScript chainScript = getScript(out.getScriptBytes());
						Unspent newOut = new Unspent(hash, out.getIndex(), out.getValue(), newBlock.getHeight(),
								isCoinBase, chainScript, getScriptAddress(chainScript));
						blockStore.addUnspentTransactionOutput(newOut);
						txOutsCreated.add(newOut);
					}

					if (valueOut.signum() < 0 || valueOut.compareTo(params.getMaxMoney()) > 0)
						throw new VeriException("Transaction output value out of range");
					if (isCoinBase) {
						coinbaseValue = valueOut;
					} else {
						if (valueIn.compareTo(valueOut) < 0 || valueIn.compareTo(params.getMaxMoney()) > 0)
							throw new VeriException("Transaction input value out of range");
						totalFees = totalFees.add(valueIn.subtract(valueOut));
					}

					if (!isCoinBase) {

						FutureTask<VeriException> future = new FutureTask<VeriException>(
								new Verifier(tx, prevOutScripts, verifyFlags));
						scriptVerificationExecutor.execute(future);
						listScriptVerificationResults.add(future);
					}
				}
				if (totalFees.compareTo(params.getMaxMoney()) > 0 || newBlock.getHeader()
						.getBlockInflation(newBlock.getHeight()).add(totalFees).compareTo(coinbaseValue) < 0)
					throw new VeriException("Transaction fees out of range");
				txOutChanges = new TxOutputChanges(txOutsCreated, txOutsSpent);
				for (Future<VeriException> future : listScriptVerificationResults) {
					VeriException e;
					try {
						e = future.get();
					} catch (InterruptedException thrownE) {
						throw new RuntimeException(thrownE);
					} catch (ExecutionException thrownE) {
						log.error("ChainScript.correctlySpends threw a non-normal exception: " + thrownE.getCause());
						throw new VeriException(
								"Bug in ChainScript.correctlySpends, likely script malformed in some new and interesting way.",
								thrownE);
					}
					if (e != null)
						throw e;
				}
			} else {
				txOutChanges = block.getTxOutChanges();
				if (!params.isCheckpoint(newBlock.getHeight()))
					for (Unspent out : txOutChanges.txOutsCreated) {
						Sha256Hash hash = out.getHash();
						if (blockStore.getTransactionOutput(hash, out.getIndex()) != null)
							throw new VeriException("Block failed BIP30 test!");
					}
				for (Unspent out : txOutChanges.txOutsCreated)
					blockStore.addUnspentTransactionOutput(out);
				for (Unspent out : txOutChanges.txOutsSpent)
					blockStore.removeUnspentTransactionOutput(out);
			}
		} catch (VeriException e) {
			scriptVerificationExecutor.shutdownNow();
			blockStore.abortDatabaseBatchWrite();
			throw e;
		} catch (BlockDataException e) {
			scriptVerificationExecutor.shutdownNow();
			blockStore.abortDatabaseBatchWrite();
			throw e;
		}
		return txOutChanges;
	}

	@Override
	protected void disconnectTransactions(StoredDataBlock oldBlock) throws PartialException, BlockDataException {
		checkState(lock.isHeldByCurrentThread());
		blockStore.beginDatabaseBatchWrite();
		try {
			StoredInvalidDataBlock undoBlock = blockStore.getUndoBlock(oldBlock.getHeader().getHash());
			if (undoBlock == null)
				throw new PartialException(oldBlock.getHeader().getHash());
			TxOutputChanges txOutChanges = undoBlock.getTxOutChanges();
			for (Unspent out : txOutChanges.txOutsSpent)
				blockStore.addUnspentTransactionOutput(out);
			for (Unspent out : txOutChanges.txOutsCreated)
				blockStore.removeUnspentTransactionOutput(out);
		} catch (PartialException e) {
			blockStore.abortDatabaseBatchWrite();
			throw e;
		} catch (BlockDataException e) {
			blockStore.abortDatabaseBatchWrite();
			throw e;
		}
	}

	@Override
	protected void doSetChainHead(StoredDataBlock chainHead) throws BlockDataException {
		checkState(lock.isHeldByCurrentThread());
		blockStore.setVerifiedChainHead(chainHead);
		blockStore.commitDatabaseBatchWrite();
	}

	@Override
	protected void notSettingChainHead() throws BlockDataException {
		blockStore.abortDatabaseBatchWrite();
	}

	@Override
	protected StoredDataBlock getStoredBlockInCurrentScope(Sha256Hash hash) throws BlockDataException {
		checkState(lock.isHeldByCurrentThread());
		return blockStore.getOnceUndoableStoredBlock(hash);
	}
}
