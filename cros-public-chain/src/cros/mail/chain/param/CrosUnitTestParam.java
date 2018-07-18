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

import cros.mail.chain.core.Address;
import cros.mail.chain.core.Block;
import cros.mail.chain.core.ECKey;


public class CrosUnitTestParam extends AbstractCrosNetParam {

	public static ECKey TEST_KEY = new ECKey();
	public static Address TEST_ADDRESS;

	public CrosUnitTestParam() {
		super();
		id = ID_UNITTESTNET;
		packetMagic = 0x0b110907;
		addressHeader = 111;
		p2shHeader = 196;
		acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
		maxTarget = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
		genesisBlock.setTime(System.currentTimeMillis() / 1000);
		genesisBlock.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
		genesisBlock.solve();
		port = 18333;
		interval = 10;
		dumpedPrivateKeyHeader = 239;
		targetTimespan = 200000000;
		spendableCoinbaseDepth = 5;
		subsidyDecreaseBlockCount = 100;
		dnsSeeds = null;
		addrSeeds = null;
		bip32HeaderPub = 0x043587CF;
		bip32HeaderPriv = 0x04358394;
	}

	private static CrosUnitTestParam instance;

	public static synchronized CrosUnitTestParam get() {
		if (instance == null) {
			instance = new CrosUnitTestParam();
			TEST_ADDRESS = TEST_KEY.toAddress(instance);
		}
		return instance;
	}

	@Override
	public String getPaymentProtocolId() {
		return "unittest";
	}
}
