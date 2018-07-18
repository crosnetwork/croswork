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

import org.slf4j.*;

import static com.google.common.base.Preconditions.*;

public class Context {
	private static final Logger log = LoggerFactory.getLogger(Context.class);

	private TxDegreeTable confidenceTable;
	private NetworkParams params;
	private int eventHorizon = 100;

	public Context(NetworkParams params) {
		this.confidenceTable = new TxDegreeTable();
		this.params = params;
		lastConstructed = this;

		slot.set(this);
	}

	public Context(NetworkParams params, int eventHorizon) {
		this(params);
		this.eventHorizon = eventHorizon;
	}

	private static volatile Context lastConstructed;
	private static final ThreadLocal<Context> slot = new ThreadLocal<Context>();

	public static Context get() {
		Context tls = slot.get();
		if (tls == null) {
			if (lastConstructed == null)
				throw new IllegalStateException("You must construct a Context object before using bitcoinj!");
			slot.set(lastConstructed);
			log.error(
					"Performing thread fixup: you are accessing bitcoinj via a thread that has not had any context set on it.");
			log.error("This error has been corrected for, but doing this makes your app less robust.");
			log.error("You should use Context.propagate() or a ContextThreadFactory.");
			log.error("Please refer to the user guide for more information about this.");

			return lastConstructed;
		} else {
			return tls;
		}
	}

	public static Context getOrCreate(NetworkParams params) {
		Context context;
		try {
			context = get();
		} catch (IllegalStateException e) {
			log.warn("Implicitly creating context. This is a migration step and this message will eventually go away.");
			context = new Context(params);
			return context;
		}
		if (context.getParams() != params)
			throw new IllegalStateException(
					"Context does not match implicit network params: " + context.getParams() + " vs " + params);
		return context;
	}

	public static void propagate(Context context) {
		slot.set(checkNotNull(context));
	}

	public TxDegreeTable getConfidenceTable() {
		return confidenceTable;
	}

	public NetworkParams getParams() {
		return params;
	}

	public int getEventHorizon() {
		return eventHorizon;
	}
}
