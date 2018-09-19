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

import cros.mail.chain.core.*;
import cros.mail.chain.encrypt.Child;
import cros.mail.chain.encrypt.Signature;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.wallet.KeyPackage;
import cros.mail.chain.wallet.RedeemBlockData;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CustomTransactionSignature extends StatelessTransactionSignature {
	private static final Logger log = LoggerFactory.getLogger(CustomTransactionSignature.class);

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
			TxOutput txOut = txIn.getConnectedOutput();
			if (txOut == null) {
				continue;
			}
			ChainScript scriptPubKey = txOut.getScriptPubKey();
			if (!scriptPubKey.isPayToScriptHash()) {
				log.warn("CustomTransactionSignature works only with P2SH transactions");
				return false;
			}

			ChainScript inputScript = checkNotNull(txIn.getScriptSig());

			try {

				txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey());
				log.warn(
						"Input {} already correctly spends output, assuming SIGHASH type used will be safe and skipping signing.",
						i);
				continue;
			} catch (ScriptException e) {

			}

			RedeemBlockData redeemBlockData = txIn.getConnectedRedeemData(keyPackage);
			if (redeemBlockData == null) {
				log.warn("No redeem data found for input {}", i);
				continue;
			}

			Sha256Hash sighash = tx.hashForSignature(i, redeemBlockData.redeemScript, Transaction.SigHash.ALL, false);
			SignatureAndKey sigKey = getSignature(sighash, propTx.keyPaths.get(scriptPubKey));
			Signature txSig = new Signature(sigKey.sig, Transaction.SigHash.ALL, false);
			int sigIndex = inputScript.getSigInsertionIndex(sighash, sigKey.pubKey);
			inputScript = scriptPubKey.getScriptSigWithSignature(inputScript, txSig.encodeToBitcoin(), sigIndex);
			txIn.setScriptSig(inputScript);
		}
		return true;
	}

	protected abstract SignatureAndKey getSignature(Sha256Hash sighash, List<Child> derivationPath);

	public class SignatureAndKey {
		public final ECKey.ECDSASignature sig;
		public final ECKey pubKey;

		public SignatureAndKey(ECKey.ECDSASignature sig, ECKey pubKey) {
			this.sig = sig;
			this.pubKey = pubKey;
		}
	}

}
