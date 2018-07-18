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

import org.slf4j.*;


import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptBuilder;

import javax.annotation.*;
import java.io.*;
import java.util.*;

import static com.google.common.base.Preconditions.*;

public class TxOutput extends ChildMessage implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(TxOutput.class);
	private static final long serialVersionUID = -590332479859256824L;

	private long value;

	private byte[] scriptBytes;

	private transient ChainScript scriptPubKey;

	private boolean availableForSpending;
	@Nullable
	private TxInput spentBy;

	private transient int scriptLen;

	public TxOutput(NetworkParams params, @Nullable Transaction parent, byte[] payload, int offset)
			throws ProtocolException {
		super(params, payload, offset);
		setParent(parent);
		availableForSpending = true;
	}

	public TxOutput(NetworkParams params, @Nullable Transaction parent, byte[] payload, int offset, boolean parseLazy,
			boolean parseRetain) throws ProtocolException {
		super(params, payload, offset, parent, parseLazy, parseRetain, UNKNOWN_LENGTH);
		availableForSpending = true;
	}

	public TxOutput(NetworkParams params, @Nullable Transaction parent, Coin value, Address to) {
		this(params, parent, value, ChainScriptBuilder.createOutputScript(to).getProgram());
	}

	public TxOutput(NetworkParams params, @Nullable Transaction parent, Coin value, ECKey to) {
		this(params, parent, value, ChainScriptBuilder.createOutputScript(to).getProgram());
	}

	public TxOutput(NetworkParams params, @Nullable Transaction parent, Coin value, byte[] scriptBytes) {
		super(params);

		checkArgument(value.signum() >= 0 || value.equals(Coin.NEGATIVE_SATOSHI), "Negative values not allowed");
		checkArgument(value.compareTo(NetworkParams.MAX_MONEY) <= 0, "Values larger than MAX_MONEY not allowed");
		this.value = value.value;
		this.scriptBytes = scriptBytes;
		setParent(parent);
		availableForSpending = true;
		length = 8 + VariableInt.sizeOf(scriptBytes.length) + scriptBytes.length;
	}

	public ChainScript getScriptPubKey() throws ScriptException {
		if (scriptPubKey == null) {
			maybeParse();
			scriptPubKey = new ChainScript(scriptBytes);
		}
		return scriptPubKey;
	}

	@Nullable
	public Address getAddressFromP2PKHScript(NetworkParams networkParams) throws ScriptException {
		if (getScriptPubKey().isSentToAddress())
			return getScriptPubKey().getToAddress(networkParams);

		return null;
	}

	@Nullable
	public Address getAddressFromP2SH(NetworkParams networkParams) throws ScriptException {
		if (getScriptPubKey().isPayToScriptHash())
			return getScriptPubKey().getToAddress(networkParams);

		return null;
	}

	@Override
	protected void parseLite() throws ProtocolException {
		value = readInt64();
		scriptLen = (int) readVarInt();
		length = cursor - offset + scriptLen;
	}

	@Override
	void parse() throws ProtocolException {
		scriptBytes = readBytes(scriptLen);
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		checkNotNull(scriptBytes);
		maybeParse();
		Utils.int64ToByteStreamLE(value, stream);

		stream.write(new VariableInt(scriptBytes.length).encode());
		stream.write(scriptBytes);
	}

	public Coin getValue() {
		maybeParse();
		try {
			return Coin.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void setValue(Coin value) {
		checkNotNull(value);
		unCache();
		this.value = value.value;
	}

	public int getIndex() {
		List<TxOutput> outputs = getParentTransaction().getOutputs();
		for (int i = 0; i < outputs.size(); i++) {
			if (outputs.get(i) == this)
				return i;
		}
		throw new IllegalStateException("Output linked to wrong parent transaction?");
	}

	public Coin getMinNonDustValue(Coin feePerKbRequired) {

		final long size = this.bitcoinSerialize().length + 148;
		Coin[] nonDustAndRemainder = feePerKbRequired.multiply(size).divideAndRemainder(1000);
		return nonDustAndRemainder[1].equals(Coin.ZERO) ? nonDustAndRemainder[0]
				: nonDustAndRemainder[0].add(Coin.SATOSHI);
	}

	public Coin getMinNonDustValue() {
		return getMinNonDustValue(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.multiply(3));
	}

	public void markAsSpent(TxInput input) {
		checkState(availableForSpending);
		availableForSpending = false;
		spentBy = input;
		if (parent != null)
			if (log.isDebugEnabled())
				log.debug("Marked {}:{} as spent by {}", getParentTransactionHash(), getIndex(), input);
			else if (log.isDebugEnabled())
				log.debug("Marked floating output as spent by {}", input);
	}

	public void markAsUnspent() {
		if (parent != null)
			if (log.isDebugEnabled())
				log.debug("Un-marked {}:{} as spent by {}", getParentTransactionHash(), getIndex(), spentBy);
			else if (log.isDebugEnabled())
				log.debug("Un-marked floating output as spent by {}", spentBy);
		availableForSpending = true;
		spentBy = null;
	}

	public boolean isAvailableForSpending() {
		return availableForSpending;
	}

	public byte[] getScriptBytes() {
		maybeParse();
		return scriptBytes;
	}

	public boolean isMineOrWatched(TransactionBlock transactionBlock) {
		return isMine(transactionBlock) || isWatched(transactionBlock);
	}

	public boolean isWatched(TransactionBlock transactionBlock) {
		try {
			ChainScript chainScript = getScriptPubKey();
			return transactionBlock.isWatchedScript(chainScript);
		} catch (ScriptException e) {

			log.debug("Could not parse tx output script: {}", e.toString());
			return false;
		}
	}

	public boolean isMine(TransactionBlock transactionBlock) {
		try {
			ChainScript chainScript = getScriptPubKey();
			if (chainScript.isSentToRawPubKey()) {
				byte[] pubkey = chainScript.getPubKey();
				return transactionBlock.isPubKeyMine(pubkey);
			}
			if (chainScript.isPayToScriptHash()) {
				return transactionBlock.isPayToScriptHashMine(chainScript.getPubKeyHash());
			} else {
				byte[] pubkeyHash = chainScript.getPubKeyHash();
				return transactionBlock.isPubKeyHashMine(pubkeyHash);
			}
		} catch (ScriptException e) {

			log.debug("Could not parse tx output script: {}", e.toString());
			return false;
		}
	}

	@Override
	public String toString() {
		try {
			ChainScript chainScript = getScriptPubKey();
			StringBuilder buf = new StringBuilder("TxOut of ");
			buf.append(Coin.valueOf(value).toFriendlyString());
			if (chainScript.isSentToAddress() || chainScript.isPayToScriptHash())
				buf.append(" to ").append(chainScript.getToAddress(params));
			else if (chainScript.isSentToRawPubKey())
				buf.append(" to pubkey ").append(Utils.HEX.encode(chainScript.getPubKey()));
			else if (chainScript.isSentToMultiSig())
				buf.append(" to multisig");
			else
				buf.append(" (unknown type)");
			buf.append(" script:").append(chainScript);
			return buf.toString();
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public TxInput getSpentBy() {
		return spentBy;
	}

	@Nullable
	public Transaction getParentTransaction() {
		return (Transaction) parent;
	}

	@Nullable
	public Sha256Hash getParentTransactionHash() {
		return parent == null ? null : parent.getHash();
	}

	public int getParentTransactionDepthInBlocks() {
		if (getParentTransaction() != null) {
			TransactionDegree confidence = getParentTransaction().getConfidence();
			if (confidence.getConfidenceType() == TransactionDegree.ConfidenceType.BUILDING) {
				return confidence.getDepthInBlocks();
			}
		}
		return -1;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		maybeParse();
		out.defaultWriteObject();
	}

	public TxOutPoint getOutPointFor() {
		return new TxOutPoint(params, getIndex(), getParentTransaction());
	}

	public TxOutput duplicateDetached() {
		return new TxOutput(params, null, Coin.valueOf(value), org.spongycastle.util.Arrays.clone(scriptBytes));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TxOutput other = (TxOutput) o;

		if (!Arrays.equals(scriptBytes, other.scriptBytes))
			return false;
		if (value != other.value)
			return false;
		if (parent != null && parent != other.parent)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (value ^ (value >>> 32));
		result = 31 * result + Arrays.hashCode(scriptBytes);
		if (parent != null)
			result *= parent.getHash().hashCode() + getIndex();
		return result;
	}
}
