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

import java.util.List;

public class GetHeaderMessage extends GetBlocksMessage {
	public GetHeaderMessage(NetworkParams params, List<Sha256Hash> locator, Sha256Hash stopHash) {
		super(params, locator, stopHash);
	}

	public GetHeaderMessage(NetworkParams params, byte[] payload) throws ProtocolException {
		super(params, payload);
	}

	@Override
	public String toString() {
		return "getheaders: " + Utils.join(locator);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		GetHeaderMessage other = (GetHeaderMessage) o;
		return version == other.version && locator.size() == other.locator.size() && locator.containsAll(other.locator)
				&& stopHash.equals(other.stopHash);
	}

	@Override
	public int hashCode() {
		int hashCode = (int) version ^ "getheaders".hashCode();
		for (Sha256Hash aLocator : locator)
			hashCode ^= aLocator.hashCode();
		hashCode ^= stopHash.hashCode();
		return hashCode;
	}
}
