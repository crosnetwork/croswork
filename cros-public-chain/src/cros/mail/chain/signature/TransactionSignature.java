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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cros.mail.chain.core.Transaction;
import cros.mail.chain.encrypt.Child;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.wallet.KeyPackage;

public interface TransactionSignature {

	class ProposedTransaction {

		public final Transaction partialTx;

		public final Map<ChainScript, List<Child>> keyPaths;

		public ProposedTransaction(Transaction partialTx) {
			this.partialTx = partialTx;
			this.keyPaths = new HashMap<ChainScript, List<Child>>();
		}
	}

	class MissingSignatureException extends RuntimeException {
	}

	boolean isReady();

	byte[] serialize();

	void deserialize(byte[] data);

	boolean signInputs(ProposedTransaction propTx, KeyPackage keyPackage);

}
