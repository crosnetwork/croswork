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

import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Sha256Hash;
import cros.mail.chain.core.StoredDataBlock;

public interface BlockData {

	void put(StoredDataBlock block) throws BlockDataException;

	StoredDataBlock get(Sha256Hash hash) throws BlockDataException;

	StoredDataBlock getChainHead() throws BlockDataException;

	void setChainHead(StoredDataBlock chainHead) throws BlockDataException;

	void close() throws BlockDataException;

	NetworkParams getParams();
}
