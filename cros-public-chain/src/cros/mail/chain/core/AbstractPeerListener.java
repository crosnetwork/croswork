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

import javax.annotation.*;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author CROS
 *
 */
public class AbstractPeerListener implements PeerEventListener {
	@Override
	public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
	}

	@Override
	public void onBlocksDownloaded(Peer peer, Block block, @Nullable LightBlock lightBlock, int blocksLeft) {
	}

	@Override
	public void onChainDownloadStarted(Peer peer, int blocksLeft) {
	}

	@Override
	public void onPeerConnected(Peer peer, int peerCount) {
	}

	@Override
	public void onPeerDisconnected(Peer peer, int peerCount) {
	}

	@Override
	public Message onPreMessageReceived(Peer peer, Message m) {

		return m;
	}

	@Override
	public void onTransaction(Peer peer, Transaction t) {
	}

	@Override
	public List<Message> getData(Peer peer, GetDataMessage m) {
		return null;
	}
}
