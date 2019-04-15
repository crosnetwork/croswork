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

import static com.google.common.base.Preconditions.checkArgument;
/**
 * 
 * @author CROS
 *
 */
public class InventoryMessage extends ListMessage {
	private static final long serialVersionUID = -7050246551646107066L;

	public static final int MAX_INV_SIZE = 50000;

	public InventoryMessage(NetworkParams params, byte[] bytes) throws ProtocolException {
		super(params, bytes);
	}

	public InventoryMessage(NetworkParams params, byte[] payload, boolean parseLazy, boolean parseRetain, int length)
			throws ProtocolException {
		super(params, payload, parseLazy, parseRetain, length);
	}

	public InventoryMessage(NetworkParams params) {
		super(params);
	}

	public void addBlock(Block block) {
		addItem(new InventoryItem(InventoryItem.Type.Block, block.getHash()));
	}

	public void addTransaction(Transaction tx) {
		addItem(new InventoryItem(InventoryItem.Type.Transaction, tx.getHash()));
	}

	public static InventoryMessage with(Transaction... txns) {
		checkArgument(txns.length > 0);
		InventoryMessage result = new InventoryMessage(txns[0].getParams());
		for (Transaction tx : txns)
			result.addTransaction(tx);
		return result;
	}
}
