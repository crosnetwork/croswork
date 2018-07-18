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
package cros.mail.chain.param;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import cros.mail.chain.core.NetworkParams;

import java.util.Collection;
import java.util.Set;

public class CrosNetworks {

	private static Set<? extends NetworkParams> networks = ImmutableSet.of(CrosTestNet3Param.get(),
			CrosMainNetParam.get());

	public static Set<? extends NetworkParams> get() {
		return networks;
	}

	public static void register(NetworkParams network) {
		register(Lists.newArrayList(network));
	}

	public static void register(Collection<? extends NetworkParams> networks) {
		ImmutableSet.Builder<NetworkParams> builder = ImmutableSet.builder();
		builder.addAll(CrosNetworks.networks);
		builder.addAll(networks);
		CrosNetworks.networks = builder.build();
	}

	public static void unregister(NetworkParams network) {
		if (networks.contains(network)) {
			ImmutableSet.Builder<NetworkParams> builder = ImmutableSet.builder();
			for (NetworkParams parameters : networks) {
				if (parameters.equals(network))
					continue;
				builder.add(parameters);
			}
			networks = builder.build();
		}
	}
}
