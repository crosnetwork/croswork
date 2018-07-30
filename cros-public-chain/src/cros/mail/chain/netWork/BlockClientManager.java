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

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListenableFuture;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlockClientManager extends AbstractIdleService implements ConnectionManager {
	private final SocketFactory socketFactory;
	private final Set<BlockClient> clients = Collections.synchronizedSet(new HashSet<BlockClient>());

	private int connectTimeoutMillis = 1000;

	public BlockClientManager() {
		socketFactory = SocketFactory.getDefault();
	}

	public BlockClientManager(SocketFactory socketFactory) {
		this.socketFactory = checkNotNull(socketFactory);
	}

	@Override
	public ListenableFuture<SocketAddress> openConnection(SocketAddress serverAddress, StreamMessageParser parser) {
		try {
			if (!isRunning())
				throw new IllegalStateException();
			return new BlockClient(serverAddress, parser, connectTimeoutMillis, socketFactory, clients)
					.getConnectFuture();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setConnectTimeoutMillis(int connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	@Override
	protected void startUp() throws Exception {
	}

	@Override
	protected void shutDown() throws Exception {
		synchronized (clients) {
			for (BlockClient client : clients)
				client.closeConnection();
		}
	}

	@Override
	public int getConnectedClientCount() {
		return clients.size();
	}

	@Override
	public void closeConnections(int n) {
		if (!isRunning())
			throw new IllegalStateException();
		synchronized (clients) {
			Iterator<BlockClient> it = clients.iterator();
			while (n-- > 0 && it.hasNext())
				it.next().closeConnection();
		}
	}
}
