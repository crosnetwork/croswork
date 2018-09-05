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
package cros.mail.chain.netWork;

import cros.mail.chain.core.Utils;
import cros.mail.chain.misc.Threading;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ProtobufMessageParser<MessageType extends MessageLite> extends AbstractTimeout
		implements StreamMessageParser {
	private static final Logger log = LoggerFactory.getLogger(ProtobufMessageParser.class);

	public interface Listener<MessageType extends MessageLite> {

		void messageReceived(ProtobufMessageParser<MessageType> handler, MessageType msg);

		void connectionOpen(ProtobufMessageParser<MessageType> handler);

		void connectionClosed(ProtobufMessageParser<MessageType> handler);
	}

	private final Listener<MessageType> handler;

	private final MessageLite prototype;

	final int maxMessageSize;

	@GuardedBy("lock")
	private int messageBytesOffset = 0;
	@GuardedBy("lock")
	private byte[] messageBytes;
	private final ReentrantLock lock = Threading.lock("ProtobufMessageParser");

	@VisibleForTesting
	final AtomicReference<MessageTarget> writeTarget = new AtomicReference<MessageTarget>();

	public ProtobufMessageParser(Listener<MessageType> handler, MessageType prototype, int maxMessageSize,
			int timeoutMillis) {
		this.handler = handler;
		this.prototype = prototype;
		this.maxMessageSize = Math.min(maxMessageSize, Integer.MAX_VALUE - 4);
		setTimeoutEnabled(false);
		setSocketTimeout(timeoutMillis);
	}

	@Override
	public void setWriteTarget(MessageTarget writeTarget) {

		checkState(this.writeTarget.getAndSet(checkNotNull(writeTarget)) == null);
	}

	@Override
	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	public void closeConnection() {
		this.writeTarget.get().closeConnection();
	}

	@Override
	protected void timeoutOccurred() {
		log.warn("Timeout occurred for " + handler);
		closeConnection();
	}

	@SuppressWarnings("unchecked")

	private void deserializeMessage(ByteBuffer buff) throws Exception {
		MessageType msg = (MessageType) prototype.newBuilderForType().mergeFrom(ByteString.copyFrom(buff)).build();
		resetTimeout();
		handler.messageReceived(this, msg);
	}

	@Override
	public int receiveBytes(ByteBuffer buff) throws Exception {
		lock.lock();
		try {
			if (messageBytes != null) {

				int bytesToGet = Math.min(messageBytes.length - messageBytesOffset, buff.remaining());
				buff.get(messageBytes, messageBytesOffset, bytesToGet);
				messageBytesOffset += bytesToGet;
				if (messageBytesOffset == messageBytes.length) {

					deserializeMessage(ByteBuffer.wrap(messageBytes));
					messageBytes = null;
					if (buff.hasRemaining())
						return bytesToGet + receiveBytes(buff);
				}
				return bytesToGet;
			}

			if (buff.remaining() < 4)
				return 0;

			buff.order(ByteOrder.BIG_ENDIAN);
			final int len = buff.getInt();

			if (len > maxMessageSize || len + 4 < 4)
				throw new IllegalStateException("Message too large or length underflowed");

			if (buff.capacity() < len + 4) {
				messageBytes = new byte[len];

				int bytesToRead = buff.remaining();
				buff.get(messageBytes, 0, bytesToRead);
				messageBytesOffset = bytesToRead;
				return bytesToRead + 4;
			}

			if (buff.remaining() < len) {

				buff.position(buff.position() - 4);
				return 0;
			}

			int limit = buff.limit();
			buff.limit(buff.position() + len);
			deserializeMessage(buff);
			checkState(buff.remaining() == 0);
			buff.limit(limit);

			if (buff.hasRemaining())
				return len + 4 + receiveBytes(buff);
			else
				return len + 4;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void connectionClosed() {
		handler.connectionClosed(this);
	}

	@Override
	public void connectionOpened() {
		setTimeoutEnabled(true);
		handler.connectionOpen(this);
	}

	public void write(MessageType msg) throws IllegalStateException {
		byte[] messageBytes = msg.toByteArray();
		checkState(messageBytes.length <= maxMessageSize);
		byte[] messageLength = new byte[4];
		Utils.uint32ToByteArrayBE(messageBytes.length, messageLength, 0);
		try {
			MessageTarget target = writeTarget.get();
			target.writeBytes(messageLength);
			target.writeBytes(messageBytes);
		} catch (IOException e) {
			closeConnection();
		}
	}
}
