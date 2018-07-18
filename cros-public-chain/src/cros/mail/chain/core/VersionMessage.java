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

import javax.annotation.Nullable;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public class VersionMessage extends Message {
	private static final long serialVersionUID = 7313594258967483180L;

	public static final int NODE_NETWORK = 1;

	public static final int NODE_GETUTXOS = 2;

	public int clientVersion;

	public long localServices;

	public long time;

	public PeerAddress myAddr;

	public PeerAddress theirAddr;

	public String subVer;

	public long bestHeight;

	public boolean relayTxesBeforeFilter;

	public static final String BITCOINJ_VERSION = "0.13.6";

	public static final String LIBRARY_SUBVER = "/bitcoinj:" + BITCOINJ_VERSION + "/";

	public VersionMessage(NetworkParams params, byte[] payload) throws ProtocolException {
		super(params, payload, 0);
	}

	public VersionMessage(NetworkParams params, int newBestHeight) {
		super(params);
		clientVersion = NetworkParams.PROTOCOL_VERSION;
		localServices = 0;
		time = System.currentTimeMillis() / 1000;

		try {

			final byte[] localhost = { 127, 0, 0, 1 };
			myAddr = new PeerAddress(InetAddress.getByAddress(localhost), params.getPort(), 0);
			theirAddr = new PeerAddress(InetAddress.getByAddress(localhost), params.getPort(), 0);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		subVer = LIBRARY_SUBVER;
		bestHeight = newBestHeight;
		relayTxesBeforeFilter = true;

		length = 85;
		if (protocolVersion > 31402)
			length += 8;
		length += VariableInt.sizeOf(subVer.length()) + subVer.length();
	}

	@Override
	protected void parseLite() throws ProtocolException {

	}

	@Override
	public void parse() throws ProtocolException {
		if (parsed)
			return;
		parsed = true;

		clientVersion = (int) readUint32();
		localServices = readUint64().longValue();
		time = readUint64().longValue();
		myAddr = new PeerAddress(params, payload, cursor, 0);
		cursor += myAddr.getMessageSize();
		theirAddr = new PeerAddress(params, payload, cursor, 0);
		cursor += theirAddr.getMessageSize();

		readUint64();
		try {

			subVer = "";
			bestHeight = 0;
			relayTxesBeforeFilter = true;
			if (!hasMoreBytes())
				return;

			subVer = readStr();
			if (!hasMoreBytes())
				return;

			bestHeight = readUint32();
			if (!hasMoreBytes())
				return;
			relayTxesBeforeFilter = readBytes(1)[0] != 0;
		} finally {
			length = cursor - offset;
		}
	}

	@Override
	public void bitcoinSerializeToStream(OutputStream buf) throws IOException {
		Utils.uint32ToByteStreamLE(clientVersion, buf);
		Utils.uint32ToByteStreamLE(localServices, buf);
		Utils.uint32ToByteStreamLE(localServices >> 32, buf);
		Utils.uint32ToByteStreamLE(time, buf);
		Utils.uint32ToByteStreamLE(time >> 32, buf);
		try {

			myAddr.bitcoinSerialize(buf);

			theirAddr.bitcoinSerialize(buf);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Utils.uint32ToByteStreamLE(0, buf);
		Utils.uint32ToByteStreamLE(0, buf);

		byte[] subVerBytes = subVer.getBytes("UTF-8");
		buf.write(new VariableInt(subVerBytes.length).encode());
		buf.write(subVerBytes);

		Utils.uint32ToByteStreamLE(bestHeight, buf);
		buf.write(relayTxesBeforeFilter ? 1 : 0);
	}

	public boolean hasBlockChain() {
		return (localServices & NODE_NETWORK) == NODE_NETWORK;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VersionMessage other = (VersionMessage) o;
		return other.bestHeight == bestHeight && other.clientVersion == clientVersion
				&& other.localServices == localServices && other.time == time && other.subVer.equals(subVer)
				&& other.myAddr.equals(myAddr) && other.theirAddr.equals(theirAddr)
				&& other.relayTxesBeforeFilter == relayTxesBeforeFilter;
	}

	@Override
	public int hashCode() {
		return (int) bestHeight ^ clientVersion ^ (int) localServices ^ (int) time ^ subVer.hashCode()
				^ myAddr.hashCode() ^ theirAddr.hashCode() * (relayTxesBeforeFilter ? 1 : 2);
	}

	@Override
	byte[] getChecksum() {
		throw new UnsupportedOperationException();
	}

	@Override
	void setChecksum(byte[] checksum) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n");
		stringBuilder.append("client version: ").append(clientVersion).append("\n");
		stringBuilder.append("local services: ").append(localServices).append("\n");
		stringBuilder.append("time:           ").append(time).append("\n");
		stringBuilder.append("my addr:        ").append(myAddr).append("\n");
		stringBuilder.append("their addr:     ").append(theirAddr).append("\n");
		stringBuilder.append("sub version:    ").append(subVer).append("\n");
		stringBuilder.append("best height:    ").append(bestHeight).append("\n");
		stringBuilder.append("delay tx relay: ").append(!relayTxesBeforeFilter).append("\n");
		return stringBuilder.toString();
	}

	public VersionMessage duplicate() {
		VersionMessage v = new VersionMessage(params, (int) bestHeight);
		v.clientVersion = clientVersion;
		v.localServices = localServices;
		v.time = time;
		v.myAddr = myAddr;
		v.theirAddr = theirAddr;
		v.subVer = subVer;
		v.relayTxesBeforeFilter = relayTxesBeforeFilter;
		return v;
	}

	public void appendToSubVer(String name, String version, @Nullable String comments) {
		checkSubVerComponent(name);
		checkSubVerComponent(version);
		if (comments != null) {
			checkSubVerComponent(comments);
			subVer = subVer.concat(String.format(Locale.US, "%s:%s(%s)/", name, version, comments));
		} else {
			subVer = subVer.concat(String.format(Locale.US, "%s:%s/", name, version));
		}
	}

	private static void checkSubVerComponent(String component) {
		if (component.contains("/") || component.contains("(") || component.contains(")"))
			throw new IllegalArgumentException("name contains invalid characters");
	}

	public boolean isPingPongSupported() {
		return clientVersion >= Pong.MIN_PROTOCOL_VERSION;
	}

	public boolean isBloomFilteringSupported() {
		return clientVersion >= LightBlock.MIN_PROTOCOL_VERSION;
	}

	public boolean isGetUTXOsSupported() {
		return clientVersion >= GetUTXOsMessage.MIN_PROTOCOL_VERSION
				&& (localServices & NODE_GETUTXOS) == NODE_GETUTXOS;
	}
}
