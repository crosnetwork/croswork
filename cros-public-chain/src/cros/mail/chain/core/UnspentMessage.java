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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnspentMessage extends Message {
	private long height;
	private Sha256Hash chainHead;
	private byte[] hits;

	private List<TxOutput> outputs;
	private long[] heights;

	public static long MEMPOOL_HEIGHT = 0x7FFFFFFFL;

	public UnspentMessage(NetworkParams params, byte[] payloadBytes) {
		super(params, payloadBytes, 0);
	}

	public UnspentMessage(NetworkParams params, List<TxOutput> outputs, long[] heights, Sha256Hash chainHead,
			long height) {
		super(params);
		hits = new byte[(int) Math.ceil(outputs.size() / 8.0)];
		for (int i = 0; i < outputs.size(); i++) {
			if (outputs.get(i) != null)
				Utils.setBitLE(hits, i);
		}
		this.outputs = new ArrayList<TxOutput>(outputs.size());
		for (TxOutput output : outputs) {
			if (output != null)
				this.outputs.add(output);
		}
		this.chainHead = chainHead;
		this.height = height;
		this.heights = Arrays.copyOf(heights, heights.length);
	}

	@Override
	void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		Utils.uint32ToByteStreamLE(height, stream);
		stream.write(chainHead.getBytes());
		stream.write(new VariableInt(hits.length).encode());
		stream.write(hits);
		stream.write(new VariableInt(outputs.size()).encode());
		for (int i = 0; i < outputs.size(); i++) {
			TxOutput output = outputs.get(i);
			Transaction tx = output.getParentTransaction();
			Utils.uint32ToByteStreamLE(tx != null ? tx.getVersion() : 0L, stream);
			Utils.uint32ToByteStreamLE(heights[i], stream);
			output.bitcoinSerializeToStream(stream);
		}
	}

	@Override
	void parse() throws ProtocolException {

		height = readUint32();
		chainHead = readHash();
		int numBytes = (int) readVarInt();
		if (numBytes < 0 || numBytes > InventoryMessage.MAX_INVENTORY_ITEMS / 8)
			throw new ProtocolException("hitsBitmap out of range: " + numBytes);
		hits = readBytes(numBytes);
		int numOuts = (int) readVarInt();
		if (numOuts < 0 || numOuts > InventoryMessage.MAX_INVENTORY_ITEMS)
			throw new ProtocolException("numOuts out of range: " + numOuts);
		outputs = new ArrayList<TxOutput>(numOuts);
		heights = new long[numOuts];
		for (int i = 0; i < numOuts; i++) {
			long version = readUint32();
			long height = readUint32();
			if (version > 1)
				throw new ProtocolException("Unknown tx version in getutxo output: " + version);
			TxOutput output = new TxOutput(params, null, payload, cursor);
			outputs.add(output);
			heights[i] = height;
			cursor += output.length;
		}
		length = cursor;
	}

	@Override
	protected void parseLite() throws ProtocolException {

	}

	public byte[] getHitMap() {
		return Arrays.copyOf(hits, hits.length);
	}

	public List<TxOutput> getOutputs() {
		return new ArrayList<TxOutput>(outputs);
	}

	public long[] getHeights() {
		return Arrays.copyOf(heights, heights.length);
	}

	@Override
	public String toString() {
		return "UnspentMessage{" + "height=" + height + ", chainHead=" + chainHead + ", hitMap=" + Arrays.toString(hits)
				+ ", outputs=" + outputs + ", heights=" + Arrays.toString(heights) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UnspentMessage message = (UnspentMessage) o;

		if (height != message.height)
			return false;
		if (!chainHead.equals(message.chainHead))
			return false;
		if (!Arrays.equals(heights, message.heights))
			return false;
		if (!Arrays.equals(hits, message.hits))
			return false;
		if (!outputs.equals(message.outputs))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (height ^ (height >>> 32));
		result = 31 * result + chainHead.hashCode();
		result = 31 * result + Arrays.hashCode(hits);
		result = 31 * result + outputs.hashCode();
		result = 31 * result + Arrays.hashCode(heights);
		return result;
	}
}
