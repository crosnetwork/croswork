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
package cros.mail.chain.param;

import java.math.BigInteger;

import cros.mail.chain.core.Block;

import static com.google.common.base.Preconditions.checkState;

public class CrosTestParam extends CrosTestNetParam {
	private static final BigInteger MAX_TARGET = new BigInteger(
			"7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

	public CrosTestParam() {
		super();
		interval = 10000;
		maxTarget = MAX_TARGET;
		subsidyDecreaseBlockCount = 150;
		port = 18444;
		id = ID_REGTEST;
	}

	@Override
	public boolean allowEmptyPeerChain() {
		return true;
	}

	private static Block genesis;

	@Override
	public Block getGenesisBlock() {
		synchronized (CrosTestParam.class) {
			if (genesis == null) {
				genesis = super.getGenesisBlock();
				genesis.setNonce(2);
				genesis.setDifficultyTarget(0x207fFFFFL);
				genesis.setTime(1296688602L);
				checkState(genesis.getHashAsString().toLowerCase()
						.equals("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206"));
			}
			return genesis;
		}
	}

	private static CrosTestParam instance;

	public static synchronized CrosTestParam get() {
		if (instance == null) {
			instance = new CrosTestParam();
		}
		return instance;
	}

	@Override
	public String getPaymentProtocolId() {
		return PAYMENT_PROTOCOL_ID_REGTEST;
	}
}
