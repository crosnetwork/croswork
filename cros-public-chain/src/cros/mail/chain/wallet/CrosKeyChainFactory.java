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
import cros.mail.chain.encrypt.DeterKey;
import cros.mail.chain.encrypt.KeyCrypt;

public interface CrosKeyChainFactory {

	DeterKeyChain makeKeyChain(Protos.Key key, Protos.Key firstSubKey, DeterSeed seed, KeyCrypt crypter,
			boolean isMarried);

	DeterKeyChain makeWatchingKeyChain(Protos.Key key, Protos.Key firstSubKey, DeterKey accountKey,
			boolean isFollowingKey, boolean isMarried) throws InvalidWalletException;
}
