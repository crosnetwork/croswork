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

import cros.mail.chain.core.*;
import com.google.common.util.concurrent.*;

import org.slf4j.*;

import javax.annotation.*;
import javax.net.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import static com.google.common.base.Preconditions.*;

public class BlockClient implements MessageTarget {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(BlockClient.class);

	private static final int BUFFER_SIZE_LOWER_BOUND = 4096;
	private static final int BUFFER_SIZE_UPPER_BOUND = 65536;

	private final ByteBuffer dbuf;
	private Socket socket;
	private volatile boolean vCloseRequested = false;
	private SettableFuture<SocketAddress> connectFuture;

	public BlockClient(final SocketAddress serverAddress, final StreamMessageParser parser,
			final int connectTimeoutMillis, final SocketFactory socketFactory,
			@Nullable final Set<BlockClient> clientSet) throws IOException {
		connectFuture = SettableFuture.create();

		dbuf = ByteBuffer.allocateDirect(
				Math.min(Math.max(parser.getMaxMessageSize(), BUFFER_SIZE_LOWER_BOUND), BUFFER_SIZE_UPPER_BOUND));
		parser.setWriteTarget(this);
		socket = socketFactory.createSocket();
		final Context context = Context.get();
		Thread t = new Thread() {
			@Override
			public void run() {
				Context.propagate(context);
				if (clientSet != null)
					clientSet.add(BlockClient.this);
				try {
					socket.connect(serverAddress, connectTimeoutMillis);
					parser.connectionOpened();
					connectFuture.set(serverAddress);
					InputStream stream = socket.getInputStream();
					byte[] readBuff = new byte[dbuf.capacity()];

					while (true) {

						checkState(dbuf.remaining() > 0 && dbuf.remaining() <= readBuff.length);
						int read = stream.read(readBuff, 0,
								Math.max(1, Math.min(dbuf.remaining(), stream.available())));
						if (read == -1)
							return;
						dbuf.put(readBuff, 0, read);

						dbuf.flip();

						int bytesConsumed = parser.receiveBytes(dbuf);
						checkState(dbuf.position() == bytesConsumed);

						dbuf.compact();
					}
				} catch (Exception e) {
					if (!vCloseRequested) {
						log.error("Error trying to open/read from connection: {}: {}", serverAddress, e.getMessage());
						connectFuture.setException(e);
					}
				} finally {
					try {
						socket.close();
					} catch (IOException e1) {

					}
					if (clientSet != null)
						clientSet.remove(BlockClient.this);
					parser.connectionClosed();
				}
			}
		};
		t.setName("BlockClient network thread for " + serverAddress);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void closeConnection() {

		try {
			vCloseRequested = true;
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void writeBytes(byte[] message) throws IOException {
		try {
			OutputStream stream = socket.getOutputStream();
			stream.write(message);
			stream.flush();
		} catch (IOException e) {
			log.error("Error writing message to connection, closing connection", e);
			closeConnection();
			throw e;
		}
	}

	public ListenableFuture<SocketAddress> getConnectFuture() {
		return connectFuture;
	}
}
