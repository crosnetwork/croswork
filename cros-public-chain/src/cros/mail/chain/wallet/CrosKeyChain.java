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

import java.util.List;
import java.util.concurrent.Executor;

import cros.mail.chain.core.BloomFilter;
import cros.mail.chain.core.ECKey;

public interface CrosKeyChain {

	boolean hasKey(ECKey key);

	enum KeyPurpose {
		RECEIVE_FUNDS, CHANGE, REFUND, AUTHENTICATION
	}

	List<? extends ECKey> getKeys(KeyPurpose purpose, int numberOfKeys);

	ECKey getKey(KeyPurpose purpose);

	List<Protos.Key> serializeToProtobuf();

	void addEventListener(CrosKeyChainListener listener);

	void addEventListener(CrosKeyChainListener listener, Executor executor);

	boolean removeEventListener(CrosKeyChainListener listener);

	int numKeys();

	int numBloomFilterEntries();

	long getEarliestKeyCreationTime();

	BloomFilter getFilter(int size, double falsePositiveRate, long tweak);
}
