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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.*;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ProgressTracker extends AbstractPeerListener {
	private static final Logger log = LoggerFactory.getLogger(ProgressTracker.class);
	private int originalBlocksLeft = -1;
	private int lastPercent = 0;
	private SettableFuture<Long> future = SettableFuture.create();
	private boolean caughtUp = false;

	@Override
	public void onChainDownloadStarted(Peer peer, int blocksLeft) {
		if (blocksLeft > 0 && originalBlocksLeft == -1)
			startDownload(blocksLeft);

		if (originalBlocksLeft == -1)
			originalBlocksLeft = blocksLeft;
		else
			log.info("Chain download switched to {}", peer);
		if (blocksLeft == 0) {
			doneDownload();
			future.set(peer.getBestHeight());
		}
	}

	@Override
	public void onBlocksDownloaded(Peer peer, Block block, @Nullable LightBlock lightBlock, int blocksLeft) {
		if (caughtUp)
			return;

		if (blocksLeft == 0) {
			caughtUp = true;
			doneDownload();
			future.set(peer.getBestHeight());
		}

		if (blocksLeft < 0 || originalBlocksLeft <= 0)
			return;

		double pct = 100.0 - (100.0 * (blocksLeft / (double) originalBlocksLeft));
		if ((int) pct != lastPercent) {
			progress(pct, blocksLeft, new Date(block.getTimeSeconds() * 1000));
			lastPercent = (int) pct;
		}
	}

	protected void progress(double pct, int blocksSoFar, Date date) {
		log.info(String.format(Locale.US, "Chain download %d%% done with %d blocks to go, block date %s", (int) pct,
				blocksSoFar, Utils.dateTimeFormat(date)));
	}

	protected void startDownload(int blocks) {
		log.info("Downloading block chain of size " + blocks + ". " + (blocks > 1000 ? "This may take a while." : ""));
	}

	protected void doneDownload() {
	}

	public void await() throws InterruptedException {
		try {
			future.get();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public ListenableFuture<Long> getFuture() {
		return future;
	}
}
