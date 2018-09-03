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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.LoggerFactory;

public class CrosServer extends AbstractExecutionThreadService {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(CrosServer.class);

	private final StreamMessageParserFactory parserFactory;

	private final ServerSocketChannel sc;
	@VisibleForTesting
	final Selector selector;

	private void handleKey(Selector selector, SelectionKey key) throws IOException {
		if (key.isValid() && key.isAcceptable()) {

			SocketChannel newChannel = sc.accept();
			newChannel.configureBlocking(false);
			SelectionKey newKey = newChannel.register(selector, SelectionKey.OP_READ);
			try {
				ConnectionHandler handler = new ConnectionHandler(parserFactory, newKey);
				newKey.attach(handler);
				handler.parser.connectionOpened();
			} catch (IOException e) {

				log.error("Error handling new connection", Throwables.getRootCause(e).getMessage());
				newKey.channel().close();
			}
		} else {
			ConnectionHandler.handleKey(key);
		}
	}

	public CrosServer(final StreamMessageParserFactory parserFactory, InetSocketAddress bindAddress)
			throws IOException {
		this.parserFactory = parserFactory;

		sc = ServerSocketChannel.open();
		sc.configureBlocking(false);
		sc.socket().bind(bindAddress);
		selector = SelectorProvider.provider().openSelector();
		sc.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	protected void run() throws Exception {
		try {
			while (isRunning()) {
				selector.select();

				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					keyIterator.remove();

					handleKey(selector, key);
				}
			}
		} catch (Exception e) {
			log.error("Error trying to open/read from connection: {}", e);
		} finally {

			for (SelectionKey key : selector.keys()) {
				try {
					key.channel().close();
				} catch (IOException e) {
					log.error("Error closing channel", e);
				}
				try {
					key.cancel();
					handleKey(selector, key);
				} catch (IOException e) {
					log.error("Error closing selection key", e);
				}
			}
			try {
				selector.close();
			} catch (IOException e) {
				log.error("Error closing server selector", e);
			}
			try {
				sc.close();
			} catch (IOException e) {
				log.error("Error closing server channel", e);
			}
		}
	}

	@Override
	public void triggerShutdown() {

		selector.wakeup();
	}
}
