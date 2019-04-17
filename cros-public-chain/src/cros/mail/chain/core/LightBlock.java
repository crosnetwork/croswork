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

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
/**
 * 
 * @author CROS
 *
 */
public class LightBlock extends Message {

	public static final int MIN_PROTOCOL_VERSION = 70000;
	private Block header;

	private MerkleTree merkleTree;
	private List<Sha256Hash> cachedTransactionHashes = null;

	private Map<Sha256Hash, Transaction> associatedTransactions = new HashMap<Sha256Hash, Transaction>();

	public LightBlock(NetworkParams params, byte[] payloadBytes) throws ProtocolException {
		super(params, payloadBytes, 0);
	}

	public LightBlock(NetworkParams params, Block header, MerkleTree pmt) {
		super(params);
		this.header = header;
		this.merkleTree = pmt;
	}

	@Override
	public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		if (header.transactions == null)
			header.bitcoinSerializeToStream(stream);
		else
			header.cloneAsHeader().bitcoinSerializeToStream(stream);
		merkleTree.bitcoinSerializeToStream(stream);
	}

	@Override
	void parse() throws ProtocolException {
		byte[] headerBytes = new byte[Block.HEADER_SIZE];
		System.arraycopy(payload, 0, headerBytes, 0, Block.HEADER_SIZE);
		header = new Block(params, headerBytes);

		merkleTree = new MerkleTree(params, payload, Block.HEADER_SIZE);

		length = Block.HEADER_SIZE + merkleTree.getMessageSize();
	}

	@Override
	protected void parseLite() throws ProtocolException {

	}

	public List<Sha256Hash> getTransactionHashes() throws VeriException {
		if (cachedTransactionHashes != null)
			return Collections.unmodifiableList(cachedTransactionHashes);
		List<Sha256Hash> hashesMatched = new LinkedList<Sha256Hash>();
		if (header.getMerkleRoot().equals(merkleTree.getTxnHashAndMerkleRoot(hashesMatched))) {
			cachedTransactionHashes = hashesMatched;
			return Collections.unmodifiableList(cachedTransactionHashes);
		} else
			throw new VeriException("Merkle root of block header does not match merkle root of partial merkle tree.");
	}

	public Block getBlockHeader() {
		return header.cloneAsHeader();
	}

	@Override
	public Sha256Hash getHash() {
		return header.getHash();
	}

	public boolean provideTransaction(Transaction tx) throws VeriException {
		Sha256Hash hash = tx.getHash();
		if (getTransactionHashes().contains(hash)) {
			associatedTransactions.put(hash, tx);
			return true;
		}
		return false;
	}

	public MerkleTree getPartialMerkleTree() {
		return merkleTree;
	}

	public Map<Sha256Hash, Transaction> getAssociatedTransactions() {
		return Collections.unmodifiableMap(associatedTransactions);
	}

	public int getTransactionCount() {
		return merkleTree.getTransactionCount();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		LightBlock block = (LightBlock) o;

		if (!associatedTransactions.equals(block.associatedTransactions))
			return false;
		if (!header.equals(block.header))
			return false;
		if (!merkleTree.equals(block.merkleTree))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = header.hashCode();
		result = 31 * result + merkleTree.hashCode();
		result = 31 * result + associatedTransactions.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "LightBlock{merkleTree=" + merkleTree + ", header=" + header + '}';
	}
}
