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


import javax.annotation.*;

import cros.mail.chain.script.ChainScript;
import cros.mail.chain.wallet.KeyPackage;
import cros.mail.chain.wallet.RedeemBlockData;

import java.io.*;

import static com.google.common.base.Preconditions.*;

public class TxOutPoint extends ChildMessage implements Serializable {
	private static final long serialVersionUID = -6320880638344662579L;

	static final int MESSAGE_LENGTH = 36;

	private Sha256Hash hash;

	private long index;

	Transaction fromTx;

	private TxOutput connectedOutput;

	public TxOutPoint(NetworkParams params, long index, @Nullable Transaction fromTx) {
		super(params);
		this.index = index;
		if (fromTx != null) {
			this.hash = fromTx.getHash();
			this.fromTx = fromTx;
		} else {

			hash = Sha256Hash.ZERO_HASH;
		}
		length = MESSAGE_LENGTH;
	}

	public TxOutPoint(NetworkParams params, long index, Sha256Hash hash) {
		super(params);
		this.index = index;
		this.hash = hash;
		length = MESSAGE_LENGTH;
	}

	public TxOutPoint(NetworkParams params, TxOutput connectedOutput) {
		this(params, connectedOutput.getIndex(), connectedOutput.getParentTransactionHash());
		this.connectedOutput = connectedOutput;
	}

	public TxOutPoint(NetworkParams params, byte[] payload, int offset) throws ProtocolException {
		super(params, payload, offset);
	}

	public TxOutPoint(NetworkParams params, byte[] payload, int offset, Message parent, boolean parseLazy,
			boolean parseRetain) throws ProtocolException {
		super(params, payload, offset, parent, parseLazy, parseRetain, MESSAGE_LENGTH);
	}

	@Override
	protected void parseLite() throws ProtocolException {
		length = MESSAGE_LENGTH;
	}

	@Override
	void parse() throws ProtocolException {
		hash = readHash();
		index = readUint32();
	}

	@Override
	public int getMessageSize() {
		return MESSAGE_LENGTH;
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		stream.write(hash.getReversedBytes());
		Utils.uint32ToByteStreamLE(index, stream);
	}

	@Nullable
	public TxOutput getConnectedOutput() {
		if (fromTx != null) {
			return fromTx.getOutputs().get((int) index);
		} else if (connectedOutput != null) {
			return connectedOutput;
		}
		return null;
	}

	public byte[] getConnectedPubKeyScript() {
		byte[] result = checkNotNull(getConnectedOutput()).getScriptBytes();
		checkState(result.length > 0);
		return result;
	}

	@Nullable
	public ECKey getConnectedKey(KeyPackage keyPackage) throws ScriptException {
		TxOutput connectedOutput = getConnectedOutput();
		checkNotNull(connectedOutput, "Input is not connected so cannot retrieve key");
		ChainScript connectedScript = connectedOutput.getScriptPubKey();
		if (connectedScript.isSentToAddress()) {
			byte[] addressBytes = connectedScript.getPubKeyHash();
			return keyPackage.findKeyFromPubHash(addressBytes);
		} else if (connectedScript.isSentToRawPubKey()) {
			byte[] pubkeyBytes = connectedScript.getPubKey();
			return keyPackage.findKeyFromPubKey(pubkeyBytes);
		} else {
			throw new ScriptException("Could not understand form of connected output script: " + connectedScript);
		}
	}

	@Nullable
	public RedeemBlockData getConnectedRedeemData(KeyPackage keyPackage) throws ScriptException {
		TxOutput connectedOutput = getConnectedOutput();
		checkNotNull(connectedOutput, "Input is not connected so cannot retrieve key");
		ChainScript connectedScript = connectedOutput.getScriptPubKey();
		if (connectedScript.isSentToAddress()) {
			byte[] addressBytes = connectedScript.getPubKeyHash();
			return RedeemBlockData.of(keyPackage.findKeyFromPubHash(addressBytes), connectedScript);
		} else if (connectedScript.isSentToRawPubKey()) {
			byte[] pubkeyBytes = connectedScript.getPubKey();
			return RedeemBlockData.of(keyPackage.findKeyFromPubKey(pubkeyBytes), connectedScript);
		} else if (connectedScript.isPayToScriptHash()) {
			byte[] scriptHash = connectedScript.getPubKeyHash();
			return keyPackage.findRedeemDataFromScriptHash(scriptHash);
		} else {
			throw new ScriptException("Could not understand form of connected output script: " + connectedScript);
		}
	}

	@Override
	public String toString() {
		return hash + ":" + index;
	}

	@Override
	public Sha256Hash getHash() {
		maybeParse();
		return hash;
	}

	void setHash(Sha256Hash hash) {
		this.hash = hash;
	}

	public long getIndex() {
		maybeParse();
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		maybeParse();
		out.defaultWriteObject();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		TxOutPoint other = (TxOutPoint) o;
		return getIndex() == other.getIndex() && getHash().equals(other.getHash());
	}

	@Override
	public int hashCode() {
		return 31 * hash.hashCode() + (int) (index ^ (index >>> 32));
	}
}
