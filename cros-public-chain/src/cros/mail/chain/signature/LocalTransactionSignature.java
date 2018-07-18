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
package cros.mail.chain.signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.ScriptException;
import cros.mail.chain.core.Transaction;
import cros.mail.chain.core.TxInput;
import cros.mail.chain.encrypt.DeterKey;
import cros.mail.chain.encrypt.Signature;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.wallet.KeyPackage;
import cros.mail.chain.wallet.RedeemBlockData;

public class LocalTransactionSignature extends StatelessTransactionSignature {
	private static final Logger log = LoggerFactory.getLogger(LocalTransactionSignature.class);

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public boolean signInputs(ProposedTransaction propTx, KeyPackage keyPackage) {
		Transaction tx = propTx.partialTx;
		int numInputs = tx.getInputs().size();
		for (int i = 0; i < numInputs; i++) {
			TxInput txIn = tx.getInput(i);
			if (txIn.getConnectedOutput() == null) {
				log.warn("Missing connected output, assuming input {} is already signed.", i);
				continue;
			}

			try {

				txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey());
				log.warn(
						"Input {} already correctly spends output, assuming SIGHASH type used will be safe and skipping signing.",
						i);
				continue;
			} catch (ScriptException e) {

			}

			RedeemBlockData redeemBlockData = txIn.getConnectedRedeemData(keyPackage);

			ChainScript scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();

			ECKey pubKey = redeemBlockData.keys.get(0);
			if (pubKey instanceof DeterKey)
				propTx.keyPaths.put(scriptPubKey, (((DeterKey) pubKey).getPath()));

			ECKey key;

			if ((key = redeemBlockData.getFullKey()) == null) {
				log.warn("No local key found for input {}", i);
				continue;
			}

			ChainScript inputScript = txIn.getScriptSig();

			byte[] script = redeemBlockData.redeemScript.getProgram();
			try {
				Signature signature = tx.calculateSignature(i, key, script, Transaction.SigHash.ALL, false);

				int sigIndex = 0;
				inputScript = scriptPubKey.getScriptSigWithSignature(inputScript, signature.encodeToBitcoin(),
						sigIndex);
				txIn.setScriptSig(inputScript);
			} catch (ECKey.KeyIsEncryptedException e) {
				throw e;
			} catch (ECKey.MissingPrivateKeyException e) {
				log.warn("No private key in keypair for input {}", i);
			}

		}
		return true;
	}

}
