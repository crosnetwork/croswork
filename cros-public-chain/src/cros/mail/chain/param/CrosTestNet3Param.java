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
import java.util.Date;

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.core.Block;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.StoredDataBlock;
import cros.mail.chain.core.Utils;
import cros.mail.chain.core.VeriException;

import static com.google.common.base.Preconditions.checkState;

public class CrosTestNet3Param extends AbstractCrosNetParam {
	public CrosTestNet3Param() {
		super();
		id = ID_TESTNET;

		packetMagic = 0x0b110907;
		interval = INTERVAL;
		targetTimespan = TARGET_TIMESPAN;
		maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
		port = 18333;
		addressHeader = 111;
		p2shHeader = 196;
		acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
		dumpedPrivateKeyHeader = 239;
		genesisBlock.setTime(1296688602L);
		genesisBlock.setDifficultyTarget(0x1d00ffffL);
		genesisBlock.setNonce(414098458);
		spendableCoinbaseDepth = 100;
		subsidyDecreaseBlockCount = 210000;
		String genesisHash = genesisBlock.getHashAsString();
		checkState(genesisHash.equals("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943"));
		alertSigningKey = Utils.HEX.decode(
				"04302390343f91cc401d56d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a");

		dnsSeeds = new String[] { "testnet-seed.bitcoin.schildbach.de", "testnet-seed.bitcoin.petertodd.org" };
		addrSeeds = null;
		bip32HeaderPub = 0x043587CF;
		bip32HeaderPriv = 0x04358394;
	}

	private static CrosTestNet3Param instance;

	public static synchronized CrosTestNet3Param get() {
		if (instance == null) {
			instance = new CrosTestNet3Param();
		}
		return instance;
	}

	@Override
	public String getPaymentProtocolId() {
		return PAYMENT_PROTOCOL_ID_TESTNET;
	}

	private static final Date testnetDiffDate = new Date(1329264000000L);

	@Override
	public void checkDifficultyTransitions(final StoredDataBlock storedPrev, final Block nextBlock,
			final BlockData blockData) throws VeriException, BlockDataException {
		if (!isDifficultyTransitionPoint(storedPrev) && nextBlock.getTime().after(testnetDiffDate)) {
			Block prev = storedPrev.getHeader();

			final long timeDelta = nextBlock.getTimeSeconds() - prev.getTimeSeconds();

			if (timeDelta >= 0 && timeDelta <= NetworkParams.TARGET_SPACING * 2) {

				StoredDataBlock cursor = storedPrev;
				while (!cursor.getHeader().equals(getGenesisBlock()) && cursor.getHeight() % getInterval() != 0
						&& cursor.getHeader().getDifficultyTargetAsInteger().equals(getMaxTarget()))
					cursor = cursor.getPrev(blockData);
				BigInteger cursorTarget = cursor.getHeader().getDifficultyTargetAsInteger();
				BigInteger newTarget = nextBlock.getDifficultyTargetAsInteger();
				if (!cursorTarget.equals(newTarget))
					throw new VeriException("Testnet block transition that is not allowed: "
							+ Long.toHexString(cursor.getHeader().getDifficultyTarget()) + " vs "
							+ Long.toHexString(nextBlock.getDifficultyTarget()));
			}
		} else {
			super.checkDifficultyTransitions(storedPrev, nextBlock, blockData);
		}
	}
}
