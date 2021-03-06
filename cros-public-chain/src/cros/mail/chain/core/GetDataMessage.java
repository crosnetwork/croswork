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
/**
 * 
 * @author CROS
 *
 */
public class GetDataMessage extends ListMessage {
	private static final long serialVersionUID = 2754681589501709887L;

	public GetDataMessage(NetworkParams params, byte[] payloadBytes) throws ProtocolException {
		super(params, payloadBytes);
	}

	public GetDataMessage(NetworkParams params, byte[] payload, boolean parseLazy, boolean parseRetain, int length)
			throws ProtocolException {
		super(params, payload, parseLazy, parseRetain, length);
	}

	public GetDataMessage(NetworkParams params) {
		super(params);
	}

	public void addTransaction(Sha256Hash hash) {
		addItem(new InventoryItem(InventoryItem.Type.Transaction, hash));
	}

	public void addBlock(Sha256Hash hash) {
		addItem(new InventoryItem(InventoryItem.Type.Block, hash));
	}

	public void addFilteredBlock(Sha256Hash hash) {
		addItem(new InventoryItem(InventoryItem.Type.FilteredBlock, hash));
	}

	public Sha256Hash getHashOf(int i) {
		return getItems().get(i).hash;
	}
}
