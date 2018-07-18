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
package cros.mail.chain.wallet;

import org.slf4j.*;

import cros.mail.chain.core.*;
import cros.mail.chain.misc.*;

import javax.annotation.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.google.common.base.Preconditions.*;

public class CrosWalletFile {
	private static final Logger log = LoggerFactory.getLogger(CrosWalletFile.class);

	private final Wallet wallet;
	private final ScheduledThreadPoolExecutor executor;
	private final File file;
	private final AtomicBoolean savePending;
	private final long delay;
	private final TimeUnit delayTimeUnit;
	private final Callable<Void> saver;

	private volatile Listener vListener;

	public interface Listener {

		void onBeforeAutoSave(File tempFile);

		void onAfterAutoSave(File newlySavedFile);
	}

	public CrosWalletFile(final Wallet wallet, File file, long delay, TimeUnit delayTimeUnit) {

		this.executor = new ScheduledThreadPoolExecutor(1,
				new ContextThreadFactory("Wallet autosave thread", Thread.MIN_PRIORITY));
		this.executor.setKeepAliveTime(5, TimeUnit.SECONDS);
		this.executor.allowCoreThreadTimeOut(true);
		this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		this.wallet = checkNotNull(wallet);

		this.file = checkNotNull(file);
		this.savePending = new AtomicBoolean();
		this.delay = delay;
		this.delayTimeUnit = checkNotNull(delayTimeUnit);

		this.saver = new Callable<Void>() {
			@Override
			public Void call() throws Exception {

				if (!savePending.getAndSet(false)) {

					return null;
				}
				Date lastBlockSeenTime = wallet.getLastBlockSeenTime();
				log.info("Background saving wallet; last seen block is height {}, date {}, hash {}",
						wallet.getLastBlockSeenHeight(),
						lastBlockSeenTime != null ? Utils.dateTimeFormat(lastBlockSeenTime) : "unknown",
						wallet.getLastBlockSeenHash());
				saveNowInternal();
				return null;
			}
		};
	}

	public void setListener(@Nonnull Listener listener) {
		this.vListener = checkNotNull(listener);
	}

	public void saveNow() throws IOException {

		Date lastBlockSeenTime = wallet.getLastBlockSeenTime();
		log.info("Saving wallet; last seen block is height {}, date {}, hash {}", wallet.getLastBlockSeenHeight(),
				lastBlockSeenTime != null ? Utils.dateTimeFormat(lastBlockSeenTime) : "unknown",
				wallet.getLastBlockSeenHash());
		saveNowInternal();
	}

	private void saveNowInternal() throws IOException {
		long now = System.currentTimeMillis();
		File directory = file.getAbsoluteFile().getParentFile();
		File temp = File.createTempFile("wallet", null, directory);
		final Listener listener = vListener;
		if (listener != null)
			listener.onBeforeAutoSave(temp);
		wallet.saveToFile(temp, file);
		if (listener != null)
			listener.onAfterAutoSave(file);
		log.info("Save completed in {}msec", System.currentTimeMillis() - now);
	}

	public void saveLater() {
		if (savePending.getAndSet(true))
			return;
		executor.schedule(saver, delay, delayTimeUnit);
	}

	public void shutdownAndWait() {
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException x) {
			throw new RuntimeException(x);
		}
	}
}
