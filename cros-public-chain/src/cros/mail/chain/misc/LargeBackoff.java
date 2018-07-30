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

import static com.google.common.base.Preconditions.checkArgument;

import cros.mail.chain.core.Utils;

public class LargeBackoff implements Comparable<LargeBackoff> {
	public static final int DEFAULT_INITIAL_MILLIS = 100;
	public static final float DEFAULT_MULTIPLIER = 1.1f;
	public static final int DEFAULT_MAXIMUM_MILLIS = 30 * 1000;

	private float backoff;
	private long retryTime;
	private final Params params;

	public static class Params {
		private final float initial;
		private final float multiplier;
		private final float maximum;

		public Params(long initialMillis, float multiplier, long maximumMillis) {
			checkArgument(multiplier > 1.0f, "multiplier must be greater than 1.0");
			checkArgument(maximumMillis >= initialMillis, "maximum must not be less than initial");

			this.initial = initialMillis;
			this.multiplier = multiplier;
			this.maximum = maximumMillis;
		}

		public Params() {
			initial = DEFAULT_INITIAL_MILLIS;
			multiplier = DEFAULT_MULTIPLIER;
			maximum = DEFAULT_MAXIMUM_MILLIS;
		}
	}

	public LargeBackoff(Params params) {
		this.params = params;
		trackSuccess();
	}

	public void trackSuccess() {
		backoff = params.initial;
		retryTime = Utils.currentTimeMillis();
	}

	public void trackFailure() {
		retryTime = Utils.currentTimeMillis() + (long) backoff;
		backoff = Math.min(backoff * params.multiplier, params.maximum);
	}

	public long getRetryTime() {
		return retryTime;
	}

	@Override
	public int compareTo(LargeBackoff other) {
		if (retryTime < other.retryTime)
			return -1;
		if (retryTime > other.retryTime)
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return "LargeBackoff retry=" + retryTime + " backoff=" + backoff;
	}
}
