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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * 
 * @author CROS
 *
 */
public class InsufficientFundException extends Exception {

	@Nullable
	public final Coin missing;

	protected InsufficientFundException() {
		this.missing = null;
	}

	public InsufficientFundException(Coin missing) {
		this(missing, "Insufficient money,  missing " + missing.toFriendlyString());
	}

	public InsufficientFundException(Coin missing, String message) {
		super(message);
		this.missing = checkNotNull(missing);
	}
}
