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
package cros.mail.chain.wallet;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cros.mail.chain.core.ECKey;
import cros.mail.chain.script.ChainScript;

import static com.google.common.base.Preconditions.checkArgument;

public class RedeemBlockData {
	public final ChainScript redeemScript;
	public final List<ECKey> keys;

	private RedeemBlockData(List<ECKey> keys, ChainScript redeemScript) {
		this.redeemScript = redeemScript;
		List<ECKey> sortedKeys = new ArrayList<ECKey>(keys);
		Collections.sort(sortedKeys, ECKey.PUBKEY_COMPARATOR);
		this.keys = sortedKeys;
	}

	public static RedeemBlockData of(List<ECKey> keys, ChainScript redeemScript) {
		return new RedeemBlockData(keys, redeemScript);
	}

	public static RedeemBlockData of(ECKey key, ChainScript program) {
		checkArgument(program.isSentToAddress() || program.isSentToRawPubKey());
		return key != null ? new RedeemBlockData(Collections.singletonList(key), program) : null;
	}

	public ECKey getFullKey() {
		for (ECKey key : keys)
			if (key.hasPrivKey())
				return key;
		return null;
	}
}
