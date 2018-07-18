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

import cros.mail.chain.core.Message;
import cros.mail.chain.misc.Threading;
import com.google.common.base.Throwables;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

class ConnectionHandler implements MessageTarget {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

	private static final int BUFFER_SIZE_LOWER_BOUND = 4096;
	private static final int BUFFER_SIZE_UPPER_BOUND = 65536;

	private static final int OUTBOUND_BUFFER_BYTE_COUNT = Message.MAX_SIZE + 24;

	private final ReentrantLock lock = Threading.lock("nioConnectionHandler");
	@GuardedBy("lock")
	private final ByteBuffer readBuff;
	@GuardedBy("lock")
	private final SocketChannel channel;
	@GuardedBy("lock")
	private final SelectionKey key;
	@GuardedBy("lock")
	StreamMessageParser parser;
	@GuardedBy("lock")
	private boolean closeCalled = false;

	@GuardedBy("lock")
	private long bytesToWriteRemaining = 0;
	@GuardedBy("lock")
	private final LinkedList<ByteBuffer> bytesToWrite = new LinkedList<ByteBuffer>();

	private Set<ConnectionHandler> connectedHandlers;

	public ConnectionHandler(StreamMessageParserFactory parserFactory, SelectionKey key) throws IOException {
		this(parserFactory.getNewParser(((SocketChannel) key.channel()).socket().getInetAddress(),
				((SocketChannel) key.channel()).socket().getPort()), key);
		if (parser == null)
			throw new IOException("Parser factory.getNewParser returned null");
	}

	private ConnectionHandler(@Nullable StreamMessageParser parser, SelectionKey key) {
		this.key = key;
		this.channel = checkNotNull(((SocketChannel) key.channel()));
		if (parser == null) {
			readBuff = null;
			return;
		}
		this.parser = parser;
		readBuff = ByteBuffer.allocateDirect(
				Math.min(Math.max(parser.getMaxMessageSize(), BUFFER_SIZE_LOWER_BOUND), BUFFER_SIZE_UPPER_BOUND));
		parser.setWriteTarget(this);
		connectedHandlers = null;
	}

	public ConnectionHandler(StreamMessageParser parser, SelectionKey key, Set<ConnectionHandler> connectedHandlers) {
		this(checkNotNull(parser), key);

		lock.lock();
		try {
			this.connectedHandlers = connectedHandlers;
			if (!closeCalled)
				checkState(this.connectedHandlers.add(this));
		} finally {
			lock.unlock();
		}
	}

	@GuardedBy("lock")
	private void setWriteOps() {

		key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

		key.selector().wakeup();
	}

	private void tryWriteBytes() throws IOException {
		lock.lock();
		try {

			Iterator<ByteBuffer> bytesIterator = bytesToWrite.iterator();
			while (bytesIterator.hasNext()) {
				ByteBuffer buff = bytesIterator.next();
				bytesToWriteRemaining -= channel.write(buff);
				if (!buff.hasRemaining())
					bytesIterator.remove();
				else {
					setWriteOps();
					break;
				}
			}

			if (bytesToWrite.isEmpty())
				key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void writeBytes(byte[] message) throws IOException {
		boolean andUnlock = true;
		lock.lock();
		try {

			if (bytesToWriteRemaining + message.length > OUTBOUND_BUFFER_BYTE_COUNT)
				throw new IOException("Outbound buffer overflowed");

			bytesToWrite.offer(ByteBuffer.wrap(Arrays.copyOf(message, message.length)));
			bytesToWriteRemaining += message.length;
			setWriteOps();
		} catch (IOException e) {
			lock.unlock();
			andUnlock = false;
			log.warn("Error writing message to connection, closing connection", e);
			closeConnection();
			throw e;
		} catch (CancelledKeyException e) {
			lock.unlock();
			andUnlock = false;
			log.warn("Error writing message to connection, closing connection", e);
			closeConnection();
			throw new IOException(e);
		} finally {
			if (andUnlock)
				lock.unlock();
		}
	}

	@Override
	public void closeConnection() {
		checkState(!lock.isHeldByCurrentThread());
		try {
			channel.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		connectionClosed();
	}

	private void connectionClosed() {
		boolean callClosed = false;
		lock.lock();
		try {
			callClosed = !closeCalled;
			closeCalled = true;
		} finally {
			lock.unlock();
		}
		if (callClosed) {
			checkState(connectedHandlers == null || connectedHandlers.remove(this));
			parser.connectionClosed();
		}
	}

	public static void handleKey(SelectionKey key) {
		ConnectionHandler handler = ((ConnectionHandler) key.attachment());
		try {
			if (handler == null)
				return;
			if (!key.isValid()) {
				handler.closeConnection();
				return;
			}
			if (key.isReadable()) {

				int read = handler.channel.read(handler.readBuff);
				if (read == 0)
					return;
				else if (read == -1) {
					key.cancel();
					handler.closeConnection();
					return;
				}

				handler.readBuff.flip();

				int bytesConsumed = checkNotNull(handler.parser).receiveBytes(handler.readBuff);
				checkState(handler.readBuff.position() == bytesConsumed);

				handler.readBuff.compact();
			}
			if (key.isWritable())
				handler.tryWriteBytes();
		} catch (Exception e) {

			Throwable t = Throwables.getRootCause(e);
			log.warn("Error handling SelectionKey: {}",
					t.getMessage() != null ? t.getMessage() : t.getClass().getName());
			handler.closeConnection();
		}
	}
}
