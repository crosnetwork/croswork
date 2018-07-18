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

import cros.mail.chain.misc.Threading;
import cros.mail.chain.netWork.AbstractTimeout;
import cros.mail.chain.netWork.MessageTarget;
import cros.mail.chain.netWork.StreamMessageParser;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.locks.Lock;

import static com.google.common.base.Preconditions.*;

public abstract class PeerHandler extends AbstractTimeout implements StreamMessageParser {
	private static final Logger log = LoggerFactory.getLogger(PeerHandler.class);

	private final TokenSerializer serializer;
	protected PeerAddress peerAddress;

	private boolean closePending = false;

	@VisibleForTesting
	protected MessageTarget writeTarget = null;

	private byte[] largeReadBuffer;
	private int largeReadBufferPos;
	private TokenSerializer.BitcoinPacketHeader header;

	private Lock lock = Threading.lock("PeerHandler");

	public PeerHandler(NetworkParams params, InetSocketAddress remoteIp) {
		serializer = new TokenSerializer(checkNotNull(params));
		this.peerAddress = new PeerAddress(remoteIp);
	}

	public PeerHandler(NetworkParams params, PeerAddress peerAddress) {
		serializer = new TokenSerializer(checkNotNull(params));
		this.peerAddress = checkNotNull(peerAddress);
	}

	public void sendMessage(Message message) throws NotYetConnectedException {
		lock.lock();
		try {
			if (writeTarget == null)
				throw new NotYetConnectedException();
		} finally {
			lock.unlock();
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			serializer.serialize(message, out);
			writeTarget.writeBytes(out.toByteArray());
		} catch (IOException e) {
			exceptionCaught(e);
		}
	}

	public void close() {
		lock.lock();
		try {
			if (writeTarget == null) {
				closePending = true;
				return;
			}
		} finally {
			lock.unlock();
		}
		writeTarget.closeConnection();
	}

	@Override
	protected void timeoutOccurred() {
		log.info("{}: Timed out", getAddress());
		close();
	}

	protected abstract void processMessage(Message m) throws Exception;

	@Override
	public int receiveBytes(ByteBuffer buff) {
		checkArgument(buff.position() == 0 && buff.capacity() >= TokenSerializer.BitcoinPacketHeader.HEADER_LENGTH + 4);
		try {

			boolean firstMessage = true;
			while (true) {

				if (largeReadBuffer != null) {

					checkState(firstMessage);

					int bytesToGet = Math.min(buff.remaining(), largeReadBuffer.length - largeReadBufferPos);
					buff.get(largeReadBuffer, largeReadBufferPos, bytesToGet);
					largeReadBufferPos += bytesToGet;

					if (largeReadBufferPos == largeReadBuffer.length) {

						processMessage(serializer.deserializePayload(header, ByteBuffer.wrap(largeReadBuffer)));
						largeReadBuffer = null;
						header = null;
						firstMessage = false;
					} else
						return buff.position();
				}

				Message message;
				int preSerializePosition = buff.position();
				try {
					message = serializer.deserialize(buff);
				} catch (BufferUnderflowException e) {

					if (firstMessage && buff.limit() == buff.capacity()) {

						buff.position(0);
						try {
							serializer.seekPastMagicBytes(buff);
							header = serializer.deserializeHeader(buff);

							largeReadBuffer = new byte[header.size];
							largeReadBufferPos = buff.remaining();
							buff.get(largeReadBuffer, 0, largeReadBufferPos);
						} catch (BufferUnderflowException e1) {

							throw new ProtocolException(
									"No magic bytes+header after reading " + buff.capacity() + " bytes");
						}
					} else {

						buff.position(preSerializePosition);
					}
					return buff.position();
				}

				processMessage(message);
				firstMessage = false;
			}
		} catch (Exception e) {
			exceptionCaught(e);
			return -1;
		}
	}

	@Override
	public void setWriteTarget(MessageTarget writeTarget) {
		checkArgument(writeTarget != null);
		lock.lock();
		boolean closeNow = false;
		try {
			checkArgument(this.writeTarget == null);
			closeNow = closePending;
			this.writeTarget = writeTarget;
		} finally {
			lock.unlock();
		}
		if (closeNow)
			writeTarget.closeConnection();
	}

	@Override
	public int getMaxMessageSize() {
		return Message.MAX_SIZE;
	}

	public PeerAddress getAddress() {
		return peerAddress;
	}

	private void exceptionCaught(Exception e) {
		PeerAddress addr = getAddress();
		String s = addr == null ? "?" : addr.toString();
		if (e instanceof ConnectException || e instanceof IOException) {

			log.info(s + " - " + e.getMessage());
		} else {
			log.warn(s + " - ", e);
			Thread.UncaughtExceptionHandler handler = Threading.uncaughtExceptionHandler;
			if (handler != null)
				handler.uncaughtException(Thread.currentThread(), e);
		}

		close();
	}
}
