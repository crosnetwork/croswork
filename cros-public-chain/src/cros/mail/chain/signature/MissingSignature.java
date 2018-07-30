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
import cros.mail.chain.core.TxInput;
import cros.mail.chain.core.Wallet;
import cros.mail.chain.encrypt.Signature;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptChunk;
import cros.mail.chain.wallet.KeyPackage;

public class MissingSignature extends StatelessTransactionSignature {
	private static final Logger log = LoggerFactory.getLogger(MissingSignature.class);

	public Wallet.MissingSigsMode missingSigsMode = Wallet.MissingSigsMode.USE_DUMMY_SIG;

	public MissingSignature() {
	}

	public MissingSignature(Wallet.MissingSigsMode missingSigsMode) {
		this.missingSigsMode = missingSigsMode;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public boolean signInputs(ProposedTransaction propTx, KeyPackage keyPackage) {
		if (missingSigsMode == Wallet.MissingSigsMode.USE_OP_ZERO)
			return true;

		int numInputs = propTx.partialTx.getInputs().size();
		byte[] dummySig = Signature.dummy().encodeToBitcoin();
		for (int i = 0; i < numInputs; i++) {
			TxInput txIn = propTx.partialTx.getInput(i);
			if (txIn.getConnectedOutput() == null) {
				log.warn("Missing connected output, assuming input {} is already signed.", i);
				continue;
			}

			ChainScript scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
			ChainScript inputScript = txIn.getScriptSig();
			if (scriptPubKey.isPayToScriptHash() || scriptPubKey.isSentToMultiSig()) {
				int sigSuffixCount = scriptPubKey.isPayToScriptHash() ? 1 : 0;

				for (int j = 1; j < inputScript.getChunks().size() - sigSuffixCount; j++) {
					ChainScriptChunk chainScriptChunk = inputScript.getChunks().get(j);
					if (chainScriptChunk.equalsOpCode(0)) {
						if (missingSigsMode == Wallet.MissingSigsMode.THROW) {
							throw new MissingSignatureException();
						} else if (missingSigsMode == Wallet.MissingSigsMode.USE_DUMMY_SIG) {
							txIn.setScriptSig(scriptPubKey.getScriptSigWithSignature(inputScript, dummySig, j - 1));
						}
					}
				}
			} else {
				if (inputScript.getChunks().get(0).equalsOpCode(0)) {
					if (missingSigsMode == Wallet.MissingSigsMode.THROW) {
						throw new ECKey.MissingPrivateKeyException();
					} else if (missingSigsMode == Wallet.MissingSigsMode.USE_DUMMY_SIG) {
						txIn.setScriptSig(scriptPubKey.getScriptSigWithSignature(inputScript, dummySig, 0));
					}
				}
			}

		}
		return true;
	}
}
