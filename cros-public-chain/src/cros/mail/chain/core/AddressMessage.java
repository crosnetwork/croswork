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
import java.util.Collections;
import java.util.List;
/**
 * 
 * @author CROS
 *
 */
public class AddressMessage extends Message {
	private static final long serialVersionUID = 8058283864924679460L;
	private static final long MAX_ADDRESSES = 1024;
	private List<PeerAddress> addresses;

	AddressMessage(NetworkParams params, byte[] payload, int offset, boolean parseLazy, boolean parseRetain, int length)
			throws ProtocolException {
		super(params, payload, offset, parseLazy, parseRetain, length);
	}

	AddressMessage(NetworkParams params, byte[] payload, boolean parseLazy, boolean parseRetain, int length)
			throws ProtocolException {
		super(params, payload, 0, parseLazy, parseRetain, length);
	}

	AddressMessage(NetworkParams params, byte[] payload, int offset) throws ProtocolException {
		super(params, payload, offset, false, false, UNKNOWN_LENGTH);
	}

	AddressMessage(NetworkParams params, byte[] payload) throws ProtocolException {
		super(params, payload, 0, false, false, UNKNOWN_LENGTH);
	}

	@Override
	protected void parseLite() throws ProtocolException {
	}

	@Override
	void parse() throws ProtocolException {
		long numAddresses = readVarInt();

		if (numAddresses > MAX_ADDRESSES)
			throw new ProtocolException("Address message too large.");
		addresses = new ArrayList<PeerAddress>((int) numAddresses);
		for (int i = 0; i < numAddresses; i++) {
			PeerAddress addr = new PeerAddress(params, payload, cursor, protocolVersion, this, parseLazy, parseRetain);
			addresses.add(addr);
			cursor += addr.getMessageSize();
		}
		length = cursor - offset;
	}

	@Override
	void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		if (addresses == null)
			return;
		stream.write(new VariableInt(addresses.size()).encode());
		for (PeerAddress addr : addresses) {
			addr.bitcoinSerialize(stream);
		}
	}

	@Override
	public int getMessageSize() {
		if (length != UNKNOWN_LENGTH)
			return length;
		if (addresses != null) {
			length = new VariableInt(addresses.size()).getSizeInBytes();

			length += addresses.size()
					* (protocolVersion > 31402 ? PeerAddress.MESSAGE_SIZE : PeerAddress.MESSAGE_SIZE - 4);
		}
		return length;
	}

	@Override
	void setChecksum(byte[] checksum) {
		if (parseRetain)
			super.setChecksum(checksum);
		else
			this.checksum = null;
	}

	public List<PeerAddress> getAddresses() {
		maybeParse();
		return Collections.unmodifiableList(addresses);
	}

	public void addAddress(PeerAddress address) {
		unCache();
		maybeParse();
		address.setParent(this);
		addresses.add(address);
		if (length == UNKNOWN_LENGTH)
			getMessageSize();
		else
			length += address.getMessageSize();
	}

	public void removeAddress(int index) {
		unCache();
		PeerAddress address = addresses.remove(index);
		address.setParent(null);
		if (length == UNKNOWN_LENGTH)
			getMessageSize();
		else
			length -= address.getMessageSize();
	}

	@Override
	public String toString() {
		return "addr: " + Utils.join(addresses);
	}

}
