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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class TxOutputChanges {
	public final List<Unspent> txOutsCreated;
	public final List<Unspent> txOutsSpent;

	public TxOutputChanges(List<Unspent> txOutsCreated, List<Unspent> txOutsSpent) {
		this.txOutsCreated = txOutsCreated;
		this.txOutsSpent = txOutsSpent;
	}

	public TxOutputChanges(InputStream in) throws IOException {
		int numOutsCreated = (in.read() & 0xFF) | ((in.read() & 0xFF) << 8) | ((in.read() & 0xFF) << 16)
				| ((in.read() & 0xFF) << 24);
		txOutsCreated = new LinkedList<Unspent>();
		for (int i = 0; i < numOutsCreated; i++)
			txOutsCreated.add(new Unspent(in));

		int numOutsSpent = (in.read() & 0xFF) | ((in.read() & 0xFF) << 8) | ((in.read() & 0xFF) << 16)
				| ((in.read() & 0xFF) << 24);
		txOutsSpent = new LinkedList<Unspent>();
		for (int i = 0; i < numOutsSpent; i++)
			txOutsSpent.add(new Unspent(in));
	}

	public void serializeToStream(OutputStream bos) throws IOException {
		int numOutsCreated = txOutsCreated.size();
		bos.write(0xFF & numOutsCreated);
		bos.write(0xFF & (numOutsCreated >> 8));
		bos.write(0xFF & (numOutsCreated >> 16));
		bos.write(0xFF & (numOutsCreated >> 24));
		for (Unspent output : txOutsCreated) {
			output.serializeToStream(bos);
		}

		int numOutsSpent = txOutsSpent.size();
		bos.write(0xFF & numOutsSpent);
		bos.write(0xFF & (numOutsSpent >> 8));
		bos.write(0xFF & (numOutsSpent >> 16));
		bos.write(0xFF & (numOutsSpent >> 24));
		for (Unspent output : txOutsSpent) {
			output.serializeToStream(bos);
		}
	}
}
