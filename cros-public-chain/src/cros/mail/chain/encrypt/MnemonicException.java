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
package cros.mail.chain.encrypt;

@SuppressWarnings("serial")
public class MnemonicException extends Exception {
	public MnemonicException() {
		super();
	}

	public MnemonicException(String msg) {
		super(msg);
	}

	public static class MnemonicLengthException extends MnemonicException {
		public MnemonicLengthException(String msg) {
			super(msg);
		}
	}

	public static class MnemonicChecksumException extends MnemonicException {
		public MnemonicChecksumException() {
			super();
		}
	}

	public static class MnemonicWordException extends MnemonicException {

		public final String badWord;

		public MnemonicWordException(String badWord) {
			super();
			this.badWord = badWord;
		}
	}
}
