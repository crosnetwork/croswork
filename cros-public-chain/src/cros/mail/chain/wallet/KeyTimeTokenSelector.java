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

import cros.mail.chain.core.*;
import cros.mail.chain.script.ChainScript;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class KeyTimeTokenSelector implements TokenSelector {
	private static final Logger log = LoggerFactory.getLogger(KeyTimeTokenSelector.class);

	public static final int MAX_SIMULTANEOUS_INPUTS = 600;

	private final long unixTimeSeconds;
	private final Wallet wallet;
	private final boolean ignorePending;

	public KeyTimeTokenSelector(Wallet wallet, long unixTimeSeconds, boolean ignorePending) {
		this.unixTimeSeconds = unixTimeSeconds;
		this.wallet = wallet;
		this.ignorePending = ignorePending;
	}

	@Override
	public TokenSelection select(Coin target, List<TxOutput> candidates) {
		try {
			LinkedList<TxOutput> gathered = Lists.newLinkedList();
			Coin valueGathered = Coin.ZERO;
			for (TxOutput output : candidates) {
				if (ignorePending && !isConfirmed(output))
					continue;

				final ChainScript scriptPubKey = output.getScriptPubKey();
				ECKey controllingKey;
				if (scriptPubKey.isSentToRawPubKey()) {
					controllingKey = wallet.findKeyFromPubKey(scriptPubKey.getPubKey());
				} else if (scriptPubKey.isSentToAddress()) {
					controllingKey = wallet.findKeyFromPubHash(scriptPubKey.getPubKeyHash());
				} else {
					log.info("Skipping tx output {} because it's not of simple form.", output);
					continue;
				}
				checkNotNull(controllingKey, "Coin selector given output as candidate for which we lack the key");
				if (controllingKey.getCreationTimeSeconds() >= unixTimeSeconds)
					continue;

				valueGathered = valueGathered.add(output.getValue());
				gathered.push(output);
				if (gathered.size() >= MAX_SIMULTANEOUS_INPUTS) {
					log.warn("Reached {} inputs, going further would yield a tx that is too large, stopping here.",
							gathered.size());
					break;
				}
			}
			return new TokenSelection(valueGathered, gathered);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isConfirmed(TxOutput output) {
		return output.getParentTransaction().getConfidence().getConfidenceType()
				.equals(TransactionDegree.ConfidenceType.BUILDING);
	}
}
