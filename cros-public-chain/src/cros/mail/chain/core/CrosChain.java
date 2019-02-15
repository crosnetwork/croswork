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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;

/**
 * 
 * @author CROS
 *
 */
public class CrosChain extends AbstractChain {

	protected final BlockData blockData;

	public CrosChain(Context context, Wallet wallet, BlockData blockData) throws BlockDataException {
		this(context, new ArrayList<CrosChainListener>(), blockData);
		addWallet(wallet);
	}

	public CrosChain(NetworkParams params, Wallet wallet, BlockData blockData) throws BlockDataException {
		this(Context.getOrCreate(params), wallet, blockData);
	}

	public CrosChain(Context context, BlockData blockData) throws BlockDataException {
		this(context, new ArrayList<CrosChainListener>(), blockData);
	}

	public CrosChain(NetworkParams params, BlockData blockData) throws BlockDataException {
		this(params, new ArrayList<CrosChainListener>(), blockData);
	}

	public CrosChain(Context params, List<CrosChainListener> wallets, BlockData blockData) throws BlockDataException {
		super(params, wallets, blockData);
		this.blockData = blockData;
	}

	public CrosChain(NetworkParams params, List<CrosChainListener> wallets, BlockData blockData)
			throws BlockDataException {
		this(Context.getOrCreate(params), wallets, blockData);
	}

	@Override
	protected StoredDataBlock addToBlockStore(StoredDataBlock storedPrev, Block blockHeader,
			TxOutputChanges txOutChanges) throws BlockDataException, VeriException {
		StoredDataBlock newBlock = storedPrev.build(blockHeader);
		blockData.put(newBlock);
		return newBlock;
	}

	@Override
	protected StoredDataBlock addToBlockStore(StoredDataBlock storedPrev, Block blockHeader)
			throws BlockDataException, VeriException {
		StoredDataBlock newBlock = storedPrev.build(blockHeader);
		blockData.put(newBlock);
		return newBlock;
	}

	@Override
	protected void rollbackBlockStore(int height) throws BlockDataException {
		lock.lock();
		try {
			int currentHeight = getBestChainHeight();
			checkArgument(height >= 0 && height <= currentHeight, "Bad height: %s", height);
			if (height == currentHeight)
				return;

			StoredDataBlock newChainHead = blockData.getChainHead();
			while (newChainHead.getHeight() > height) {
				newChainHead = newChainHead.getPrev(blockData);
				if (newChainHead == null)
					throw new BlockDataException("Unreachable height");
			}

			blockData.put(newChainHead);
			this.setChainHead(newChainHead);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected boolean shouldVerifyTransactions() {
		return false;
	}

	@Override
	protected TxOutputChanges connectTransactions(int height, Block block) {

		throw new UnsupportedOperationException();
	}

	@Override
	protected TxOutputChanges connectTransactions(StoredDataBlock newBlock) {

		throw new UnsupportedOperationException();
	}

	@Override
	protected void disconnectTransactions(StoredDataBlock block) {

		throw new UnsupportedOperationException();
	}

	@Override
	protected void doSetChainHead(StoredDataBlock chainHead) throws BlockDataException {
		blockData.setChainHead(chainHead);
	}

	@Override
	protected void notSettingChainHead() throws BlockDataException {

	}

	@Override
	protected StoredDataBlock getStoredBlockInCurrentScope(Sha256Hash hash) throws BlockDataException {
		return blockData.get(hash);
	}

	@Override
	public boolean add(LightBlock block) throws VeriException, PartialException {
		boolean success = super.add(block);
		if (success) {
			trackFilteredTransactions(block.getTransactionCount());
		}
		return success;
	}
}
