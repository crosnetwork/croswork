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

import static cros.mail.chain.core.Utils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class TokenSerializer {
	private static final Logger log = LoggerFactory.getLogger(TokenSerializer.class);
	private static final int COMMAND_LEN = 12;

	private NetworkParams params;
	private boolean parseLazy = false;
	private boolean parseRetain = false;

	private static Map<Class<? extends Message>, String> names = new HashMap<Class<? extends Message>, String>();

	static {
		names.put(VersionMessage.class, "version");
		names.put(InventoryMessage.class, "inv");
		names.put(Block.class, "block");
		names.put(GetDataMessage.class, "getdata");
		names.put(Transaction.class, "tx");
		names.put(AddressMessage.class, "addr");
		names.put(Ping.class, "ping");
		names.put(Pong.class, "pong");
		names.put(Version.class, "verack");
		names.put(GetBlocksMessage.class, "getblocks");
		names.put(GetHeaderMessage.class, "getheaders");
		names.put(GetAddrMessage.class, "getaddr");
		names.put(HeadersMessage.class, "headers");
		names.put(BloomFilter.class, "filterload");
		names.put(LightBlock.class, "merkleblock");
		names.put(NotFoundMessage.class, "notfound");
		names.put(PoolMessage.class, "mempool");
		names.put(RejectMessage.class, "reject");
		names.put(GetUTXOsMessage.class, "getutxos");
		names.put(UnspentMessage.class, "utxos");
	}

	public TokenSerializer(NetworkParams params) {
		this(params, false, false);
	}

	public TokenSerializer(NetworkParams params, boolean parseLazy, boolean parseRetain) {
		this.params = params;
		this.parseLazy = parseLazy;
		this.parseRetain = parseRetain;
	}

	public void serialize(String name, byte[] message, OutputStream out) throws IOException {
		byte[] header = new byte[4 + COMMAND_LEN + 4 + 4];
		uint32ToByteArrayBE(params.getPacketMagic(), header, 0);

		for (int i = 0; i < name.length() && i < COMMAND_LEN; i++) {
			header[4 + i] = (byte) (name.codePointAt(i) & 0xFF);
		}

		Utils.uint32ToByteArrayLE(message.length, header, 4 + COMMAND_LEN);

		byte[] hash = Sha256Hash.hashTwice(message);
		System.arraycopy(hash, 0, header, 4 + COMMAND_LEN + 4, 4);
		out.write(header);
		out.write(message);

		if (log.isDebugEnabled())
			log.debug("Sending {} message: {}", name, HEX.encode(header) + HEX.encode(message));
	}

	public void serialize(Message message, OutputStream out) throws IOException {
		String name = names.get(message.getClass());
		if (name == null) {
			throw new Error("TokenSerializer doesn't currently know how to serialize " + message.getClass());
		}
		serialize(name, message.bitcoinSerialize(), out);
	}

	public Message deserialize(ByteBuffer in) throws ProtocolException, IOException {

		seekPastMagicBytes(in);
		BitcoinPacketHeader header = new BitcoinPacketHeader(in);

		return deserializePayload(header, in);
	}

	public BitcoinPacketHeader deserializeHeader(ByteBuffer in) throws ProtocolException, IOException {
		return new BitcoinPacketHeader(in);
	}

	public Message deserializePayload(BitcoinPacketHeader header, ByteBuffer in)
			throws ProtocolException, BufferUnderflowException {
		byte[] payloadBytes = new byte[header.size];
		in.get(payloadBytes, 0, header.size);

		byte[] hash;
		hash = Sha256Hash.hashTwice(payloadBytes);
		if (header.checksum[0] != hash[0] || header.checksum[1] != hash[1] || header.checksum[2] != hash[2]
				|| header.checksum[3] != hash[3]) {
			throw new ProtocolException(
					"Checksum failed to verify, actual " + HEX.encode(hash) + " vs " + HEX.encode(header.checksum));
		}

		if (log.isDebugEnabled()) {
			log.debug("Received {} byte '{}' message: {}", header.size, header.command, HEX.encode(payloadBytes));
		}

		try {
			return makeMessage(header.command, header.size, payloadBytes, hash, header.checksum);
		} catch (Exception e) {
			throw new ProtocolException("Error deserializing message " + HEX.encode(payloadBytes) + "\n", e);
		}
	}

	private Message makeMessage(String command, int length, byte[] payloadBytes, byte[] hash, byte[] checksum)
			throws ProtocolException {

		Message message;
		if (command.equals("version")) {
			return new VersionMessage(params, payloadBytes);
		} else if (command.equals("inv")) {
			message = new InventoryMessage(params, payloadBytes, parseLazy, parseRetain, length);
		} else if (command.equals("block")) {
			message = new Block(params, payloadBytes, parseLazy, parseRetain, length);
		} else if (command.equals("merkleblock")) {
			message = new LightBlock(params, payloadBytes);
		} else if (command.equals("getdata")) {
			message = new GetDataMessage(params, payloadBytes, parseLazy, parseRetain, length);
		} else if (command.equals("getblocks")) {
			message = new GetBlocksMessage(params, payloadBytes);
		} else if (command.equals("getheaders")) {
			message = new GetHeaderMessage(params, payloadBytes);
		} else if (command.equals("tx")) {
			Transaction tx = new Transaction(params, payloadBytes, null, parseLazy, parseRetain, length);
			if (hash != null)
				tx.setHash(Sha256Hash.wrapReversed(hash));
			message = tx;
		} else if (command.equals("addr")) {
			message = new AddressMessage(params, payloadBytes, parseLazy, parseRetain, length);
		} else if (command.equals("ping")) {
			message = new Ping(params, payloadBytes);
		} else if (command.equals("pong")) {
			message = new Pong(params, payloadBytes);
		} else if (command.equals("verack")) {
			return new Version(params, payloadBytes);
		} else if (command.equals("headers")) {
			return new HeadersMessage(params, payloadBytes);
		} else if (command.equals("alert")) {
			return new AlertMessage(params, payloadBytes);
		} else if (command.equals("filterload")) {
			return new BloomFilter(params, payloadBytes);
		} else if (command.equals("notfound")) {
			return new NotFoundMessage(params, payloadBytes);
		} else if (command.equals("mempool")) {
			return new PoolMessage();
		} else if (command.equals("reject")) {
			return new RejectMessage(params, payloadBytes);
		} else if (command.equals("utxos")) {
			return new UnspentMessage(params, payloadBytes);
		} else if (command.equals("getutxos")) {
			return new GetUTXOsMessage(params, payloadBytes);
		} else {
			log.warn("No support for deserializing message with name {}", command);
			return new UnknownMessage(params, command, payloadBytes);
		}
		if (checksum != null)
			message.setChecksum(checksum);
		return message;
	}

	public void seekPastMagicBytes(ByteBuffer in) throws BufferUnderflowException {
		int magicCursor = 3;
		while (true) {
			byte b = in.get();

			byte expectedByte = (byte) (0xFF & params.getPacketMagic() >>> (magicCursor * 8));
			if (b == expectedByte) {
				magicCursor--;
				if (magicCursor < 0) {

					return;
				} else {

				}
			} else {
				magicCursor = 3;
			}
		}
	}

	public boolean isParseLazyMode() {
		return parseLazy;
	}

	public boolean isParseRetainMode() {
		return parseRetain;
	}

	public static class BitcoinPacketHeader {

		public static final int HEADER_LENGTH = COMMAND_LEN + 4 + 4;

		public final byte[] header;
		public final String command;
		public final int size;
		public final byte[] checksum;

		public BitcoinPacketHeader(ByteBuffer in) throws ProtocolException, BufferUnderflowException {
			header = new byte[HEADER_LENGTH];
			in.get(header, 0, header.length);

			int cursor = 0;

			for (; header[cursor] != 0 && cursor < COMMAND_LEN; cursor++)
				;
			byte[] commandBytes = new byte[cursor];
			System.arraycopy(header, 0, commandBytes, 0, cursor);
			command = Utils.toString(commandBytes, "US-ASCII");
			cursor = COMMAND_LEN;

			size = (int) readUint32(header, cursor);
			cursor += 4;

			if (size > Message.MAX_SIZE)
				throw new ProtocolException("Message size too large: " + size);

			checksum = new byte[4];

			System.arraycopy(header, cursor, checksum, 0, 4);
			cursor += 4;
		}
	}
}
