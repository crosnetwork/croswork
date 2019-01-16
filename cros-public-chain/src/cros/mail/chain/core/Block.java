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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import static cros.mail.chain.core.Coin.FIFTY_COINS;
import static cros.mail.chain.core.Sha256Hash.hashTwice;
/**
 * 
 * @author CROS
 *
 */
public class Block extends Message {
	private static final Logger log = LoggerFactory.getLogger(Block.class);
	private static final long serialVersionUID = 2738848929966035281L;

	public static final int HEADER_SIZE = 80;

	static final long ALLOWED_TIME_DRIFT = 2 * 60 * 60;

	public static final int MAX_BLOCK_SIZE = 1 * 1000 * 1000;

	public static final int MAX_BLOCK_SIGOPS = MAX_BLOCK_SIZE / 50;

	public static final long EASIEST_DIFFICULTY_TARGET = 0x207fFFFFL;

	public static final long BLOCK_VERSION_BIP34 = 2;

	public static final long BLOCK_VERSION_BIP66 = 3;

	public static final long BLOCK_VERSION_BIP65 = 4;

	private long version;
	private Sha256Hash prevBlockHash;
	private Sha256Hash merkleRoot;
	private long time;
	private long difficultyTarget;
	private long nonce;

	@Nullable
	List<Transaction> transactions;

	private transient Sha256Hash hash;

	private transient boolean headerParsed;
	private transient boolean transactionsParsed;

	private transient boolean headerBytesValid;
	private transient boolean transactionBytesValid;

	private transient int optimalEncodingMessageSize;

	Block(NetworkParams params) {
		super(params);

		version = 1;
		difficultyTarget = 0x1d07fff8L;
		time = System.currentTimeMillis() / 1000;
		prevBlockHash = Sha256Hash.ZERO_HASH;

		length = 80;
	}

	public Block(NetworkParams params, byte[] payloadBytes) throws ProtocolException {
		super(params, payloadBytes, 0, false, false, payloadBytes.length);
	}

	public Block(NetworkParams params, byte[] payloadBytes, boolean parseLazy, boolean parseRetain, int length)
			throws ProtocolException {
		super(params, payloadBytes, 0, parseLazy, parseRetain, length);
	}

	public Block(NetworkParams params, long version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time,
			long difficultyTarget, long nonce, List<Transaction> transactions) {
		super(params);
		this.version = version;
		this.prevBlockHash = prevBlockHash;
		this.merkleRoot = merkleRoot;
		this.time = time;
		this.difficultyTarget = difficultyTarget;
		this.nonce = nonce;
		this.transactions = new LinkedList<Transaction>();
		this.transactions.addAll(transactions);
	}

	public Coin getBlockInflation(int height) {
		return FIFTY_COINS.shiftRight(height / params.getSubsidyDecreaseBlockCount());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();

		hash = null;
	}

	protected void parseHeader() throws ProtocolException {
		if (headerParsed)
			return;

		cursor = offset;
		version = readUint32();
		prevBlockHash = readHash();
		merkleRoot = readHash();
		time = readUint32();
		difficultyTarget = readUint32();
		nonce = readUint32();

		hash = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(payload, offset, cursor - offset));

		headerParsed = true;
		headerBytesValid = parseRetain;
	}

	protected void parseTransactions() throws ProtocolException {
		if (transactionsParsed)
			return;

		cursor = offset + HEADER_SIZE;
		optimalEncodingMessageSize = HEADER_SIZE;
		if (payload.length == cursor) {

			transactionsParsed = true;
			transactionBytesValid = false;
			return;
		}

		int numTransactions = (int) readVarInt();
		optimalEncodingMessageSize += VariableInt.sizeOf(numTransactions);
		transactions = new ArrayList<Transaction>(numTransactions);
		for (int i = 0; i < numTransactions; i++) {
			Transaction tx = new Transaction(params, payload, cursor, this, parseLazy, parseRetain, UNKNOWN_LENGTH);

			tx.getConfidence().setSource(TransactionDegree.Source.NETWORK);
			transactions.add(tx);
			cursor += tx.getMessageSize();
			optimalEncodingMessageSize += tx.getOptimalEncodingMessageSize();
		}

		transactionsParsed = true;
		transactionBytesValid = parseRetain;
	}

	@Override
	void parse() throws ProtocolException {
		parseHeader();
		parseTransactions();
		length = cursor - offset;
	}

	public int getOptimalEncodingMessageSize() {
		if (optimalEncodingMessageSize != 0)
			return optimalEncodingMessageSize;
		maybeParseTransactions();
		if (optimalEncodingMessageSize != 0)
			return optimalEncodingMessageSize;
		optimalEncodingMessageSize = bitcoinSerialize().length;
		return optimalEncodingMessageSize;
	}

	@Override
	protected void parseLite() throws ProtocolException {

		if (length == UNKNOWN_LENGTH) {
			Preconditions.checkState(parseLazy,
					"Performing lite parse of block transaction as block was initialised from byte array "
							+ "without providing length.  This should never need to happen.");
			parseTransactions();
			length = cursor - offset;
		} else {
			transactionBytesValid = !transactionsParsed || parseRetain && length > HEADER_SIZE;
		}
		headerBytesValid = !headerParsed || parseRetain && length >= HEADER_SIZE;
	}

	private void maybeParseHeader() {
		if (headerParsed || payload == null)
			return;
		try {
			parseHeader();
			if (!(headerBytesValid || transactionBytesValid))
				payload = null;
		} catch (ProtocolException e) {
			throw new LazyParseException(
					"ProtocolException caught during lazy parse.  For safe access to fields call ensureParsed before attempting read or write access",
					e);
		}
	}

	private void maybeParseTransactions() {
		if (transactionsParsed || payload == null)
			return;
		try {
			parseTransactions();
			if (!parseRetain) {
				transactionBytesValid = false;
				if (headerParsed)
					payload = null;
			}
		} catch (ProtocolException e) {
			throw new LazyParseException(
					"ProtocolException caught during lazy parse.  For safe access to fields call ensureParsed before attempting read or write access",
					e);
		}
	}

	@Override
	protected void maybeParse() {
		throw new LazyParseException(
				"checkParse() should never be called on a Block.  Instead use checkParseHeader() and checkParseTransactions()");
	}

	@Override
	public void ensureParsed() throws ProtocolException {
		try {
			maybeParseHeader();
			maybeParseTransactions();
		} catch (LazyParseException e) {
			if (e.getCause() instanceof ProtocolException)
				throw (ProtocolException) e.getCause();
			throw new ProtocolException(e);
		}
	}

	public void ensureParsedHeader() throws ProtocolException {
		try {
			maybeParseHeader();
		} catch (LazyParseException e) {
			if (e.getCause() instanceof ProtocolException)
				throw (ProtocolException) e.getCause();
			throw new ProtocolException(e);
		}
	}

	public void ensureParsedTransactions() throws ProtocolException {
		try {
			maybeParseTransactions();
		} catch (LazyParseException e) {
			if (e.getCause() instanceof ProtocolException)
				throw (ProtocolException) e.getCause();
			throw new ProtocolException(e);
		}
	}

	void writeHeader(OutputStream stream) throws IOException {

		if (headerBytesValid && payload != null && payload.length >= offset + HEADER_SIZE) {
			stream.write(payload, offset, HEADER_SIZE);
			return;
		}

		maybeParseHeader();
		Utils.uint32ToByteStreamLE(version, stream);
		stream.write(prevBlockHash.getReversedBytes());
		stream.write(getMerkleRoot().getReversedBytes());
		Utils.uint32ToByteStreamLE(time, stream);
		Utils.uint32ToByteStreamLE(difficultyTarget, stream);
		Utils.uint32ToByteStreamLE(nonce, stream);
	}

	private void writeTransactions(OutputStream stream) throws IOException {

		if (transactions == null && transactionsParsed) {
			return;
		}

		if (transactionBytesValid && payload != null && payload.length >= offset + length) {
			stream.write(payload, offset + HEADER_SIZE, length - HEADER_SIZE);
			return;
		}

		if (transactions != null) {
			stream.write(new VariableInt(transactions.size()).encode());
			for (Transaction tx : transactions) {
				tx.bitcoinSerialize(stream);
			}
		}
	}

	@Override
	public byte[] bitcoinSerialize() {

		if (headerBytesValid && transactionBytesValid) {
			Preconditions.checkNotNull(payload,
					"Bytes should never be null if headerBytesValid && transactionBytesValid");
			if (length == payload.length) {
				return payload;
			} else {

				byte[] buf = new byte[length];
				System.arraycopy(payload, offset, buf, 0, length);
				return buf;
			}
		}

		ByteArrayOutputStream stream = new UnsafeOutput(
				length == UNKNOWN_LENGTH ? HEADER_SIZE + guessTransactionsLength() : length);
		try {
			writeHeader(stream);
			writeTransactions(stream);
		} catch (IOException e) {

		}
		return stream.toByteArray();
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		writeHeader(stream);

		writeTransactions(stream);
	}

	private int guessTransactionsLength() {
		if (transactionBytesValid)
			return payload.length - HEADER_SIZE;
		if (transactions == null)
			return 0;
		int len = VariableInt.sizeOf(transactions.size());
		for (Transaction tx : transactions) {

			len += tx.length == UNKNOWN_LENGTH ? 255 : tx.length;
		}
		return len;
	}

	@Override
	protected void unCache() {

		unCacheTransactions();
	}

	private void unCacheHeader() {
		maybeParseHeader();
		headerBytesValid = false;
		if (!transactionBytesValid)
			payload = null;
		hash = null;
		checksum = null;
	}

	private void unCacheTransactions() {
		maybeParseTransactions();
		transactionBytesValid = false;
		if (!headerBytesValid)
			payload = null;

		unCacheHeader();

		merkleRoot = null;
	}

	private Sha256Hash calculateHash() {
		try {
			ByteArrayOutputStream bos = new UnsafeOutput(HEADER_SIZE);
			writeHeader(bos);
			return Sha256Hash.wrapReversed(Sha256Hash.hashTwice(bos.toByteArray()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getHashAsString() {
		return getHash().toString();
	}

	@Override
	public Sha256Hash getHash() {
		if (hash == null)
			hash = calculateHash();
		return hash;
	}

	private static BigInteger LARGEST_HASH = BigInteger.ONE.shiftLeft(256);

	public BigInteger getWork() throws VeriException {
		BigInteger target = getDifficultyTargetAsInteger();
		return LARGEST_HASH.divide(target.add(BigInteger.ONE));
	}

	public Block cloneAsHeader() {
		maybeParseHeader();
		Block block = new Block(params);
		copyBitcoinHeaderTo(block);
		return block;
	}

	protected final void copyBitcoinHeaderTo(final Block block) {
		block.nonce = nonce;
		block.prevBlockHash = prevBlockHash;
		block.merkleRoot = getMerkleRoot();
		block.version = version;
		block.time = time;
		block.difficultyTarget = difficultyTarget;
		block.transactions = null;
		block.hash = getHash();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(" block: \n");
		s.append("   hash: ").append(getHashAsString()).append('\n');
		s.append("   version: ").append(version);
		String bips = Joiner.on(", ").skipNulls().join(isBIP34() ? "BIP34" : null, isBIP66() ? "BIP66" : null,
				isBIP65() ? "BIP65" : null);
		if (!bips.isEmpty())
			s.append(" (").append(bips).append(')');
		s.append('\n');
		s.append("   previous block: ").append(getPrevBlockHash()).append("\n");
		s.append("   merkle root: ").append(getMerkleRoot()).append("\n");
		s.append("   time: ").append(time).append(" (").append(Utils.dateTimeFormat(time * 1000)).append(")\n");
		s.append("   difficulty target (nBits): ").append(difficultyTarget).append("\n");
		s.append("   nonce: ").append(nonce).append("\n");
		if (transactions != null && transactions.size() > 0) {
			s.append("   with ").append(transactions.size()).append(" transaction(s):\n");
			for (Transaction tx : transactions) {
				s.append(tx);
			}
		}
		return s.toString();
	}

	public void solve() {
		maybeParseHeader();
		while (true) {
			try {

				if (checkProofOfWork(false))
					return;

				setNonce(getNonce() + 1);
			} catch (VeriException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public BigInteger getDifficultyTargetAsInteger() throws VeriException {
		maybeParseHeader();
		BigInteger target = Utils.decodeCompactBits(difficultyTarget);
		if (target.signum() <= 0 || target.compareTo(params.maxTarget) > 0)
			throw new VeriException("Difficulty target is bad: " + target.toString());
		return target;
	}

	protected boolean checkProofOfWork(boolean throwException) throws VeriException {

		BigInteger target = getDifficultyTargetAsInteger();

		BigInteger h = getHash().toBigInteger();
		if (h.compareTo(target) > 0) {

			if (throwException)
				throw new VeriException(
						"Hash is higher than target: " + getHashAsString() + " vs " + target.toString(16));
			else
				return false;
		}
		return true;
	}

	private void checkTimestamp() throws VeriException {
		maybeParseHeader();

		long currentTime = Utils.currentTimeSeconds();
		if (time > currentTime + ALLOWED_TIME_DRIFT)
			throw new VeriException(String.format(Locale.US, "Block too far in future: %d vs %d", time,
					currentTime + ALLOWED_TIME_DRIFT));
	}

	private void checkSigOps() throws VeriException {

		int sigOps = 0;
		for (Transaction tx : transactions) {
			sigOps += tx.getSigOpCount();
		}
		if (sigOps > MAX_BLOCK_SIGOPS)
			throw new VeriException("Block had too many Signature Operations");
	}

	private void checkMerkleRoot() throws VeriException {
		Sha256Hash calculatedRoot = calculateMerkleRoot();
		if (!calculatedRoot.equals(merkleRoot)) {
			log.error("Merkle tree did not verify");
			throw new VeriException("Merkle hashes do not match: " + calculatedRoot + " vs " + merkleRoot);
		}
	}

	private Sha256Hash calculateMerkleRoot() {
		List<byte[]> tree = buildMerkleTree();
		return Sha256Hash.wrap(tree.get(tree.size() - 1));
	}

	private List<byte[]> buildMerkleTree() {

		maybeParseTransactions();
		ArrayList<byte[]> tree = new ArrayList<byte[]>();

		for (Transaction t : transactions) {
			tree.add(t.getHash().getBytes());
		}
		int levelOffset = 0;

		for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {

			for (int left = 0; left < levelSize; left += 2) {

				int right = Math.min(left + 1, levelSize - 1);
				byte[] leftBytes = Utils.reverseBytes(tree.get(levelOffset + left));
				byte[] rightBytes = Utils.reverseBytes(tree.get(levelOffset + right));
				tree.add(Utils.reverseBytes(hashTwice(leftBytes, 0, 32, rightBytes, 0, 32)));
			}

			levelOffset += levelSize;
		}
		return tree;
	}

	private void checkTransactions() throws VeriException {

		if (!transactions.get(0).isCoinBase())
			throw new VeriException("First tx is not coinbase");

		for (int i = 1; i < transactions.size(); i++) {
			if (transactions.get(i).isCoinBase())
				throw new VeriException("TX " + i + " is coinbase when it should not be.");
		}
	}

	public void verifyHeader() throws VeriException {

		maybeParseHeader();
		checkProofOfWork(true);
		checkTimestamp();
	}

	public void verifyTransactions() throws VeriException {

		if (transactions.isEmpty())
			throw new VeriException("Block had no transactions");
		maybeParseTransactions();
		if (this.getOptimalEncodingMessageSize() > MAX_BLOCK_SIZE)
			throw new VeriException("Block larger than MAX_BLOCK_SIZE");
		checkTransactions();
		checkMerkleRoot();
		checkSigOps();
		for (Transaction transaction : transactions)
			transaction.verify();
	}

	public void verify() throws VeriException {
		verifyHeader();
		verifyTransactions();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Block other = (Block) o;
		return getHash().equals(other.getHash());
	}

	@Override
	public int hashCode() {
		return getHash().hashCode();
	}

	public Sha256Hash getMerkleRoot() {
		maybeParseHeader();
		if (merkleRoot == null) {

			unCacheHeader();
			merkleRoot = calculateMerkleRoot();
		}
		return merkleRoot;
	}

	void setMerkleRoot(Sha256Hash value) {
		unCacheHeader();
		merkleRoot = value;
		hash = null;
	}

	public void addTransaction(Transaction t) {
		addTransaction(t, true);
	}

	void addTransaction(Transaction t, boolean runSanityChecks) {
		unCacheTransactions();
		if (transactions == null) {
			transactions = new ArrayList<Transaction>();
		}
		t.setParent(this);
		if (runSanityChecks && transactions.size() == 0 && !t.isCoinBase())
			throw new RuntimeException("Attempted to add a non-coinbase transaction as the first transaction: " + t);
		else if (runSanityChecks && transactions.size() > 0 && t.isCoinBase())
			throw new RuntimeException("Attempted to add a coinbase transaction when there already is one: " + t);
		transactions.add(t);
		adjustLength(transactions.size(), t.length);

		merkleRoot = null;
		hash = null;
	}

	public long getVersion() {
		maybeParseHeader();
		return version;
	}

	public Sha256Hash getPrevBlockHash() {
		maybeParseHeader();
		return prevBlockHash;
	}

	void setPrevBlockHash(Sha256Hash prevBlockHash) {
		unCacheHeader();
		this.prevBlockHash = prevBlockHash;
		this.hash = null;
	}

	public long getTimeSeconds() {
		maybeParseHeader();
		return time;
	}

	public Date getTime() {
		return new Date(getTimeSeconds() * 1000);
	}

	public void setTime(long time) {
		unCacheHeader();
		this.time = time;
		this.hash = null;
	}

	public long getDifficultyTarget() {
		maybeParseHeader();
		return difficultyTarget;
	}

	public void setDifficultyTarget(long compactForm) {
		unCacheHeader();
		this.difficultyTarget = compactForm;
		this.hash = null;
	}

	public long getNonce() {
		maybeParseHeader();
		return nonce;
	}

	public void setNonce(long nonce) {
		unCacheHeader();
		this.nonce = nonce;
		this.hash = null;
	}

	@Nullable
	public List<Transaction> getTransactions() {
		maybeParseTransactions();
		return transactions == null ? null : ImmutableList.copyOf(transactions);
	}

	private static int txCounter;

	@VisibleForTesting
	void addCoinbaseTransaction(byte[] pubKeyTo, Coin value) {
		unCacheTransactions();
		transactions = new ArrayList<Transaction>();
		Transaction coinbase = new Transaction(params);

		coinbase.addInput(new TxInput(params, coinbase, new ChainScriptBuilder()
				.data(new byte[] { (byte) txCounter, (byte) (txCounter++ >> 8) }).build().getProgram()));
		coinbase.addOutput(new TxOutput(params, coinbase, value,
				ChainScriptBuilder.createOutputScript(ECKey.fromPublicOnly(pubKeyTo)).getProgram()));
		transactions.add(coinbase);
		coinbase.setParent(this);
		coinbase.length = coinbase.bitcoinSerialize().length;
		adjustLength(transactions.size(), coinbase.length);
	}

	static final byte[] EMPTY_BYTES = new byte[32];

	private static final byte[] pubkeyForTesting = new ECKey().getPubKey();

	@VisibleForTesting
	public Block createNextBlock(Address to, long time) {
		return createNextBlock(to, null, time, pubkeyForTesting, FIFTY_COINS);
	}

	Block createNextBlock(@Nullable Address to, @Nullable TxOutPoint prevOut, long time, byte[] pubKey,
			Coin coinbaseValue) {
		Block b = new Block(params);
		b.setDifficultyTarget(difficultyTarget);
		b.addCoinbaseTransaction(pubKey, coinbaseValue);

		if (to != null) {

			Transaction t = new Transaction(params);
			t.addOutput(new TxOutput(params, t, FIFTY_COINS, to));

			TxInput input;
			if (prevOut == null) {
				input = new TxInput(params, t, ChainScript.createInputScript(EMPTY_BYTES, EMPTY_BYTES));

				byte[] counter = new byte[32];
				counter[0] = (byte) txCounter;
				counter[1] = (byte) (txCounter++ >> 8);
				input.getOutpoint().setHash(Sha256Hash.wrap(counter));
			} else {
				input = new TxInput(params, t, ChainScript.createInputScript(EMPTY_BYTES, EMPTY_BYTES), prevOut);
			}
			t.addInput(input);
			b.addTransaction(t);
		}

		b.setPrevBlockHash(getHash());

		if (getTimeSeconds() >= time)
			b.setTime(getTimeSeconds() + 1);
		else
			b.setTime(time);
		b.solve();
		try {
			b.verifyHeader();
		} catch (VeriException e) {
			throw new RuntimeException(e);
		}
		return b;
	}

	@VisibleForTesting
	public Block createNextBlock(@Nullable Address to, TxOutPoint prevOut) {
		return createNextBlock(to, prevOut, getTimeSeconds() + 5, pubkeyForTesting, FIFTY_COINS);
	}

	@VisibleForTesting
	public Block createNextBlock(@Nullable Address to, Coin value) {
		return createNextBlock(to, null, getTimeSeconds() + 5, pubkeyForTesting, value);
	}

	@VisibleForTesting
	public Block createNextBlock(@Nullable Address to) {
		return createNextBlock(to, FIFTY_COINS);
	}

	@VisibleForTesting
	public Block createNextBlockWithCoinbase(byte[] pubKey, Coin coinbaseValue) {
		return createNextBlock(null, null, Utils.currentTimeSeconds(), pubKey, coinbaseValue);
	}

	@VisibleForTesting
	Block createNextBlockWithCoinbase(byte[] pubKey) {
		return createNextBlock(null, null, Utils.currentTimeSeconds(), pubKey, FIFTY_COINS);
	}

	@VisibleForTesting
	boolean isParsedHeader() {
		return headerParsed;
	}

	@VisibleForTesting
	boolean isParsedTransactions() {
		return transactionsParsed;
	}

	@VisibleForTesting
	boolean isHeaderBytesValid() {
		return headerBytesValid;
	}

	@VisibleForTesting
	boolean isTransactionBytesValid() {
		return transactionBytesValid;
	}

	public boolean isBIP34() {
		return version >= BLOCK_VERSION_BIP34;
	}

	public boolean isBIP66() {
		return version >= BLOCK_VERSION_BIP66;
	}

	public boolean isBIP65() {
		return version >= BLOCK_VERSION_BIP65;
	}
}
