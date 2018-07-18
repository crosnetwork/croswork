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

import com.google.common.net.InetAddresses;

import cros.mail.chain.param.CrosMainNetParam;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static cros.mail.chain.core.Utils.uint32ToByteStreamLE;
import static cros.mail.chain.core.Utils.uint64ToByteStreamLE;
import static com.google.common.base.Preconditions.checkNotNull;

public class PeerAddress extends ChildMessage {
	private static final long serialVersionUID = 7501293709324197411L;
	static final int MESSAGE_SIZE = 30;

	private InetAddress addr;
	private int port;
	private BigInteger services;
	private long time;

	public PeerAddress(NetworkParams params, byte[] payload, int offset, int protocolVersion) throws ProtocolException {
		super(params, payload, offset, protocolVersion);
	}

	public PeerAddress(NetworkParams params, byte[] payload, int offset, int protocolVersion, Message parent,
			boolean parseLazy, boolean parseRetain) throws ProtocolException {
		super(params, payload, offset, protocolVersion, parent, parseLazy, parseRetain, UNKNOWN_LENGTH);

	}

	public PeerAddress(InetAddress addr, int port, int protocolVersion) {
		this.addr = checkNotNull(addr);
		this.port = port;
		this.protocolVersion = protocolVersion;
		this.services = BigInteger.ZERO;
		length = protocolVersion > 31402 ? MESSAGE_SIZE : MESSAGE_SIZE - 4;
	}

	public PeerAddress(InetAddress addr, int port) {
		this(addr, port, NetworkParams.PROTOCOL_VERSION);
	}

	public PeerAddress(InetAddress addr) {
		this(addr, CrosMainNetParam.get().getPort());
	}

	public PeerAddress(InetSocketAddress addr) {
		this(addr.getAddress(), addr.getPort());
	}

	public static PeerAddress localhost(NetworkParams params) {
		return new PeerAddress(InetAddresses.forString("127.0.0.1"), params.getPort());
	}

	@Override
	protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		if (protocolVersion >= 31402) {

			int secs = (int) (Utils.currentTimeSeconds());
			uint32ToByteStreamLE(secs, stream);
		}
		uint64ToByteStreamLE(services, stream);

		byte[] ipBytes = addr.getAddress();
		if (ipBytes.length == 4) {
			byte[] v6addr = new byte[16];
			System.arraycopy(ipBytes, 0, v6addr, 12, 4);
			v6addr[10] = (byte) 0xFF;
			v6addr[11] = (byte) 0xFF;
			ipBytes = v6addr;
		}
		stream.write(ipBytes);

		stream.write((byte) (0xFF & port >> 8));
		stream.write((byte) (0xFF & port));
	}

	@Override
	protected void parseLite() {
		length = protocolVersion > 31402 ? MESSAGE_SIZE : MESSAGE_SIZE - 4;
	}

	@Override
	protected void parse() throws ProtocolException {

		if (protocolVersion > 31402)
			time = readUint32();
		else
			time = -1;
		services = readUint64();
		byte[] addrBytes = readBytes(16);
		try {
			addr = InetAddress.getByAddress(addrBytes);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		port = ((0xFF & payload[cursor++]) << 8) | (0xFF & payload[cursor++]);
	}

	@Override
	public int getMessageSize() {

		length = protocolVersion > 31402 ? MESSAGE_SIZE : MESSAGE_SIZE - 4;
		return length;
	}

	public InetAddress getAddr() {
		maybeParse();
		return addr;
	}

	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(getAddr(), getPort());
	}

	public void setAddr(InetAddress addr) {
		unCache();
		this.addr = addr;
	}

	public int getPort() {
		maybeParse();
		return port;
	}

	public void setPort(int port) {
		unCache();
		this.port = port;
	}

	public BigInteger getServices() {
		maybeParse();
		return services;
	}

	public void setServices(BigInteger services) {
		unCache();
		this.services = services;
	}

	public long getTime() {
		maybeParse();
		return time;
	}

	public void setTime(long time) {
		unCache();
		this.time = time;
	}

	@Override
	public String toString() {
		return "[" + addr.getHostAddress() + "]:" + port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PeerAddress other = (PeerAddress) o;
		return other.addr.equals(addr) && other.port == port && other.services.equals(services) && other.time == time;

	}

	@Override
	public int hashCode() {
		return addr.hashCode() ^ port ^ (int) time ^ services.hashCode();
	}

	public InetSocketAddress toSocketAddress() {
		return new InetSocketAddress(addr, port);
	}
}
