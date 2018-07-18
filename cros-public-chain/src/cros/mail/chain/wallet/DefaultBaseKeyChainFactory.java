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

import cros.mail.chain.blockdata.InvalidWalletException;
import cros.mail.chain.encrypt.*;

public class DefaultBaseKeyChainFactory implements CrosKeyChainFactory {
	@Override
	public DeterKeyChain makeKeyChain(Protos.Key key, Protos.Key firstSubKey, DeterSeed seed,
			KeyCrypt crypter, boolean isMarried) {
		DeterKeyChain chain;
		if (isMarried)
			chain = new CoupledKeyChain(seed, crypter);
		else
			chain = new DeterKeyChain(seed, crypter);
		return chain;
	}

	@Override
	public DeterKeyChain makeWatchingKeyChain(Protos.Key key, Protos.Key firstSubKey, DeterKey accountKey,
			boolean isFollowingKey, boolean isMarried) throws InvalidWalletException {
		if (!accountKey.getPath().equals(DeterKeyChain.ACCOUNT_ZERO_PATH))
			throw new InvalidWalletException(
					"Expecting account key but found key with path: " + HDUtils.formatPath(accountKey.getPath()));
		DeterKeyChain chain;
		if (isMarried)
			chain = new CoupledKeyChain(accountKey);
		else
			chain = new DeterKeyChain(accountKey, isFollowingKey);
		return chain;
	}
}
