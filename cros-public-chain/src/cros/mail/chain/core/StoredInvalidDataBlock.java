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

import java.io.Serializable;
import java.util.List;

public class StoredInvalidDataBlock implements Serializable {
	private static final long serialVersionUID = 5127353027086786117L;

	Sha256Hash blockHash;

	private TxOutputChanges txOutChanges;
	private List<Transaction> transactions;

	public StoredInvalidDataBlock(Sha256Hash hash, TxOutputChanges txOutChanges) {
		this.blockHash = hash;
		this.transactions = null;
		this.txOutChanges = txOutChanges;
	}

	public StoredInvalidDataBlock(Sha256Hash hash, List<Transaction> transactions) {
		this.blockHash = hash;
		this.txOutChanges = null;
		this.transactions = transactions;
	}

	public TxOutputChanges getTxOutChanges() {
		return txOutChanges;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public Sha256Hash getHash() {
		return blockHash;
	}

	@Override
	public int hashCode() {
		return blockHash.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		StoredInvalidDataBlock other = (StoredInvalidDataBlock) o;
		return getHash().equals(other.getHash());
	}

	@Override
	public String toString() {
		return "Undoable Block " + blockHash;
	}
}
