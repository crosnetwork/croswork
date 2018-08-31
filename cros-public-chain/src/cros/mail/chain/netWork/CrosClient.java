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

import com.google.common.base.*;
import com.google.common.util.concurrent.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.*;

public class CrosClient implements MessageTarget {
	private static final Logger log = LoggerFactory.getLogger(CrosClient.class);

	private final Handler handler;
	private final CrosClientManager manager = new CrosClientManager();

	class Handler extends AbstractTimeout implements StreamMessageParser {
		private final StreamMessageParser upstreamParser;
		private MessageTarget writeTarget;
		private boolean closeOnOpen = false;
		private boolean closeCalled = false;

		Handler(StreamMessageParser upstreamParser, int connectTimeoutMillis) {
			this.upstreamParser = upstreamParser;
			setSocketTimeout(connectTimeoutMillis);
			setTimeoutEnabled(true);
		}

		@Override
		protected synchronized void timeoutOccurred() {
			closeOnOpen = true;
			connectionClosed();
		}

		@Override
		public synchronized void connectionClosed() {
			manager.stopAsync();
			if (!closeCalled) {
				closeCalled = true;
				upstreamParser.connectionClosed();
			}
		}

		@Override
		public synchronized void connectionOpened() {
			if (!closeOnOpen)
				upstreamParser.connectionOpened();
		}

		@Override
		public int receiveBytes(ByteBuffer buff) throws Exception {
			return upstreamParser.receiveBytes(buff);
		}

		@Override
		public synchronized void setWriteTarget(MessageTarget writeTarget) {
			if (closeOnOpen)
				writeTarget.closeConnection();
			else {
				setTimeoutEnabled(false);
				this.writeTarget = writeTarget;
				upstreamParser.setWriteTarget(writeTarget);
			}
		}

		@Override
		public int getMaxMessageSize() {
			return upstreamParser.getMaxMessageSize();
		}
	}

	public CrosClient(final SocketAddress serverAddress, final StreamMessageParser parser,
			final int connectTimeoutMillis) throws IOException {
		manager.startAsync();
		manager.awaitRunning();
		handler = new Handler(parser, connectTimeoutMillis);
		Futures.addCallback(manager.openConnection(serverAddress, handler), new FutureCallback<SocketAddress>() {
			@Override
			public void onSuccess(SocketAddress result) {
			}

			@Override
			public void onFailure(Throwable t) {
				log.error("Connect to {} failed: {}", serverAddress, Throwables.getRootCause(t));
			}
		});
	}

	@Override
	public void closeConnection() {
		handler.writeTarget.closeConnection();
	}

	@Override
	public synchronized void writeBytes(byte[] message) throws IOException {
		handler.writeTarget.writeBytes(message);
	}
}
