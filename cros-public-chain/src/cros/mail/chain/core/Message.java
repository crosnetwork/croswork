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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

public abstract class Message implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(Message.class);
	private static final long serialVersionUID = -3561053461717079135L;

	public static final int MAX_SIZE = 0x02000000;

	public static final int UNKNOWN_LENGTH = Integer.MIN_VALUE;

	private static final boolean SELF_CHECK = false;

	protected transient int offset;

	protected transient int cursor;

	protected transient int length = UNKNOWN_LENGTH;

	protected transient byte[] payload;

	protected transient boolean parsed = false;
	protected transient boolean recached = false;
	protected final transient boolean parseLazy;
	protected final transient boolean parseRetain;

	protected transient int protocolVersion;

	protected transient byte[] checksum;

	protected NetworkParams params;

	protected Message() {
		parsed = true;
		parseLazy = false;
		parseRetain = false;
	}

	Message(NetworkParams params) {
		this.params = params;
		parsed = true;
		parseLazy = false;
		parseRetain = false;
	}

	Message(NetworkParams params, byte[] payload, int offset, int protocolVersion) throws ProtocolException {
		this(params, payload, offset, protocolVersion, false, false, UNKNOWN_LENGTH);
	}

	Message(NetworkParams params, byte[] payload, int offset, int protocolVersion, boolean parseLazy,
			boolean parseRetain, int length) throws ProtocolException {
		this.parseLazy = parseLazy;
		this.parseRetain = parseRetain;
		this.protocolVersion = protocolVersion;
		this.params = params;
		this.payload = payload;
		this.cursor = this.offset = offset;
		this.length = length;
		if (parseLazy) {
			parseLite();
		} else {
			parseLite();
			parse();
			parsed = true;
		}

		if (this.length == UNKNOWN_LENGTH)
			checkState(false,
					"Length field has not been set in constructor for %s after %s parse. "
							+ "Refer to Message.parseLite() for detail of required Length field contract.",
					getClass().getSimpleName(), parseLazy ? "lite" : "full");

		if (SELF_CHECK) {
			selfCheck(payload, offset);
		}

		if (parseRetain || !parsed)
			return;
		this.payload = null;
	}

	private void selfCheck(byte[] payload, int offset) {
		if (!(this instanceof VersionMessage)) {
			maybeParse();
			byte[] payloadBytes = new byte[cursor - offset];
			System.arraycopy(payload, offset, payloadBytes, 0, cursor - offset);
			byte[] reserialized = bitcoinSerialize();
			if (!Arrays.equals(reserialized, payloadBytes))
				throw new RuntimeException("Serialization is wrong: \n" + Utils.HEX.encode(reserialized) + " vs \n"
						+ Utils.HEX.encode(payloadBytes));
		}
	}

	Message(NetworkParams params, byte[] payload, int offset) throws ProtocolException {
		this(params, payload, offset, NetworkParams.PROTOCOL_VERSION, false, false, UNKNOWN_LENGTH);
	}

	Message(NetworkParams params, byte[] payload, int offset, boolean parseLazy, boolean parseRetain, int length)
			throws ProtocolException {
		this(params, payload, offset, NetworkParams.PROTOCOL_VERSION, parseLazy, parseRetain, length);
	}

	abstract void parse() throws ProtocolException;

	protected abstract void parseLite() throws ProtocolException;

	protected synchronized void maybeParse() {
		if (parsed || payload == null)
			return;
		try {
			parse();
			parsed = true;
			if (!parseRetain)
				payload = null;
		} catch (ProtocolException e) {
			throw new LazyParseException(
					"ProtocolException caught during lazy parse.  For safe access to fields call ensureParsed before attempting read or write access",
					e);
		}
	}

	public void ensureParsed() throws ProtocolException {
		try {
			maybeParse();
		} catch (LazyParseException e) {
			if (e.getCause() instanceof ProtocolException)
				throw (ProtocolException) e.getCause();
			throw new ProtocolException(e);
		}
	}

	protected void unCache() {
		maybeParse();
		checksum = null;
		payload = null;
		recached = false;
	}

	protected void adjustLength(int newArraySize, int adjustment) {
		if (length == UNKNOWN_LENGTH)
			return;

		if (adjustment == UNKNOWN_LENGTH) {
			length = UNKNOWN_LENGTH;
			return;
		}
		length += adjustment;

		if (newArraySize == 1)
			length++;
		else if (newArraySize != 0)
			length += VariableInt.sizeOf(newArraySize) - VariableInt.sizeOf(newArraySize - 1);
	}

	public boolean isParsed() {
		return parsed;
	}

	public boolean isCached() {
		return payload != null;
	}

	public boolean isRecached() {
		return recached;
	}

	byte[] getChecksum() {
		return checksum;
	}

	void setChecksum(byte[] checksum) {
		if (checksum.length != 4)
			throw new IllegalArgumentException("Checksum length must be 4 bytes, actual length: " + checksum.length);
		this.checksum = checksum;
	}

	public byte[] bitcoinSerialize() {
		byte[] bytes = unsafeBitcoinSerialize();
		byte[] copy = new byte[bytes.length];
		System.arraycopy(bytes, 0, copy, 0, bytes.length);
		return copy;
	}

	public byte[] unsafeBitcoinSerialize() {

		if (payload != null) {
			if (offset == 0 && length == payload.length) {

				return payload;
			}

			byte[] buf = new byte[length];
			System.arraycopy(payload, offset, buf, 0, length);
			return buf;
		}

		ByteArrayOutputStream stream = new UnsafeOutput(length < 32 ? 32 : length + 32);
		try {
			bitcoinSerializeToStream(stream);
		} catch (IOException e) {

		}

		if (parseRetain) {

			payload = stream.toByteArray();
			cursor = cursor - offset;
			offset = 0;
			recached = true;
			length = payload.length;
			return payload;
		}

		byte[] buf = stream.toByteArray();
		length = buf.length;
		return buf;
	}

	public final void bitcoinSerialize(OutputStream stream) throws IOException {

		if (payload != null && length != UNKNOWN_LENGTH) {
			stream.write(payload, offset, length);
			return;
		}

		bitcoinSerializeToStream(stream);
	}

	void bitcoinSerializeToStream(OutputStream stream) throws IOException {
		log.error(
				"Error: {} class has not implemented bitcoinSerializeToStream method.  Generating message with no payload",
				getClass());
	}

	public Sha256Hash getHash() {
		throw new UnsupportedOperationException();
	}

	public int getMessageSize() {
		if (length != UNKNOWN_LENGTH)
			return length;
		maybeParse();
		if (length == UNKNOWN_LENGTH)
			checkState(false, "Length field has not been set in %s after full parse.", getClass().getSimpleName());
		return length;
	}

	long readUint32() throws ProtocolException {
		try {
			long u = Utils.readUint32(payload, cursor);
			cursor += 4;
			return u;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ProtocolException(e);
		}
	}

	long readInt64() throws ProtocolException {
		try {
			long u = Utils.readInt64(payload, cursor);
			cursor += 8;
			return u;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ProtocolException(e);
		}
	}

	BigInteger readUint64() throws ProtocolException {

		return new BigInteger(Utils.reverseBytes(readBytes(8)));
	}

	long readVarInt() throws ProtocolException {
		return readVarInt(0);
	}

	long readVarInt(int offset) throws ProtocolException {
		try {
			VariableInt varint = new VariableInt(payload, cursor + offset);
			cursor += offset + varint.getOriginalSizeInBytes();
			return varint.value;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ProtocolException(e);
		}
	}

	byte[] readBytes(int length) throws ProtocolException {
		if (length > MAX_SIZE) {
			throw new ProtocolException("Claimed value length too large: " + length);
		}
		try {
			byte[] b = new byte[length];
			System.arraycopy(payload, cursor, b, 0, length);
			cursor += length;
			return b;
		} catch (IndexOutOfBoundsException e) {
			throw new ProtocolException(e);
		}
	}

	byte[] readByteArray() throws ProtocolException {
		long len = readVarInt();
		return readBytes((int) len);
	}

	String readStr() throws ProtocolException {
		long length = readVarInt();
		return length == 0 ? "" : Utils.toString(readBytes((int) length), "UTF-8");
	}

	Sha256Hash readHash() throws ProtocolException {

		return Sha256Hash.wrapReversed(readBytes(32));
	}

	boolean hasMoreBytes() {
		return cursor < payload.length;
	}

	public NetworkParams getParams() {
		return params;
	}

	public static class LazyParseException extends RuntimeException {
		private static final long serialVersionUID = 6971943053112975594L;

		public LazyParseException(String message, Throwable cause) {
			super(message, cause);
		}

		public LazyParseException(String message) {
			super(message);
		}

	}
}
