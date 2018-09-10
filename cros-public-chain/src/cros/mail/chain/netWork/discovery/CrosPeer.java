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
package cros.mail.chain.netWork.discovery;

import javax.annotation.Nullable;

import cros.mail.chain.core.NetworkParams;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class CrosPeer implements NetworkPeerDiscovery {
	private NetworkParams params;
	private int[] seedAddrs;
	private int pnseedIndex;

	public CrosPeer(NetworkParams params) {
		this(params.getAddrSeeds(), params);
	}

	public CrosPeer(int[] seedAddrs, NetworkParams params) {
		this.seedAddrs = seedAddrs;
		this.params = params;
	}

	@Nullable
	public InetSocketAddress getPeer() throws NetworkDiscoveryException {
		try {
			return nextPeer();
		} catch (UnknownHostException e) {
			throw new NetworkDiscoveryException(e);
		}
	}

	@Nullable
	private InetSocketAddress nextPeer() throws UnknownHostException, NetworkDiscoveryException {
		if (seedAddrs == null || seedAddrs.length == 0)
			throw new NetworkDiscoveryException("No IP address seeds configured; unable to find any peers");

		if (pnseedIndex >= seedAddrs.length)
			return null;
		return new InetSocketAddress(convertAddress(seedAddrs[pnseedIndex++]), params.getPort());
	}

	@Override
	public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws NetworkDiscoveryException {
		try {
			return allPeers();
		} catch (UnknownHostException e) {
			throw new NetworkDiscoveryException(e);
		}
	}

	private InetSocketAddress[] allPeers() throws UnknownHostException {
		InetSocketAddress[] addresses = new InetSocketAddress[seedAddrs.length];
		for (int i = 0; i < seedAddrs.length; ++i) {
			addresses[i] = new InetSocketAddress(convertAddress(seedAddrs[i]), params.getPort());
		}
		return addresses;
	}

	private InetAddress convertAddress(int seed) throws UnknownHostException {
		byte[] v4addr = new byte[4];
		v4addr[0] = (byte) (0xFF & (seed));
		v4addr[1] = (byte) (0xFF & (seed >> 8));
		v4addr[2] = (byte) (0xFF & (seed >> 16));
		v4addr[3] = (byte) (0xFF & (seed >> 24));
		return InetAddress.getByAddress(v4addr);
	}

	@Override
	public void shutdown() {
	}
}
