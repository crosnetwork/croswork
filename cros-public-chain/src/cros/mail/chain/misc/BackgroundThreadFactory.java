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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BackgroundThreadFactory implements ThreadFactory {
	@Nullable
	private final String name;

	public BackgroundThreadFactory(@Nullable String name) {
		this.name = name;
	}

	public BackgroundThreadFactory() {
		this(null);
	}

	@Override
	public Thread newThread(@Nonnull Runnable runnable) {
		Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setDaemon(true);
		if (name != null)
			thread.setName(name);
		return thread;
	}
}
