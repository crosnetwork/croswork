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

import cros.mail.chain.core.Coin;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Transaction;
import cros.mail.chain.core.TransactionDegree;
import cros.mail.chain.core.TxOutput;
import cros.mail.chain.param.CrosTestParam;
import com.google.common.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.util.*;

public class DefaultKeySelector implements TokenSelector {
	@Override
	public TokenSelection select(Coin target, List<TxOutput> candidates) {
		ArrayList<TxOutput> selected = new ArrayList<TxOutput>();

		ArrayList<TxOutput> sortedOutputs = new ArrayList<TxOutput>(candidates);

		if (!target.equals(NetworkParams.MAX_MONEY)) {
			sortOutputs(sortedOutputs);
		}

		long total = 0;
		for (TxOutput output : sortedOutputs) {
			if (total >= target.value)
				break;

			if (!shouldSelect(output.getParentTransaction()))
				continue;
			selected.add(output);
			total += output.getValue().value;
		}

		return new TokenSelection(Coin.valueOf(total), selected);
	}

	@VisibleForTesting
	static void sortOutputs(ArrayList<TxOutput> outputs) {
		Collections.sort(outputs, new Comparator<TxOutput>() {
			@Override
			public int compare(TxOutput a, TxOutput b) {
				int depth1 = a.getParentTransactionDepthInBlocks();
				int depth2 = b.getParentTransactionDepthInBlocks();
				Coin aValue = a.getValue();
				Coin bValue = b.getValue();
				BigInteger aCoinDepth = BigInteger.valueOf(aValue.value).multiply(BigInteger.valueOf(depth1));
				BigInteger bCoinDepth = BigInteger.valueOf(bValue.value).multiply(BigInteger.valueOf(depth2));
				int c1 = bCoinDepth.compareTo(aCoinDepth);
				if (c1 != 0)
					return c1;

				int c2 = bValue.compareTo(aValue);
				if (c2 != 0)
					return c2;

				BigInteger aHash = a.getParentTransactionHash().toBigInteger();
				BigInteger bHash = b.getParentTransactionHash().toBigInteger();
				return aHash.compareTo(bHash);
			}
		});
	}

	protected boolean shouldSelect(Transaction tx) {
		if (tx != null) {
			return isSelectable(tx);
		}
		return true;
	}

	public static boolean isSelectable(Transaction tx) {

		TransactionDegree confidence = tx.getConfidence();
		TransactionDegree.ConfidenceType type = confidence.getConfidenceType();
		return type.equals(TransactionDegree.ConfidenceType.BUILDING) ||

				type.equals(TransactionDegree.ConfidenceType.PENDING)
						&& confidence.getSource().equals(TransactionDegree.Source.SELF) &&

						(confidence.numBroadcastPeers() > 1 || tx.getParams().getId().equals(NetworkParams.ID_REGTEST));
	}
}
