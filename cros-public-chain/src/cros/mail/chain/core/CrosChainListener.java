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

import java.util.List;
/**
 * 
 * @author CROS
 *
 */
public interface CrosChainListener {

	void notifyNewBestBlock(StoredDataBlock block) throws VeriException;

	void reorganize(StoredDataBlock splitPoint, List<StoredDataBlock> oldBlocks, List<StoredDataBlock> newBlocks)
			throws VeriException;

	boolean isTransactionRelevant(Transaction tx) throws ScriptException;

	void receiveFromBlock(Transaction tx, StoredDataBlock block, CrosChain.NewBlockType blockType, int relativityOffset)
			throws VeriException;

	boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredDataBlock block, CrosChain.NewBlockType blockType,
			int relativityOffset) throws VeriException;
}
