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
import java.util.List;
/**
 * 
 * @author CROS
 *
 */
public class GetBlocksMessage extends Message {
	private static final long serialVersionUID = 3479412877853645644L;
	protected long version;
	protected List<Sha256Hash> locator;
	protected Sha256Hash stopHash;

	public GetBlocksMessage(NetworkParams params, List<Sha256Hash> locator, Sha256Hash stopHash) {
		super(params);
		this.version = protocolVersion;
		this.locator = locator;
		this.stopHash = stopHash;
	}

	public GetBlocksMessage(NetworkParams params, byte[] payload) throws ProtocolException {
		super(params, payload, 0);
	}

	@Override
	protected void parseLite() throws ProtocolException {
		cursor = offset;
		version = readUint32();
		int startCount = (int) readVarInt();
		if (startCount > 500)
			throw new ProtocolException("Number of locators cannot be > 500, received: " + startCount);
		length = cursor - offset + ((startCount + 1) * 32);
	}

	@Override
	public void parse() throws ProtocolException {
		cursor = offset;
		version = readUint32();
		int startCount = (int) readVarInt();
		if (startCount > 500)
			throw new ProtocolException("Number of locators cannot be > 500, received: " + startCount);
		locator = new ArrayList<Sha256Hash>(startCount);
		for (int i = 0; i < startCount; i++) {
			locator.add(readHash());
		}
		stopHash = readHash();
	}

	public List<Sha256Hash> getLocator() {
		return locator;
	}

	public Sha256Hash getStopHash() {
		return stopHash;
	}

	@Override
	public String toString() {
		return "getblocks: " + Utils.join(locator);
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {

		Utils.uint32ToByteStreamLE(NetworkParams.PROTOCOL_VERSION, stream);

		stream.write(new VariableInt(locator.size()).encode());
		for (Sha256Hash hash : locator) {

			stream.write(hash.getReversedBytes());
		}

		stream.write(stopHash.getReversedBytes());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		GetBlocksMessage other = (GetBlocksMessage) o;
		return version == other.version && locator.size() == other.locator.size() && locator.containsAll(other.locator)
				&& stopHash.equals(other.stopHash);
	}

	@Override
	public int hashCode() {
		int hashCode = (int) version ^ "getblocks".hashCode();
		for (Sha256Hash aLocator : locator)
			hashCode ^= aLocator.hashCode();
		hashCode ^= stopHash.hashCode();
		return hashCode;
	}
}
