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
import java.io.OutputStream;
/**
 * 
 * @author CROS
 *
 */
public abstract class EmptyMessage extends Message {
	private static final long serialVersionUID = 8240801253854151802L;

	public EmptyMessage() {
		length = 0;
	}

	public EmptyMessage(NetworkParams params) {
		super(params);
		length = 0;
	}

	public EmptyMessage(NetworkParams params, byte[] payload, int offset) throws ProtocolException {
		super(params, payload, offset);
		length = 0;
	}

	@Override
	protected final void bitcoinSerializeToStream(OutputStream stream) throws IOException {
	}

	@Override
	public int getMessageSize() {
		return 0;
	}

	@Override
	void parse() throws ProtocolException {
	}

	@Override
	protected void parseLite() throws ProtocolException {
		length = 0;
	}

	@Override
	public void ensureParsed() throws ProtocolException {
		parsed = true;
	}

	@Override
	public byte[] bitcoinSerialize() {
		return new byte[0];
	}

}
