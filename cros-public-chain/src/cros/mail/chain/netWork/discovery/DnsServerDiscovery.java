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

import cros.mail.chain.core.*;
import cros.mail.chain.misc.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DnsServerDiscovery extends MultipleDiscovery {

	public DnsServerDiscovery(NetworkParams netParams) {
		this(netParams.getDnsSeeds(), netParams);
	}

	public DnsServerDiscovery(String[] dnsSeeds, NetworkParams params) {
		super(params, buildDiscoveries(params, dnsSeeds));
	}

	private static List<NetworkPeerDiscovery> buildDiscoveries(NetworkParams params, String[] seeds) {
		List<NetworkPeerDiscovery> discoveries = new ArrayList<NetworkPeerDiscovery>();
		if (seeds != null)
			for (String seed : seeds)
				discoveries.add(new DnsSeedDiscovery(params, seed));
		return discoveries;
	}

	@Override
	protected ExecutorService createExecutor() {

		if (System.getProperty("os.name").toLowerCase().contains("linux"))
			return Executors.newSingleThreadExecutor(new ContextThreadFactory("DNS seed lookups"));
		else
			return Executors.newFixedThreadPool(seeds.size(), new BackgroundThreadFactory("DNS seed lookups"));
	}

	public static class DnsSeedDiscovery implements NetworkPeerDiscovery {
		private final String hostname;
		private final NetworkParams params;

		public DnsSeedDiscovery(NetworkParams params, String hostname) {
			this.hostname = hostname;
			this.params = params;
		}

		@Override
		public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws NetworkDiscoveryException {
			try {
				InetAddress[] response = InetAddress.getAllByName(hostname);
				InetSocketAddress[] result = new InetSocketAddress[response.length];
				for (int i = 0; i < response.length; i++)
					result[i] = new InetSocketAddress(response[i], params.getPort());
				return result;
			} catch (UnknownHostException e) {
				throw new NetworkDiscoveryException(e);
			}
		}

		@Override
		public void shutdown() {
		}

		@Override
		public String toString() {
			return hostname;
		}
	}
}
