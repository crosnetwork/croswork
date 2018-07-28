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


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import cros.mail.chain.core.TransactionDegree.ConfidenceType;
import cros.mail.chain.encrypt.Signature;
import cros.mail.chain.misc.InterchangeRate;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptBuilder;
import cros.mail.chain.script.ChainScriptCodes;
import cros.mail.chain.wallet.CrosWalletTransaction.Pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

import static cros.mail.chain.core.Utils.*;
import static com.google.common.base.Preconditions.checkState;

public class Transaction extends ChildMessage implements Serializable {

	public static final Comparator<Transaction> SORT_TX_BY_UPDATE_TIME = new Comparator<Transaction>() {
		@Override
		public int compare(final Transaction tx1, final Transaction tx2) {
			final long time1 = tx1.getUpdateTime().getTime();
			final long time2 = tx2.getUpdateTime().getTime();
			final int updateTimeComparison = -(Longs.compare(time1, time2));

			return updateTimeComparison != 0 ? updateTimeComparison : tx1.getHash().compareTo(tx2.getHash());
		}
	};

	public static final Comparator<Transaction> SORT_TX_BY_HEIGHT = new Comparator<Transaction>() {
		@Override
		public int compare(final Transaction tx1, final Transaction tx2) {
			final int height1 = tx1.getConfidence().getAppearedAtChainHeight();
			final int height2 = tx2.getConfidence().getAppearedAtChainHeight();
			final int heightComparison = -(Ints.compare(height1, height2));

			return heightComparison != 0 ? heightComparison : tx1.getHash().compareTo(tx2.getHash());
		}
	};
	private static final Logger log = LoggerFactory.getLogger(Transaction.class);
	private static final long serialVersionUID = -8567546957352643140L;

	public static final int LOCKTIME_THRESHOLD = 500000000;

	public static final int MAX_STANDARD_TX_SIZE = 100000;

	public static final Coin REFERENCE_DEFAULT_MIN_TX_FEE = Coin.valueOf(5000);

	public static final Coin MIN_NONDUST_OUTPUT = Coin.valueOf(2730);

	private long version;
	private ArrayList<TxInput> inputs;
	private ArrayList<TxOutput> outputs;

	private long lockTime;

	private Date updatedAt;

	private transient Sha256Hash hash;

	@Nullable
	private TransactionDegree confidence;

	private Map<Sha256Hash, Integer> appearsInHashes;

	private transient int optimalEncodingMessageSize;

	public enum Purpose {

		UNKNOWN,

		USER_PAYMENT,

		KEY_ROTATION,

		ASSURANCE_CONTRACT_CLAIM,

		ASSURANCE_CONTRACT_PLEDGE,

		ASSURANCE_CONTRACT_STUB,

		RAISE_FEE,

	}

	private Purpose purpose = Purpose.UNKNOWN;

	@Nullable
	private InterchangeRate interchangeRate;

	@Nullable
	private String memo;

	public Transaction(NetworkParams params) {
		super(params);
		version = 1;
		inputs = new ArrayList<TxInput>();
		outputs = new ArrayList<TxOutput>();

		length = 8;
	}

	public Transaction(NetworkParams params, byte[] payloadBytes) throws ProtocolException {
		super(params, payloadBytes, 0);
	}

	public Transaction(NetworkParams params, byte[] payload, int offset) throws ProtocolException {
		super(params, payload, offset);

	}

	public Transaction(NetworkParams params, byte[] payload, int offset, @Nullable Message parent, boolean parseLazy,
			boolean parseRetain, int length) throws ProtocolException {
		super(params, payload, offset, parent, parseLazy, parseRetain, length);
	}

	public Transaction(NetworkParams params, byte[] payload, @Nullable Message parent, boolean parseLazy,
			boolean parseRetain, int length) throws ProtocolException {
		super(params, payload, 0, parent, parseLazy, parseRetain, length);
	}

	@Override
	public Sha256Hash getHash() {
		if (hash == null) {
			byte[] bits = bitcoinSerialize();
			hash = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(bits));
		}
		return hash;
	}

	void setHash(Sha256Hash hash) {
		this.hash = hash;
	}

	public String getHashAsString() {
		return getHash().toString();
	}

	Coin getValueSentToMe(TransactionBlock transactionBlock, boolean includeSpent) {
		maybeParse();

		Coin v = Coin.ZERO;
		for (TxOutput o : outputs) {
			if (!o.isMineOrWatched(transactionBlock))
				continue;
			if (!includeSpent && !o.isAvailableForSpending())
				continue;
			v = v.add(o.getValue());
		}
		return v;
	}

	boolean isConsistent(TransactionBlock transactionBlock, boolean isSpent) {
		boolean isActuallySpent = true;
		for (TxOutput o : outputs) {
			if (o.isAvailableForSpending()) {
				if (o.isMineOrWatched(transactionBlock))
					isActuallySpent = false;
				if (o.getSpentBy() != null) {
					log.error("isAvailableForSpending != spentBy");
					return false;
				}
			} else {
				if (o.getSpentBy() == null) {
					log.error("isAvailableForSpending != spentBy");
					return false;
				}
			}
		}
		return isActuallySpent == isSpent;
	}

	public Coin getValueSentToMe(TransactionBlock transactionBlock) {
		return getValueSentToMe(transactionBlock, true);
	}

	@Nullable
	public Map<Sha256Hash, Integer> getAppearsInHashes() {
		return appearsInHashes != null ? ImmutableMap.copyOf(appearsInHashes) : null;
	}

	public boolean isPending() {
		return getConfidence().getConfidenceType() == TransactionDegree.ConfidenceType.PENDING;
	}

	public void setBlockAppearance(StoredDataBlock block, boolean bestChain, int relativityOffset) {
		long blockTime = block.getHeader().getTimeSeconds() * 1000;
		if (bestChain && (updatedAt == null || updatedAt.getTime() == 0 || updatedAt.getTime() > blockTime)) {
			updatedAt = new Date(blockTime);
		}

		addBlockAppearance(block.getHeader().getHash(), relativityOffset);

		if (bestChain) {
			TransactionDegree transactionDegree = getConfidence();

			transactionDegree.setAppearedAtChainHeight(block.getHeight());
		}
	}

	public void addBlockAppearance(final Sha256Hash blockHash, int relativityOffset) {
		if (appearsInHashes == null) {

			appearsInHashes = new TreeMap<Sha256Hash, Integer>();
		}
		appearsInHashes.put(blockHash, relativityOffset);
	}

	public Coin getValueSentFromMe(TransactionBlock wallet) throws ScriptException {
		maybeParse();

		Coin v = Coin.ZERO;
		for (TxInput input : inputs) {

			TxOutput connected = input.getConnectedOutput(wallet.getTransactionPool(Pool.UNSPENT));
			if (connected == null)
				connected = input.getConnectedOutput(wallet.getTransactionPool(Pool.SPENT));
			if (connected == null)
				connected = input.getConnectedOutput(wallet.getTransactionPool(Pool.PENDING));
			if (connected == null)
				continue;

			if (!connected.isMineOrWatched(wallet))
				continue;
			v = v.add(connected.getValue());
		}
		return v;
	}

	@Nullable
	private Coin cachedValue;
	@Nullable
	private TransactionBlock cachedForBag;

	public Coin getValue(TransactionBlock wallet) throws ScriptException {

		boolean isAndroid = Utils.isAndroidRuntime();
		if (isAndroid && cachedValue != null && cachedForBag == wallet)
			return cachedValue;
		Coin result = getValueSentToMe(wallet).subtract(getValueSentFromMe(wallet));
		if (isAndroid) {
			cachedValue = result;
			cachedForBag = wallet;
		}
		return result;
	}

	public Coin getFee() {
		Coin fee = Coin.ZERO;
		for (TxInput input : inputs) {
			if (input.getValue() == null)
				return null;
			fee = fee.add(input.getValue());
		}
		for (TxOutput output : outputs) {
			fee = fee.subtract(output.getValue());
		}
		return fee;
	}

	public boolean isAnyOutputSpent() {
		maybeParse();
		for (TxOutput output : outputs) {
			if (!output.isAvailableForSpending())
				return true;
		}
		return false;
	}

	public boolean isEveryOwnedOutputSpent(TransactionBlock transactionBlock) {
		maybeParse();
		for (TxOutput output : outputs) {
			if (output.isAvailableForSpending() && output.isMineOrWatched(transactionBlock))
				return false;
		}
		return true;
	}

	public Date getUpdateTime() {
		if (updatedAt == null) {

			updatedAt = new Date(0);
		}
		return updatedAt;
	}

	public void setUpdateTime(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public enum SigHash {
		ALL, NONE, SINGLE,
	}

	public static final byte SIGHASH_ANYONECANPAY_VALUE = (byte) 0x80;

	@Override
	protected void unCache() {
		super.unCache();
		hash = null;
	}

	@Override
	protected void parseLite() throws ProtocolException {

		if (parseLazy && length == UNKNOWN_LENGTH) {

			length = calcLength(payload, offset);
			cursor = offset + length;
		}
	}

	protected static int calcLength(byte[] buf, int offset) {
		VariableInt varint;

		int cursor = offset + 4;

		int i;
		long scriptLen;

		varint = new VariableInt(buf, cursor);
		long txInCount = varint.value;
		cursor += varint.getOriginalSizeInBytes();

		for (i = 0; i < txInCount; i++) {

			cursor += 36;
			varint = new VariableInt(buf, cursor);
			scriptLen = varint.value;

			cursor += scriptLen + 4 + varint.getOriginalSizeInBytes();
		}

		varint = new VariableInt(buf, cursor);
		long txOutCount = varint.value;
		cursor += varint.getOriginalSizeInBytes();

		for (i = 0; i < txOutCount; i++) {

			cursor += 8;
			varint = new VariableInt(buf, cursor);
			scriptLen = varint.value;
			cursor += scriptLen + varint.getOriginalSizeInBytes();
		}

		return cursor - offset + 4;
	}

	@Override
	void parse() throws ProtocolException {

		if (parsed)
			return;

		cursor = offset;

		version = readUint32();
		optimalEncodingMessageSize = 4;

		long numInputs = readVarInt();
		optimalEncodingMessageSize += VariableInt.sizeOf(numInputs);
		inputs = new ArrayList<TxInput>((int) numInputs);
		for (long i = 0; i < numInputs; i++) {
			TxInput input = new TxInput(params, this, payload, cursor, parseLazy, parseRetain);
			inputs.add(input);
			long scriptLen = readVarInt(TxOutPoint.MESSAGE_LENGTH);
			optimalEncodingMessageSize += TxOutPoint.MESSAGE_LENGTH + VariableInt.sizeOf(scriptLen) + scriptLen + 4;
			cursor += scriptLen + 4;
		}

		long numOutputs = readVarInt();
		optimalEncodingMessageSize += VariableInt.sizeOf(numOutputs);
		outputs = new ArrayList<TxOutput>((int) numOutputs);
		for (long i = 0; i < numOutputs; i++) {
			TxOutput output = new TxOutput(params, this, payload, cursor, parseLazy, parseRetain);
			outputs.add(output);
			long scriptLen = readVarInt(8);
			optimalEncodingMessageSize += 8 + VariableInt.sizeOf(scriptLen) + scriptLen;
			cursor += scriptLen;
		}
		lockTime = readUint32();
		optimalEncodingMessageSize += 4;
		length = cursor - offset;
	}

	public int getOptimalEncodingMessageSize() {
		if (optimalEncodingMessageSize != 0)
			return optimalEncodingMessageSize;
		maybeParse();
		if (optimalEncodingMessageSize != 0)
			return optimalEncodingMessageSize;
		optimalEncodingMessageSize = getMessageSize();
		return optimalEncodingMessageSize;
	}

	public boolean isCoinBase() {
		maybeParse();
		return inputs.size() == 1 && inputs.get(0).isCoinBase();
	}

	public boolean isMature() {
		if (!isCoinBase())
			return true;

		if (getConfidence().getConfidenceType() != ConfidenceType.BUILDING)
			return false;

		return getConfidence().getDepthInBlocks() >= params.getSpendableCoinbaseDepth();
	}

	@Override
	public String toString() {
		return toString(null);
	}

	public String toString(@Nullable AbstractChain chain) {

		StringBuilder s = new StringBuilder();
		s.append("  ").append(getHashAsString()).append('\n');
		if (hasConfidence())
			s.append("  confidence: ").append(getConfidence()).append('\n');
		if (isTimeLocked()) {
			String time;
			if (lockTime < LOCKTIME_THRESHOLD) {
				time = "block " + lockTime;
				if (chain != null) {
					time = time + " (estimated to be reached at " + chain.estimateBlockTime((int) lockTime).toString()
							+ ")";
				}
			} else {
				time = new Date(lockTime * 1000).toString();
			}
			s.append(String.format(Locale.US, "  time locked until %s%n", time));
		}
		if (isOptInFullRBF()) {
			s.append("  opts into full replace-by-fee\n");
		}
		if (inputs.size() == 0) {
			s.append(String.format(Locale.US, "  INCOMPLETE: No inputs!%n"));
			return s.toString();
		}
		if (isCoinBase()) {
			String script;
			String script2;
			try {
				script = inputs.get(0).getScriptSig().toString();
				script2 = outputs.get(0).getScriptPubKey().toString();
			} catch (ScriptException e) {
				script = "???";
				script2 = "???";
			}
			s.append("     == COINBASE TXN (scriptSig ").append(script).append(")  (scriptPubKey ").append(script2)
					.append(")\n");
			return s.toString();
		}
		for (TxInput in : inputs) {
			s.append("     ");
			s.append("in   ");

			try {
				ChainScript scriptSig = in.getScriptSig();
				s.append(scriptSig);
				if (in.getValue() != null)
					s.append(" ").append(in.getValue().toFriendlyString());
				s.append("\n          ");
				s.append("outpoint:");
				final TxOutPoint outpoint = in.getOutpoint();
				s.append(outpoint.toString());
				final TxOutput connectedOutput = outpoint.getConnectedOutput();
				if (connectedOutput != null) {
					ChainScript scriptPubKey = connectedOutput.getScriptPubKey();
					if (scriptPubKey.isSentToAddress() || scriptPubKey.isPayToScriptHash()) {
						s.append(" hash160:");
						s.append(Utils.HEX.encode(scriptPubKey.getPubKeyHash()));
					}
				}
				String flags = Joiner.on(", ").skipNulls().join(in.hasSequence() ? "has sequence" : null,
						in.isOptInFullRBF() ? "opts into full RBF" : null);
				if (!flags.isEmpty())
					s.append("\n          (").append(flags).append(')');
			} catch (Exception e) {
				s.append("[exception: ").append(e.getMessage()).append("]");
			}
			s.append(String.format(Locale.US, "%n"));
		}
		for (TxOutput out : outputs) {
			s.append("     ");
			s.append("out  ");
			try {
				ChainScript scriptPubKey = out.getScriptPubKey();
				s.append(scriptPubKey);
				s.append(" ");
				s.append(out.getValue().toFriendlyString());
				if (!out.isAvailableForSpending()) {
					s.append(" Spent");
				}
				if (out.getSpentBy() != null) {
					s.append(" by ");
					s.append(out.getSpentBy().getParentTransaction().getHashAsString());
				}
			} catch (Exception e) {
				s.append("[exception: ").append(e.getMessage()).append("]");
			}
			s.append(String.format(Locale.US, "%n"));
		}
		final Coin fee = getFee();
		if (fee != null) {
			final int size = unsafeBitcoinSerialize().length;
			s.append("     fee  ").append(fee.multiply(1000).divide(size).toFriendlyString()).append("/kB, ")
					.append(fee.toFriendlyString()).append(" for ").append(size).append(" bytes\n");
		}
		if (purpose != null)
			s.append("     prps ").append(purpose).append(String.format(Locale.US, "%n"));
		return s.toString();
	}

	public void clearInputs() {
		unCache();
		for (TxInput input : inputs) {
			input.setParent(null);
		}
		inputs.clear();

		this.length = this.bitcoinSerialize().length;
	}

	public TxInput addInput(TxOutput from) {
		return addInput(new TxInput(params, this, from));
	}

	public TxInput addInput(TxInput input) {
		unCache();
		input.setParent(this);
		inputs.add(input);
		adjustLength(inputs.size(), input.length);
		return input;
	}

	public TxInput addInput(Sha256Hash spendTxHash, long outputIndex, ChainScript chainScript) {
		return addInput(
				new TxInput(params, this, chainScript.getProgram(), new TxOutPoint(params, outputIndex, spendTxHash)));
	}

	public TxInput addSignedInput(TxOutPoint prevOut, ChainScript scriptPubKey, ECKey sigKey, SigHash sigHash,
			boolean anyoneCanPay) throws ScriptException {

		checkState(!outputs.isEmpty(), "Attempting to sign tx without outputs.");
		TxInput input = new TxInput(params, this, new byte[] {}, prevOut);
		addInput(input);
		Sha256Hash hash = hashForSignature(inputs.size() - 1, scriptPubKey, sigHash, anyoneCanPay);
		ECKey.ECDSASignature ecSig = sigKey.sign(hash);
		Signature txSig = new Signature(ecSig, sigHash, anyoneCanPay);
		if (scriptPubKey.isSentToRawPubKey())
			input.setScriptSig(ChainScriptBuilder.createInputScript(txSig));
		else if (scriptPubKey.isSentToAddress())
			input.setScriptSig(ChainScriptBuilder.createInputScript(txSig, sigKey));
		else
			throw new ScriptException("Don't know how to sign for this kind of scriptPubKey: " + scriptPubKey);
		return input;
	}

	public TxInput addSignedInput(TxOutPoint prevOut, ChainScript scriptPubKey, ECKey sigKey) throws ScriptException {
		return addSignedInput(prevOut, scriptPubKey, sigKey, SigHash.ALL, false);
	}

	public TxInput addSignedInput(TxOutput output, ECKey signingKey) {
		return addSignedInput(output.getOutPointFor(), output.getScriptPubKey(), signingKey);
	}

	public TxInput addSignedInput(TxOutput output, ECKey signingKey, SigHash sigHash, boolean anyoneCanPay) {
		return addSignedInput(output.getOutPointFor(), output.getScriptPubKey(), signingKey, sigHash, anyoneCanPay);
	}

	public void clearOutputs() {
		unCache();
		for (TxOutput output : outputs) {
			output.setParent(null);
		}
		outputs.clear();

		this.length = this.bitcoinSerialize().length;
	}

	public TxOutput addOutput(TxOutput to) {
		unCache();
		to.setParent(this);
		outputs.add(to);
		adjustLength(outputs.size(), to.length);
		return to;
	}

	public TxOutput addOutput(Coin value, Address address) {
		return addOutput(new TxOutput(params, this, value, address));
	}

	public TxOutput addOutput(Coin value, ECKey pubkey) {
		return addOutput(new TxOutput(params, this, value, pubkey));
	}

	public TxOutput addOutput(Coin value, ChainScript chainScript) {
		return addOutput(new TxOutput(params, this, value, chainScript.getProgram()));
	}

	public synchronized Signature calculateSignature(int inputIndex, ECKey key, byte[] redeemScript, SigHash hashType,
			boolean anyoneCanPay) {
		Sha256Hash hash = hashForSignature(inputIndex, redeemScript, hashType, anyoneCanPay);
		return new Signature(key.sign(hash), hashType, anyoneCanPay);
	}

	public synchronized Signature calculateSignature(int inputIndex, ECKey key, ChainScript redeemScript,
			SigHash hashType, boolean anyoneCanPay) {
		Sha256Hash hash = hashForSignature(inputIndex, redeemScript.getProgram(), hashType, anyoneCanPay);
		return new Signature(key.sign(hash), hashType, anyoneCanPay);
	}

	public synchronized Sha256Hash hashForSignature(int inputIndex, byte[] redeemScript, SigHash type,
			boolean anyoneCanPay) {
		byte sigHashType = (byte) Signature.calcSigHashValue(type, anyoneCanPay);
		return hashForSignature(inputIndex, redeemScript, sigHashType);
	}

	public synchronized Sha256Hash hashForSignature(int inputIndex, ChainScript redeemScript, SigHash type,
			boolean anyoneCanPay) {
		int sigHash = Signature.calcSigHashValue(type, anyoneCanPay);
		return hashForSignature(inputIndex, redeemScript.getProgram(), (byte) sigHash);
	}

	public synchronized Sha256Hash hashForSignature(int inputIndex, byte[] connectedScript, byte sigHashType) {

		try {

			byte[][] inputScripts = new byte[inputs.size()][];
			long[] inputSequenceNumbers = new long[inputs.size()];
			for (int i = 0; i < inputs.size(); i++) {
				inputScripts[i] = inputs.get(i).getScriptBytes();
				inputSequenceNumbers[i] = inputs.get(i).getSequenceNumber();
				inputs.get(i).setScriptBytes(TxInput.EMPTY_ARRAY);
			}

			connectedScript = ChainScript.removeAllInstancesOfOp(connectedScript, ChainScriptCodes.OP_CODESEPARATOR);

			TxInput input = inputs.get(inputIndex);
			input.setScriptBytes(connectedScript);

			ArrayList<TxOutput> outputs = this.outputs;
			if ((sigHashType & 0x1f) == (SigHash.NONE.ordinal() + 1)) {

				this.outputs = new ArrayList<TxOutput>(0);

				for (int i = 0; i < inputs.size(); i++)
					if (i != inputIndex)
						inputs.get(i).setSequenceNumber(0);
			} else if ((sigHashType & 0x1f) == (SigHash.SINGLE.ordinal() + 1)) {

				if (inputIndex >= this.outputs.size()) {

					for (int i = 0; i < inputs.size(); i++) {
						inputs.get(i).setScriptBytes(inputScripts[i]);
						inputs.get(i).setSequenceNumber(inputSequenceNumbers[i]);
					}
					this.outputs = outputs;

					return Sha256Hash.wrap("0100000000000000000000000000000000000000000000000000000000000000");
				}

				this.outputs = new ArrayList<TxOutput>(this.outputs.subList(0, inputIndex + 1));
				for (int i = 0; i < inputIndex; i++)
					this.outputs.set(i, new TxOutput(params, this, Coin.NEGATIVE_SATOSHI, new byte[] {}));

				for (int i = 0; i < inputs.size(); i++)
					if (i != inputIndex)
						inputs.get(i).setSequenceNumber(0);
			}

			ArrayList<TxInput> inputs = this.inputs;
			if ((sigHashType & SIGHASH_ANYONECANPAY_VALUE) == SIGHASH_ANYONECANPAY_VALUE) {

				this.inputs = new ArrayList<TxInput>();
				this.inputs.add(input);
			}

			ByteArrayOutputStream bos = new UnsafeOutput(length == UNKNOWN_LENGTH ? 256 : length + 4);
			bitcoinSerialize(bos);

			uint32ToByteStreamLE(0x000000ff & sigHashType, bos);

			Sha256Hash hash = Sha256Hash.twiceOf(bos.toByteArray());
			bos.close();

			this.inputs = inputs;
			for (int i = 0; i < inputs.size(); i++) {
				inputs.get(i).setScriptBytes(inputScripts[i]);
				inputs.get(i).setSequenceNumber(inputSequenceNumbers[i]);
			}
			this.outputs = outputs;
			return hash;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		uint32ToByteStreamLE(version, stream);
		stream.write(new VariableInt(inputs.size()).encode());
		for (TxInput in : inputs)
			in.bitcoinSerialize(stream);
		stream.write(new VariableInt(outputs.size()).encode());
		for (TxOutput out : outputs)
			out.bitcoinSerialize(stream);
		uint32ToByteStreamLE(lockTime, stream);
	}

	public long getLockTime() {
		maybeParse();
		return lockTime;
	}

	public void setLockTime(long lockTime) {
		unCache();
		boolean seqNumSet = false;
		for (TxInput input : inputs) {
			if (input.getSequenceNumber() != TxInput.NO_SEQUENCE) {
				seqNumSet = true;
				break;
			}
		}
		if (lockTime != 0 && (!seqNumSet || inputs.isEmpty())) {

			log.warn(
					"You are setting the lock time on a transaction but none of the inputs have non-default sequence numbers. This will not do what you expect!");
		}
		this.lockTime = lockTime;
	}

	public long getVersion() {
		maybeParse();
		return version;
	}

	public List<TxInput> getInputs() {
		maybeParse();
		return Collections.unmodifiableList(inputs);
	}

	public List<TxOutput> getOutputs() {
		maybeParse();
		return Collections.unmodifiableList(outputs);
	}

	public List<TxOutput> getWalletOutputs(TransactionBlock transactionBlock) {
		maybeParse();
		List<TxOutput> walletOutputs = new LinkedList<TxOutput>();
		for (TxOutput o : outputs) {
			if (!o.isMineOrWatched(transactionBlock))
				continue;
			walletOutputs.add(o);
		}

		return walletOutputs;
	}

	public void shuffleOutputs() {
		maybeParse();
		Collections.shuffle(outputs);
	}

	public TxInput getInput(long index) {
		maybeParse();
		return inputs.get((int) index);
	}

	public TxOutput getOutput(long index) {
		maybeParse();
		return outputs.get((int) index);
	}

	public TransactionDegree getConfidence() {
		return getConfidence(Context.get());
	}

	public TransactionDegree getConfidence(Context context) {
		return getConfidence(context.getConfidenceTable());
	}

	public TransactionDegree getConfidence(TxDegreeTable table) {
		if (confidence == null)
			confidence = table.getOrCreate(getHash());
		return confidence;
	}

	public boolean hasConfidence() {
		return getConfidence().getConfidenceType() != TransactionDegree.ConfidenceType.UNKNOWN;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Transaction other = (Transaction) o;
		return getHash().equals(other.getHash());
	}

	@Override
	public int hashCode() {
		return getHash().hashCode();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		maybeParse();
		out.defaultWriteObject();
	}

	public int getSigOpCount() throws ScriptException {
		maybeParse();
		int sigOps = 0;
		for (TxInput input : inputs)
			sigOps += ChainScript.getSigOpCount(input.getScriptBytes());
		for (TxOutput output : outputs)
			sigOps += ChainScript.getSigOpCount(output.getScriptBytes());
		return sigOps;
	}

	public void verify() throws VeriException {
		maybeParse();
		if (inputs.size() == 0 || outputs.size() == 0)
			throw new VeriException.EmptyInputsOrOutputs();
		if (this.getMessageSize() > Block.MAX_BLOCK_SIZE)
			throw new VeriException.LargerThanMaxBlockSize();

		Coin valueOut = Coin.ZERO;
		HashSet<TxOutPoint> outpoints = new HashSet<TxOutPoint>();
		for (TxInput input : inputs) {
			if (outpoints.contains(input.getOutpoint()))
				throw new VeriException.DuplicatedOutPoint();
			outpoints.add(input.getOutpoint());
		}
		try {
			for (TxOutput output : outputs) {
				if (output.getValue().signum() < 0)
					throw new VeriException.NegativeValueOutput();
				valueOut = valueOut.add(output.getValue());

				if (valueOut.compareTo(NetworkParams.MAX_MONEY) > 0)
					throw new IllegalArgumentException();
			}
		} catch (IllegalStateException e) {
			throw new VeriException.ExcessiveValue();
		} catch (IllegalArgumentException e) {
			throw new VeriException.ExcessiveValue();
		}

		if (isCoinBase()) {
			if (inputs.get(0).getScriptBytes().length < 2 || inputs.get(0).getScriptBytes().length > 100)
				throw new VeriException.CoinbaseScriptSizeOutOfRange();
		} else {
			for (TxInput input : inputs)
				if (input.isCoinBase())
					throw new VeriException.UnexpectedCoinbaseInput();
		}
	}

	public boolean isTimeLocked() {
		if (getLockTime() == 0)
			return false;
		for (TxInput input : getInputs())
			if (input.hasSequence())
				return true;
		return false;
	}

	public boolean isOptInFullRBF() {
		for (TxInput input : getInputs())
			if (input.isOptInFullRBF())
				return true;
		return false;
	}

	public boolean isFinal(int height, long blockTimeSeconds) {
		long time = getLockTime();
		return time < (time < LOCKTIME_THRESHOLD ? height : blockTimeSeconds) || !isTimeLocked();
	}

	public Date estimateLockTime(AbstractChain chain) {
		if (lockTime < LOCKTIME_THRESHOLD)
			return chain.estimateBlockTime((int) getLockTime());
		else
			return new Date(getLockTime() * 1000);
	}

	public Purpose getPurpose() {
		return purpose;
	}

	public void setPurpose(Purpose purpose) {
		this.purpose = purpose;
	}

	@Nullable
	public InterchangeRate getExchangeRate() {
		return interchangeRate;
	}

	public void setExchangeRate(InterchangeRate interchangeRate) {
		this.interchangeRate = interchangeRate;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
