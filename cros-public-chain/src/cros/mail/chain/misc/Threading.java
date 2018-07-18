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
package cros.mail.chain.misc;

import com.google.common.util.concurrent.CycleDetectingLockFactory;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;

import cros.mail.chain.core.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Threading {

	public static Executor USER_THREAD;

	public static final Executor SAME_THREAD;

	public static void waitForUserCode() {
		final CountDownLatch latch = new CountDownLatch(1);
		USER_THREAD.execute(new Runnable() {
			@Override
			public void run() {
				latch.countDown();
			}
		});
		Uninterruptibles.awaitUninterruptibly(latch);
	}

	@Nullable
	public static volatile Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

	public static class UserThread extends Thread implements Executor {
		private static final Logger log = LoggerFactory.getLogger(UserThread.class);

		public static int WARNING_THRESHOLD = 10000;
		private LinkedBlockingQueue<Runnable> tasks;

		public UserThread() {
			super("bitcoinj user thread");
			setDaemon(true);
			tasks = new LinkedBlockingQueue<Runnable>();
			start();
		}

		@SuppressWarnings("InfiniteLoopStatement")
		@Override
		public void run() {
			while (true) {
				Runnable task = Uninterruptibles.takeUninterruptibly(tasks);
				try {
					task.run();
				} catch (Throwable throwable) {
					log.warn("Exception in user thread", throwable);
					Thread.UncaughtExceptionHandler handler = uncaughtExceptionHandler;
					if (handler != null)
						handler.uncaughtException(this, throwable);
				}
			}
		}

		@Override
		public void execute(Runnable command) {
			final int size = tasks.size();
			if (size == WARNING_THRESHOLD) {
				log.warn("User thread has {} pending tasks, memory exhaustion may occur.\n"
						+ "If you see this message, check your memory consumption and see if it's problematic or excessively spikey.\n"
						+ "If it is, check for deadlocked or slow event handlers. If it isn't, try adjusting the constant \n"
						+ "Threading.UserThread.WARNING_THRESHOLD upwards until it's a suitable level for your app, or Integer.MAX_VALUE to disable.",
						size);
			}
			Uninterruptibles.putUninterruptibly(tasks, command);
		}
	}

	static {

		throwOnLockCycles();

		USER_THREAD = new UserThread();
		SAME_THREAD = new Executor() {
			@Override
			public void execute(@Nonnull Runnable runnable) {
				runnable.run();
			}
		};
	}

	private static CycleDetectingLockFactory.Policy policy;
	public static CycleDetectingLockFactory factory;

	public static ReentrantLock lock(String name) {
		if (Utils.isAndroidRuntime())
			return new ReentrantLock(true);
		else
			return factory.newReentrantLock(name);
	}

	public static void warnOnLockCycles() {
		setPolicy(CycleDetectingLockFactory.Policies.WARN);
	}

	public static void throwOnLockCycles() {
		setPolicy(CycleDetectingLockFactory.Policies.THROW);
	}

	public static void ignoreLockCycles() {
		setPolicy(CycleDetectingLockFactory.Policies.DISABLED);
	}

	public static void setPolicy(CycleDetectingLockFactory.Policy policy) {
		Threading.policy = policy;
		factory = CycleDetectingLockFactory.newInstance(policy);
	}

	public static CycleDetectingLockFactory.Policy getPolicy() {
		return policy;
	}

	public static ListeningExecutorService THREAD_POOL = MoreExecutors
			.listeningDecorator(Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setName("Threading.THREAD_POOL worker");
					t.setDaemon(true);
					return t;
				}
			}));
}
