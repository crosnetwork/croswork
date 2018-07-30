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
package cros.mail.chain.blockdata;

import cros.mail.chain.core.*;

public interface FullPrunedBlockData extends BlockData, UnspentProvider {

	void put(StoredDataBlock storedDataBlock, StoredInvalidDataBlock undoableBlock) throws BlockDataException;

	StoredDataBlock getOnceUndoableStoredBlock(Sha256Hash hash) throws BlockDataException;

	StoredInvalidDataBlock getUndoBlock(Sha256Hash hash) throws BlockDataException;

	Unspent getTransactionOutput(Sha256Hash hash, long index) throws BlockDataException;

	void addUnspentTransactionOutput(Unspent out) throws BlockDataException;

	void removeUnspentTransactionOutput(Unspent out) throws BlockDataException;

	boolean hasUnspentOutputs(Sha256Hash hash, int numOutputs) throws BlockDataException;

	StoredDataBlock getVerifiedChainHead() throws BlockDataException;

	void setVerifiedChainHead(StoredDataBlock chainHead) throws BlockDataException;

	void beginDatabaseBatchWrite() throws BlockDataException;

	void commitDatabaseBatchWrite() throws BlockDataException;

	void abortDatabaseBatchWrite() throws BlockDataException;
}
