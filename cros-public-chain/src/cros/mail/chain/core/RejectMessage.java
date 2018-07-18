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
import java.util.Locale;

public class RejectMessage extends Message {
	private static final long serialVersionUID = -5246995579800334336L;

	private String message, reason;

	public enum RejectCode {

		MALFORMED((byte) 0x01),

		INVALID((byte) 0x10),

		OBSOLETE((byte) 0x11),

		DUPLICATE((byte) 0x12),

		NONSTANDARD((byte) 0x40),

		DUST((byte) 0x41),

		INSUFFICIENTFEE((byte) 0x42),

		CHECKPOINT((byte) 0x43), OTHER((byte) 0xff);

		byte code;

		RejectCode(byte code) {
			this.code = code;
		}

		static RejectCode fromCode(byte code) {
			for (RejectCode rejectCode : RejectCode.values())
				if (rejectCode.code == code)
					return rejectCode;
			return OTHER;
		}
	}

	private RejectCode code;
	private Sha256Hash messageHash;

	public RejectMessage(NetworkParams params, byte[] payload) throws ProtocolException {
		super(params, payload, 0);
	}

	public RejectMessage(NetworkParams params, RejectCode code, Sha256Hash hash, String message, String reason)
			throws ProtocolException {
		super(params);
		this.code = code;
		this.messageHash = hash;
		this.message = message;
		this.reason = reason;
	}

	@Override
	protected void parseLite() throws ProtocolException {
		message = readStr();
		code = RejectCode.fromCode(readBytes(1)[0]);
		reason = readStr();
		if (message.equals("block") || message.equals("tx"))
			messageHash = readHash();
		length = cursor - offset;
	}

	@Override
	public void parse() throws ProtocolException {
		if (length == UNKNOWN_LENGTH)
			parseLite();
	}

	@Override
	public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		byte[] messageBytes = message.getBytes("UTF-8");
		stream.write(new VariableInt(messageBytes.length).encode());
		stream.write(messageBytes);
		stream.write(code.code);
		byte[] reasonBytes = reason.getBytes("UTF-8");
		stream.write(new VariableInt(reasonBytes.length).encode());
		stream.write(reasonBytes);
		if (message.equals("block") || message.equals("tx"))
			stream.write(messageHash.getReversedBytes());
	}

	public String getRejectedMessage() {
		ensureParsed();
		return message;
	}

	public Sha256Hash getRejectedObjectHash() {
		ensureParsed();
		return messageHash;
	}

	public RejectCode getReasonCode() {
		return code;
	}

	public String getReasonString() {
		return reason;
	}

	@Override
	public String toString() {
		Sha256Hash hash = getRejectedObjectHash();
		return String.format(Locale.US, "Reject: %s %s for reason '%s' (%d)", getRejectedMessage(),
				hash != null ? hash : "", getReasonString(), getReasonCode().code);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RejectMessage other = (RejectMessage) o;
		return message.equals(other.message) && code.equals(other.code) && reason.equals(other.reason)
				&& messageHash.equals(other.messageHash);
	}

	@Override
	public int hashCode() {
		int result = message.hashCode();
		result = 31 * result + reason.hashCode();
		result = 31 * result + code.hashCode();
		result = 31 * result + messageHash.hashCode();
		return result;
	}
}
