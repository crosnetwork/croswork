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

import javax.annotation.Nullable;
/**
 * 
 * @author CROS
 *
 */
public abstract class ChildMessage extends Message {
	private static final long serialVersionUID = -7657113383624517931L;

	@Nullable
	protected Message parent;

	protected ChildMessage() {
	}

	public ChildMessage(NetworkParams params) {
		super(params);
	}

	public ChildMessage(NetworkParams params, byte[] payload, int offset, int protocolVersion)
			throws ProtocolException {
		super(params, payload, offset, protocolVersion);
	}

	public ChildMessage(NetworkParams params, byte[] payload, int offset, int protocolVersion, Message parent,
			boolean parseLazy, boolean parseRetain, int length) throws ProtocolException {
		super(params, payload, offset, protocolVersion, parseLazy, parseRetain, length);
		this.parent = parent;
	}

	public ChildMessage(NetworkParams params, byte[] payload, int offset) throws ProtocolException {
		super(params, payload, offset);
	}

	public ChildMessage(NetworkParams params, byte[] payload, int offset, @Nullable Message parent, boolean parseLazy,
			boolean parseRetain, int length) throws ProtocolException {
		super(params, payload, offset, parseLazy, parseRetain, length);
		this.parent = parent;
	}

	public void setParent(@Nullable Message parent) {
		if (this.parent != null && this.parent != parent && parent != null) {

			this.parent.unCache();
		}
		this.parent = parent;
	}

	@Override
	protected void unCache() {
		super.unCache();
		if (parent != null)
			parent.unCache();
	}

	protected void adjustLength(int adjustment) {
		adjustLength(0, adjustment);
	}

	@Override
	protected void adjustLength(int newArraySize, int adjustment) {
		super.adjustLength(newArraySize, adjustment);
		if (parent != null)
			parent.adjustLength(newArraySize, adjustment);
	}

}
