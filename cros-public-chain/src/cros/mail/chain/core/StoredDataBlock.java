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

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Locale;

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;

import static com.google.common.base.Preconditions.checkState;

public class StoredDataBlock implements Serializable {
	private static final long serialVersionUID = -6097565241243701771L;

	public static final int CHAIN_WORK_BYTES = 12;
	public static final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES];
	public static final int COMPACT_SERIALIZED_SIZE = Block.HEADER_SIZE + CHAIN_WORK_BYTES + 4;

	private Block header;
	private BigInteger chainWork;
	private int height;

	public StoredDataBlock(Block header, BigInteger chainWork, int height) {
		this.header = header;
		this.chainWork = chainWork;
		this.height = height;
	}

	public Block getHeader() {
		return header;
	}

	public BigInteger getChainWork() {
		return chainWork;
	}

	public int getHeight() {
		return height;
	}

	public boolean moreWorkThan(StoredDataBlock other) {
		return chainWork.compareTo(other.chainWork) > 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		StoredDataBlock other = (StoredDataBlock) o;
		return header.equals(other.header) && chainWork.equals(other.chainWork) && height == other.height;
	}

	@Override
	public int hashCode() {

		return header.hashCode() ^ chainWork.hashCode() ^ height;
	}

	public StoredDataBlock build(Block block) throws VeriException {

		BigInteger chainWork = this.chainWork.add(block.getWork());
		int height = this.height + 1;
		return new StoredDataBlock(block, chainWork, height);
	}

	public StoredDataBlock getPrev(BlockData store) throws BlockDataException {
		return store.get(getHeader().getPrevBlockHash());
	}

	public void serializeCompact(ByteBuffer buffer) {
		byte[] chainWorkBytes = getChainWork().toByteArray();
		checkState(chainWorkBytes.length <= CHAIN_WORK_BYTES, "Ran out of space to store chain work!");
		if (chainWorkBytes.length < CHAIN_WORK_BYTES) {

			buffer.put(EMPTY_BYTES, 0, CHAIN_WORK_BYTES - chainWorkBytes.length);
		}
		buffer.put(chainWorkBytes);
		buffer.putInt(getHeight());

		byte[] bytes = getHeader().unsafeBitcoinSerialize();
		buffer.put(bytes, 0, Block.HEADER_SIZE);
	}

	public static StoredDataBlock deserializeCompact(NetworkParams params, ByteBuffer buffer) throws ProtocolException {
		byte[] chainWorkBytes = new byte[StoredDataBlock.CHAIN_WORK_BYTES];
		buffer.get(chainWorkBytes);
		BigInteger chainWork = new BigInteger(1, chainWorkBytes);
		int height = buffer.getInt();
		byte[] header = new byte[Block.HEADER_SIZE + 1];
		buffer.get(header, 0, Block.HEADER_SIZE);
		return new StoredDataBlock(new Block(params, header), chainWork, height);
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "Block %s at height %d: %s", getHeader().getHashAsString(), getHeight(),
				getHeader().toString());
	}
}
