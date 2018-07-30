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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import cros.mail.chain.core.*;

public class FilterTokenSelector implements TokenSelector {
	protected TokenSelector delegate;
	protected HashSet<TxOutPoint> spent = new HashSet<TxOutPoint>();

	public FilterTokenSelector(TokenSelector delegate) {
		this.delegate = delegate;
	}

	public void excludeOutputsSpentBy(Transaction tx) {
		for (TxInput input : tx.getInputs()) {
			spent.add(input.getOutpoint());
		}
	}

	@Override
	public TokenSelection select(Coin target, List<TxOutput> candidates) {
		Iterator<TxOutput> iter = candidates.iterator();
		while (iter.hasNext()) {
			TxOutput output = iter.next();
			if (spent.contains(output.getOutPointFor()))
				iter.remove();
		}
		return delegate.select(target, candidates);
	}
}
