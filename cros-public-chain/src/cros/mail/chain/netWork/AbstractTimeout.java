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

import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractTimeout {

	private TimerTask timeoutTask;
	private long timeoutMillis = 0;
	private boolean timeoutEnabled = true;

	private static final Timer timeoutTimer = new Timer("AbstractTimeout timeouts", true);

	public synchronized void setTimeoutEnabled(boolean timeoutEnabled) {
		this.timeoutEnabled = timeoutEnabled;
		resetTimeout();
	}

	public synchronized void setSocketTimeout(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
		resetTimeout();
	}

	protected synchronized void resetTimeout() {
		if (timeoutTask != null)
			timeoutTask.cancel();
		if (timeoutMillis == 0 || !timeoutEnabled)
			return;
		timeoutTask = new TimerTask() {
			@Override
			public void run() {
				timeoutOccurred();
			}
		};
		timeoutTimer.schedule(timeoutTask, timeoutMillis);
	}

	protected abstract void timeoutOccurred();
}
