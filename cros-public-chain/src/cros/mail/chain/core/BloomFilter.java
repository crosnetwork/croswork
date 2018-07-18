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

import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptChunk;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.*;

public class BloomFilter extends Message {

	public enum BloomUpdate {
		UPDATE_NONE, UPDATE_ALL,

		UPDATE_P2PUBKEY_ONLY
	}

	private byte[] data;
	private long hashFuncs;
	private long nTweak;
	private byte nFlags;

	private static final long MAX_FILTER_SIZE = 36000;

	private static final int MAX_HASH_FUNCS = 50;

	public BloomFilter(NetworkParams params, byte[] payloadBytes) throws ProtocolException {
		super(params, payloadBytes, 0);
	}

	public BloomFilter(int elements, double falsePositiveRate, long randomNonce) {
		this(elements, falsePositiveRate, randomNonce, BloomUpdate.UPDATE_P2PUBKEY_ONLY);
	}

	public BloomFilter(int elements, double falsePositiveRate, long randomNonce, BloomUpdate updateFlag) {

		int size = (int) (-1 / (pow(log(2), 2)) * elements * log(falsePositiveRate));
		size = max(1, min(size, (int) MAX_FILTER_SIZE * 8) / 8);
		data = new byte[size];

		hashFuncs = (int) (data.length * 8 / (double) elements * log(2));
		hashFuncs = max(1, min(hashFuncs, MAX_HASH_FUNCS));
		this.nTweak = randomNonce;
		this.nFlags = (byte) (0xff & updateFlag.ordinal());
	}

	public double getFalsePositiveRate(int elements) {
		return pow(1 - pow(E, -1.0 * (hashFuncs * elements) / (data.length * 8)), hashFuncs);
	}

	@Override
	public String toString() {
		return "Bloom Filter of size " + data.length + " with " + hashFuncs + " hash functions.";
	}

	@Override
	void parse() throws ProtocolException {
		data = readByteArray();
		if (data.length > MAX_FILTER_SIZE)
			throw new ProtocolException("Bloom filter out of size range.");
		hashFuncs = readUint32();
		if (hashFuncs > MAX_HASH_FUNCS)
			throw new ProtocolException("Bloom filter hash function count out of range");
		nTweak = readUint32();
		nFlags = readBytes(1)[0];
		length = cursor - offset;
	}

	@Override
	void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		stream.write(new VariableInt(data.length).encode());
		stream.write(data);
		Utils.uint32ToByteStreamLE(hashFuncs, stream);
		Utils.uint32ToByteStreamLE(nTweak, stream);
		stream.write(nFlags);
	}

	@Override
	protected void parseLite() throws ProtocolException {

	}

	private static int rotateLeft32(int x, int r) {
		return (x << r) | (x >>> (32 - r));
	}

	public static int murmurHash3(byte[] data, long nTweak, int hashNum, byte[] object) {
		int h1 = (int) (hashNum * 0xFBA4C795L + nTweak);
		final int c1 = 0xcc9e2d51;
		final int c2 = 0x1b873593;

		int numBlocks = (object.length / 4) * 4;

		for (int i = 0; i < numBlocks; i += 4) {
			int k1 = (object[i] & 0xFF) | ((object[i + 1] & 0xFF) << 8) | ((object[i + 2] & 0xFF) << 16)
					| ((object[i + 3] & 0xFF) << 24);

			k1 *= c1;
			k1 = rotateLeft32(k1, 15);
			k1 *= c2;

			h1 ^= k1;
			h1 = rotateLeft32(h1, 13);
			h1 = h1 * 5 + 0xe6546b64;
		}

		int k1 = 0;
		switch (object.length & 3) {
		case 3:
			k1 ^= (object[numBlocks + 2] & 0xff) << 16;

		case 2:
			k1 ^= (object[numBlocks + 1] & 0xff) << 8;

		case 1:
			k1 ^= (object[numBlocks] & 0xff);
			k1 *= c1;
			k1 = rotateLeft32(k1, 15);
			k1 *= c2;
			h1 ^= k1;

		default:

			break;
		}

		h1 ^= object.length;
		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;

		return (int) ((h1 & 0xFFFFFFFFL) % (data.length * 8));
	}

	public synchronized boolean contains(byte[] object) {
		for (int i = 0; i < hashFuncs; i++) {
			if (!Utils.checkBitLE(data, murmurHash3(data, nTweak, i, object)))
				return false;
		}
		return true;
	}

	public synchronized void insert(byte[] object) {
		for (int i = 0; i < hashFuncs; i++)
			Utils.setBitLE(data, murmurHash3(data, nTweak, i, object));
	}

	public synchronized void insert(ECKey key) {
		insert(key.getPubKey());
		insert(key.getPubKeyHash());
	}

	public synchronized void setMatchAll() {
		data = new byte[] { (byte) 0xff };
	}

	public synchronized void merge(BloomFilter filter) {
		if (!this.matchesAll() && !filter.matchesAll()) {
			checkArgument(filter.data.length == this.data.length && filter.hashFuncs == this.hashFuncs
					&& filter.nTweak == this.nTweak);
			for (int i = 0; i < data.length; i++)
				this.data[i] |= filter.data[i];
		} else {
			this.data = new byte[] { (byte) 0xff };
		}
	}

	public synchronized boolean matchesAll() {
		for (byte b : data)
			if (b != (byte) 0xff)
				return false;
		return true;
	}

	public synchronized BloomUpdate getUpdateFlag() {
		if (nFlags == 0)
			return BloomUpdate.UPDATE_NONE;
		else if (nFlags == 1)
			return BloomUpdate.UPDATE_ALL;
		else if (nFlags == 2)
			return BloomUpdate.UPDATE_P2PUBKEY_ONLY;
		else
			throw new IllegalStateException("Unknown flag combination");
	}

	public synchronized LightBlock applyAndUpdate(Block block) {
		List<Transaction> txns = block.getTransactions();
		List<Sha256Hash> txHashes = new ArrayList<Sha256Hash>(txns.size());
		List<Transaction> matched = Lists.newArrayList();
		byte[] bits = new byte[(int) Math.ceil(txns.size() / 8.0)];
		for (int i = 0; i < txns.size(); i++) {
			Transaction tx = txns.get(i);
			txHashes.add(tx.getHash());
			if (applyAndUpdate(tx)) {
				Utils.setBitLE(bits, i);
				matched.add(tx);
			}
		}
		MerkleTree pmt = MerkleTree.buildFromLeaves(block.getParams(), bits, txHashes);
		LightBlock lightBlock = new LightBlock(block.getParams(), block.cloneAsHeader(), pmt);
		for (Transaction transaction : matched)
			lightBlock.provideTransaction(transaction);
		return lightBlock;
	}

	public synchronized boolean applyAndUpdate(Transaction tx) {
		if (contains(tx.getHash().getBytes()))
			return true;
		boolean found = false;
		BloomUpdate flag = getUpdateFlag();
		for (TxOutput output : tx.getOutputs()) {
			ChainScript chainScript = output.getScriptPubKey();
			for (ChainScriptChunk chunk : chainScript.getChunks()) {
				if (!chunk.isPushData())
					continue;
				if (contains(chunk.data)) {
					boolean isSendingToPubKeys = chainScript.isSentToRawPubKey() || chainScript.isSentToMultiSig();
					if (flag == BloomUpdate.UPDATE_ALL
							|| (flag == BloomUpdate.UPDATE_P2PUBKEY_ONLY && isSendingToPubKeys))
						insert(output.getOutPointFor().bitcoinSerialize());
					found = true;
				}
			}
		}
		if (found)
			return true;
		for (TxInput input : tx.getInputs()) {
			if (contains(input.getOutpoint().bitcoinSerialize())) {
				return true;
			}
			for (ChainScriptChunk chunk : input.getScriptSig().getChunks()) {
				if (chunk.isPushData() && contains(chunk.data))
					return true;
			}
		}
		return false;
	}

	@Override
	public synchronized boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BloomFilter other = (BloomFilter) o;
		return hashFuncs == other.hashFuncs && nTweak == other.nTweak && Arrays.equals(data, other.data);
	}

	@Override
	public synchronized int hashCode() {
		return Objects.hashCode(hashFuncs, nTweak, Arrays.hashCode(data));
	}
}
