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

import cros.mail.chain.script.ChainScript;
import cros.mail.chain.wallet.CrosKeyChainListener;

import java.util.List;

public interface WalletListener extends CrosKeyChainListener {

	void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);

	void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);

	void onReorganize(Wallet wallet);

	void onTransactionConfidenceChanged(Wallet wallet, Transaction tx);

	void onWalletChanged(Wallet wallet);

	void onScriptsChanged(Wallet wallet, List<ChainScript> chainScripts, boolean isAddingScripts);
}
