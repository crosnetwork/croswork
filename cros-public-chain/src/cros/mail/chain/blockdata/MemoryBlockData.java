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
package cros.mail.chain.blockdata;

import java.util.LinkedHashMap;
import java.util.Map;

import cros.mail.chain.core.*;

public class MemoryBlockData implements BlockData {
	private LinkedHashMap<Sha256Hash, StoredDataBlock> blockMap = new LinkedHashMap<Sha256Hash, StoredDataBlock>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredDataBlock> eldest) {
			return blockMap.size() > 5000;
		}
	};
	private StoredDataBlock chainHead;
	private NetworkParams params;

	public MemoryBlockData(NetworkParams params) {

		try {
			Block genesisHeader = params.getGenesisBlock().cloneAsHeader();
			StoredDataBlock storedGenesis = new StoredDataBlock(genesisHeader, genesisHeader.getWork(), 0);
			put(storedGenesis);
			setChainHead(storedGenesis);
			this.params = params;
		} catch (BlockDataException e) {
			throw new RuntimeException(e);
		} catch (VeriException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void put(StoredDataBlock block) throws BlockDataException {
		if (blockMap == null)
			throw new BlockDataException("MemoryBlockData is closed");
		Sha256Hash hash = block.getHeader().getHash();
		blockMap.put(hash, block);
	}

	@Override
	public synchronized StoredDataBlock get(Sha256Hash hash) throws BlockDataException {
		if (blockMap == null)
			throw new BlockDataException("MemoryBlockData is closed");
		return blockMap.get(hash);
	}

	@Override
	public StoredDataBlock getChainHead() throws BlockDataException {
		if (blockMap == null)
			throw new BlockDataException("MemoryBlockData is closed");
		return chainHead;
	}

	@Override
	public void setChainHead(StoredDataBlock chainHead) throws BlockDataException {
		if (blockMap == null)
			throw new BlockDataException("MemoryBlockData is closed");
		this.chainHead = chainHead;
	}

	@Override
	public void close() {
		blockMap = null;
	}

	@Override
	public NetworkParams getParams() {
		return params;
	}
}
