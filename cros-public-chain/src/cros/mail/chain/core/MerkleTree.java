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

import static cros.mail.chain.core.Utils.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerkleTree extends Message {

	private int transactionCount;

	private byte[] matchedChildBits;

	private List<Sha256Hash> hashes;

	public MerkleTree(NetworkParams params, byte[] payloadBytes, int offset) throws ProtocolException {
		super(params, payloadBytes, offset);
	}

	public MerkleTree(NetworkParams params, byte[] bits, List<Sha256Hash> hashes, int origTxCount) {
		super(params);
		this.matchedChildBits = bits;
		this.hashes = hashes;
		this.transactionCount = origTxCount;
	}

	public static MerkleTree buildFromLeaves(NetworkParams params, byte[] includeBits, List<Sha256Hash> allLeafHashes) {

		int height = 0;
		while (getTreeWidth(allLeafHashes.size(), height) > 1)
			height++;
		List<Boolean> bitList = new ArrayList<Boolean>();
		List<Sha256Hash> hashes = new ArrayList<Sha256Hash>();
		traverseAndBuild(height, 0, allLeafHashes, includeBits, bitList, hashes);
		byte[] bits = new byte[(int) Math.ceil(bitList.size() / 8.0)];
		for (int i = 0; i < bitList.size(); i++)
			if (bitList.get(i))
				Utils.setBitLE(bits, i);
		return new MerkleTree(params, bits, hashes, allLeafHashes.size());
	}

	@Override
	public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		uint32ToByteStreamLE(transactionCount, stream);

		stream.write(new VariableInt(hashes.size()).encode());
		for (Sha256Hash hash : hashes)
			stream.write(hash.getReversedBytes());

		stream.write(new VariableInt(matchedChildBits.length).encode());
		stream.write(matchedChildBits);
	}

	@Override
	void parse() throws ProtocolException {
		transactionCount = (int) readUint32();

		int nHashes = (int) readVarInt();
		hashes = new ArrayList<Sha256Hash>(nHashes);
		for (int i = 0; i < nHashes; i++)
			hashes.add(readHash());

		int nFlagBytes = (int) readVarInt();
		matchedChildBits = readBytes(nFlagBytes);

		length = cursor - offset;
	}

	private static void traverseAndBuild(int height, int pos, List<Sha256Hash> allLeafHashes, byte[] includeBits,
			List<Boolean> matchedChildBits, List<Sha256Hash> resultHashes) {
		boolean parentOfMatch = false;

		for (int p = pos << height; p < (pos + 1) << height && p < allLeafHashes.size(); p++) {
			if (Utils.checkBitLE(includeBits, p)) {
				parentOfMatch = true;
				break;
			}
		}

		matchedChildBits.add(parentOfMatch);
		if (height == 0 || !parentOfMatch) {

			resultHashes.add(calcHash(height, pos, allLeafHashes));
		} else {

			int h = height - 1;
			int p = pos * 2;
			traverseAndBuild(h, p, allLeafHashes, includeBits, matchedChildBits, resultHashes);
			if (p + 1 < getTreeWidth(allLeafHashes.size(), h))
				traverseAndBuild(h, p + 1, allLeafHashes, includeBits, matchedChildBits, resultHashes);
		}
	}

	private static Sha256Hash calcHash(int height, int pos, List<Sha256Hash> hashes) {
		if (height == 0) {

			return hashes.get(pos);
		}
		int h = height - 1;
		int p = pos * 2;
		Sha256Hash left = calcHash(h, p, hashes);

		Sha256Hash right;
		if (p + 1 < getTreeWidth(hashes.size(), h)) {
			right = calcHash(h, p + 1, hashes);
		} else {
			right = left;
		}
		return combineLeftRight(left.getBytes(), right.getBytes());
	}

	@Override
	protected void parseLite() {

	}

	private static int getTreeWidth(int transactionCount, int height) {
		return (transactionCount + (1 << height) - 1) >> height;
	}

	private static class ValuesUsed {
		public int bitsUsed = 0, hashesUsed = 0;
	}

	private Sha256Hash recursiveExtractHashes(int height, int pos, ValuesUsed used, List<Sha256Hash> matchedHashes)
			throws VeriException {
		if (used.bitsUsed >= matchedChildBits.length * 8) {

			throw new VeriException("MerkleTree overflowed its bits array");
		}
		boolean parentOfMatch = checkBitLE(matchedChildBits, used.bitsUsed++);
		if (height == 0 || !parentOfMatch) {

			if (used.hashesUsed >= hashes.size()) {

				throw new VeriException("MerkleTree overflowed its hash array");
			}
			Sha256Hash hash = hashes.get(used.hashesUsed++);
			if (height == 0 && parentOfMatch)
				matchedHashes.add(hash);
			return hash;
		} else {

			byte[] left = recursiveExtractHashes(height - 1, pos * 2, used, matchedHashes).getBytes(), right;
			if (pos * 2 + 1 < getTreeWidth(transactionCount, height - 1)) {
				right = recursiveExtractHashes(height - 1, pos * 2 + 1, used, matchedHashes).getBytes();
				if (Arrays.equals(right, left))
					throw new VeriException("Invalid merkle tree with duplicated left/right branches");
			} else {
				right = left;
			}

			return combineLeftRight(left, right);
		}
	}

	private static Sha256Hash combineLeftRight(byte[] left, byte[] right) {
		return Sha256Hash.wrapReversed(Sha256Hash.hashTwice(reverseBytes(left), 0, 32, reverseBytes(right), 0, 32));
	}

	public Sha256Hash getTxnHashAndMerkleRoot(List<Sha256Hash> matchedHashesOut) throws VeriException {
		matchedHashesOut.clear();

		if (transactionCount == 0)
			throw new VeriException("Got a CPartialMerkleTree with 0 transactions");

		if (transactionCount > Block.MAX_BLOCK_SIZE / 60)
			throw new VeriException("Got a CPartialMerkleTree with more transactions than is possible");

		if (hashes.size() > transactionCount)
			throw new VeriException("Got a CPartialMerkleTree with more hashes than transactions");

		if (matchedChildBits.length * 8 < hashes.size())
			throw new VeriException("Got a CPartialMerkleTree with fewer matched bits than hashes");

		int height = 0;
		while (getTreeWidth(transactionCount, height) > 1)
			height++;

		ValuesUsed used = new ValuesUsed();
		Sha256Hash merkleRoot = recursiveExtractHashes(height, 0, used, matchedHashesOut);

		if ((used.bitsUsed + 7) / 8 != matchedChildBits.length ||

				used.hashesUsed != hashes.size())
			throw new VeriException("Got a CPartialMerkleTree that didn't need all the data it provided");

		return merkleRoot;
	}

	public int getTransactionCount() {
		return transactionCount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MerkleTree tree = (MerkleTree) o;

		if (transactionCount != tree.transactionCount)
			return false;
		if (!hashes.equals(tree.hashes))
			return false;
		if (!Arrays.equals(matchedChildBits, tree.matchedChildBits))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = transactionCount;
		result = 31 * result + Arrays.hashCode(matchedChildBits);
		result = 31 * result + hashes.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "MerkleTree{" + "transactionCount=" + transactionCount + ", matchedChildBits="
				+ Arrays.toString(matchedChildBits) + ", hashes=" + hashes + '}';
	}
}
