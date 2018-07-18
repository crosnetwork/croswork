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

@SuppressWarnings("serial")
public class VeriException extends RuntimeException {
	public VeriException(String msg) {
		super(msg);
	}

	public VeriException(Exception e) {
		super(e);
	}

	public VeriException(String msg, Throwable t) {
		super(msg, t);
	}

	public static class EmptyInputsOrOutputs extends VeriException {
		public EmptyInputsOrOutputs() {
			super("Transaction had no inputs or no outputs.");
		}
	}

	public static class LargerThanMaxBlockSize extends VeriException {
		public LargerThanMaxBlockSize() {
			super("Transaction larger than MAX_BLOCK_SIZE");
		}
	}

	public static class DuplicatedOutPoint extends VeriException {
		public DuplicatedOutPoint() {
			super("Duplicated outpoint");
		}
	}

	public static class NegativeValueOutput extends VeriException {
		public NegativeValueOutput() {
			super("Transaction output negative");
		}
	}

	public static class ExcessiveValue extends VeriException {
		public ExcessiveValue() {
			super("Total transaction output value greater than possible");
		}
	}

	public static class CoinbaseScriptSizeOutOfRange extends VeriException {
		public CoinbaseScriptSizeOutOfRange() {
			super("Coinbase script size out of range");
		}
	}

	public static class UnexpectedCoinbaseInput extends VeriException {
		public UnexpectedCoinbaseInput() {
			super("Coinbase input as input in non-coinbase transaction");
		}
	}
}
