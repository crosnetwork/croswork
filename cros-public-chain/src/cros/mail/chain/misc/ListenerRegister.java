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

import java.util.List;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListenerRegister<T> {
	public final T listener;
	public final Executor executor;

	public ListenerRegister(T listener, Executor executor) {
		this.listener = checkNotNull(listener);
		this.executor = checkNotNull(executor);
	}

	public static <T> boolean removeFromList(T listener, List<? extends ListenerRegister<T>> list) {
		checkNotNull(listener);

		ListenerRegister<T> item = null;
		for (ListenerRegister<T> registration : list) {
			if (registration.listener == listener) {
				item = registration;
				break;
			}
		}
		return item != null && list.remove(item);
	}
}
