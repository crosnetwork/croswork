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

import cros.mail.chain.script.ChainScript;
import cros.mail.chain.wallet.DefaultRiskAssess;
import cros.mail.chain.wallet.KeyPackage;
import cros.mail.chain.wallet.RedeemBlockData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

public class TxInput extends ChildMessage implements Serializable {

	public static final long NO_SEQUENCE = 0xFFFFFFFFL;
	private static final long serialVersionUID = 2;
	public static final byte[] EMPTY_ARRAY = new byte[0];

	private static final long UNCONNECTED = 0xFFFFFFFFL;

	private long sequence;

	private TxOutPoint outpoint;

	private byte[] scriptBytes;

	private transient WeakReference<ChainScript> scriptSig;

	@Nullable
	private Coin value;

	public TxInput(NetworkParams params, @Nullable Transaction parentTransaction, byte[] scriptBytes) {
		this(params, parentTransaction, scriptBytes, new TxOutPoint(params, UNCONNECTED, (Transaction) null));
	}

	public TxInput(NetworkParams params, @Nullable Transaction parentTransaction, byte[] scriptBytes,
			TxOutPoint outpoint) {
		this(params, parentTransaction, scriptBytes, outpoint, null);
	}

	public TxInput(NetworkParams params, @Nullable Transaction parentTransaction, byte[] scriptBytes,
			TxOutPoint outpoint, @Nullable Coin value) {
		super(params);
		this.scriptBytes = scriptBytes;
		this.outpoint = outpoint;
		this.sequence = NO_SEQUENCE;
		this.value = value;
		setParent(parentTransaction);
		length = 40 + (scriptBytes == null ? 1 : VariableInt.sizeOf(scriptBytes.length) + scriptBytes.length);
	}

	TxInput(NetworkParams params, Transaction parentTransaction, TxOutput output) {
		super(params);
		long outputIndex = output.getIndex();
		if (output.getParentTransaction() != null) {
			outpoint = new TxOutPoint(params, outputIndex, output.getParentTransaction());
		} else {
			outpoint = new TxOutPoint(params, output);
		}
		scriptBytes = EMPTY_ARRAY;
		sequence = NO_SEQUENCE;
		setParent(parentTransaction);
		this.value = output.getValue();
		length = 41;
	}

	public TxInput(NetworkParams params, @Nullable Transaction parentTransaction, byte[] payload, int offset)
			throws ProtocolException {
		super(params, payload, offset);
		setParent(parentTransaction);
		this.value = null;
	}

	public TxInput(NetworkParams params, Transaction parentTransaction, byte[] payload, int offset, boolean parseLazy,
			boolean parseRetain) throws ProtocolException {
		super(params, payload, offset, parentTransaction, parseLazy, parseRetain, UNKNOWN_LENGTH);
		this.value = null;
	}

	@Override
	protected void parseLite() throws ProtocolException {
		int curs = cursor;
		int scriptLen = (int) readVarInt(36);
		length = cursor - offset + scriptLen + 4;
		cursor = curs;
	}

	@Override
	void parse() throws ProtocolException {
		outpoint = new TxOutPoint(params, payload, cursor, this, parseLazy, parseRetain);
		cursor += outpoint.getMessageSize();
		int scriptLen = (int) readVarInt();
		scriptBytes = readBytes(scriptLen);
		sequence = readUint32();
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		outpoint.bitcoinSerialize(stream);
		stream.write(new VariableInt(scriptBytes.length).encode());
		stream.write(scriptBytes);
		Utils.uint32ToByteStreamLE(sequence, stream);
	}

	public boolean isCoinBase() {
		maybeParse();
		return outpoint.getHash().equals(Sha256Hash.ZERO_HASH) && (outpoint.getIndex() & 0xFFFFFFFFL) == 0xFFFFFFFFL;
	}

	public ChainScript getScriptSig() throws ScriptException {

		ChainScript chainScript = scriptSig == null ? null : scriptSig.get();
		if (chainScript == null) {
			maybeParse();
			chainScript = new ChainScript(scriptBytes);
			scriptSig = new WeakReference<ChainScript>(chainScript);
		}
		return chainScript;
	}

	public void setScriptSig(ChainScript scriptSig) {
		this.scriptSig = new WeakReference<ChainScript>(checkNotNull(scriptSig));

		setScriptBytes(scriptSig.getProgram());
	}

	@Deprecated
	public Address getFromAddress() throws ScriptException {
		if (isCoinBase()) {
			throw new ScriptException(
					"This is a coinbase transaction which generates new coins. It does not have a from address.");
		}
		return getScriptSig().getFromAddress(params);
	}

	public long getSequenceNumber() {
		maybeParse();
		return sequence;
	}

	public void setSequenceNumber(long sequence) {
		unCache();
		this.sequence = sequence;
	}

	public TxOutPoint getOutpoint() {
		maybeParse();
		return outpoint;
	}

	public byte[] getScriptBytes() {
		maybeParse();
		return scriptBytes;
	}

	void setScriptBytes(byte[] scriptBytes) {
		unCache();
		this.scriptSig = null;
		int oldLength = length;
		this.scriptBytes = scriptBytes;

		int newLength = 40 + (scriptBytes == null ? 1 : VariableInt.sizeOf(scriptBytes.length) + scriptBytes.length);
		adjustLength(newLength - oldLength);
	}

	public Transaction getParentTransaction() {
		return (Transaction) parent;
	}

	@Nullable
	public Coin getValue() {
		return value;
	}

	public enum ConnectionResult {
		NO_SUCH_TX, ALREADY_SPENT, SUCCESS
	}

	@Nullable
	TxOutput getConnectedOutput(Map<Sha256Hash, Transaction> transactions) {
		Transaction tx = transactions.get(outpoint.getHash());
		if (tx == null)
			return null;
		return tx.getOutputs().get((int) outpoint.getIndex());
	}

	@Nullable
	public RedeemBlockData getConnectedRedeemData(KeyPackage keyPackage) throws ScriptException {
		return getOutpoint().getConnectedRedeemData(keyPackage);
	}

	public enum ConnectMode {
		DISCONNECT_ON_CONFLICT, ABORT_ON_CONFLICT
	}

	public ConnectionResult connect(Map<Sha256Hash, Transaction> transactions, ConnectMode mode) {
		Transaction tx = transactions.get(outpoint.getHash());
		if (tx == null) {
			return TxInput.ConnectionResult.NO_SUCH_TX;
		}
		return connect(tx, mode);
	}

	public ConnectionResult connect(Transaction transaction, ConnectMode mode) {
		if (!transaction.getHash().equals(outpoint.getHash()))
			return ConnectionResult.NO_SUCH_TX;
		checkElementIndex((int) outpoint.getIndex(), transaction.getOutputs().size(), "Corrupt transaction");
		TxOutput out = transaction.getOutput((int) outpoint.getIndex());
		if (!out.isAvailableForSpending()) {
			if (getParentTransaction().equals(outpoint.fromTx)) {

				return ConnectionResult.SUCCESS;
			} else if (mode == ConnectMode.DISCONNECT_ON_CONFLICT) {
				out.markAsUnspent();
			} else if (mode == ConnectMode.ABORT_ON_CONFLICT) {
				outpoint.fromTx = out.getParentTransaction();
				return TxInput.ConnectionResult.ALREADY_SPENT;
			}
		}
		connect(out);
		return TxInput.ConnectionResult.SUCCESS;
	}

	public void connect(TxOutput out) {
		outpoint.fromTx = out.getParentTransaction();
		out.markAsSpent(this);
		value = out.getValue();
	}

	public boolean disconnect() {
		if (outpoint.fromTx == null)
			return false;
		TxOutput output = outpoint.fromTx.getOutput((int) outpoint.getIndex());
		if (output.getSpentBy() == this) {
			output.markAsUnspent();
			outpoint.fromTx = null;
			return true;
		} else {
			return false;
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		maybeParse();
		out.defaultWriteObject();
	}

	public boolean hasSequence() {
		return sequence != NO_SEQUENCE;
	}

	public boolean isOptInFullRBF() {
		return sequence < NO_SEQUENCE - 1;
	}

	public void verify() throws VeriException {
		final Transaction fromTx = getOutpoint().fromTx;
		long spendingIndex = getOutpoint().getIndex();
		checkNotNull(fromTx, "Not connected");
		final TxOutput output = fromTx.getOutput((int) spendingIndex);
		verify(output);
	}

	public void verify(TxOutput output) throws VeriException {
		if (output.parent != null) {
			if (!getOutpoint().getHash().equals(output.getParentTransaction().getHash()))
				throw new VeriException("This input does not refer to the tx containing the output.");
			if (getOutpoint().getIndex() != output.getIndex())
				throw new VeriException("This input refers to a different output on the given tx.");
		}
		ChainScript pubKey = output.getScriptPubKey();
		int myIndex = getParentTransaction().getInputs().indexOf(this);
		getScriptSig().correctlySpends(getParentTransaction(), myIndex, pubKey);
	}

	@Nullable
	public TxOutput getConnectedOutput() {
		return getOutpoint().getConnectedOutput();
	}

	public TxInput duplicateDetached() {
		return new TxInput(params, null, bitcoinSerialize(), 0);
	}

	public DefaultRiskAssess.RuleViolation isStandard() {
		return DefaultRiskAssess.isInputStandard(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TxInput input = (TxInput) o;

		if (sequence != input.sequence)
			return false;
		if (!outpoint.equals(input.outpoint))
			return false;
		if (!Arrays.equals(scriptBytes, input.scriptBytes))
			return false;
		if (scriptSig != null ? !scriptSig.equals(input.scriptSig) : input.scriptSig != null)
			return false;
		if (parent != input.parent)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (sequence ^ (sequence >>> 32));
		result = 31 * result + outpoint.hashCode();
		result = 31 * result + (scriptBytes != null ? Arrays.hashCode(scriptBytes) : 0);
		result = 31 * result + (scriptSig != null ? scriptSig.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("TxIn");
		try {
			if (isCoinBase()) {
				s.append(": COINBASE");
			} else {
				s.append(" for [").append(outpoint).append("]: ").append(getScriptSig());
				String flags = Joiner.on(", ").skipNulls().join(hasSequence() ? "has sequence" : null,
						isOptInFullRBF() ? "opts into full RBF" : null);
				if (!flags.isEmpty())
					s.append(" (").append(flags).append(')');
			}
			return s.toString();
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}
}
