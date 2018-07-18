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

import static com.google.common.base.Preconditions.checkNotNull;

import cros.mail.chain.core.Transaction;


public class CrosWalletTransaction {
	public enum Pool {
		UNSPENT, SPENT, DEAD, PENDING,
	}

	private final Transaction transaction;
	private final Pool pool;

	public CrosWalletTransaction(Pool pool, Transaction transaction) {
		this.pool = checkNotNull(pool);
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public Pool getPool() {
		return pool;
	}
}
