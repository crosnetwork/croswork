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


import com.google.common.base.Objects;

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.misc.MoneyFormat;
import cros.mail.chain.netWork.discovery.HttpServerDiscovery;
import cros.mail.chain.param.CrosMainNetParam;
import cros.mail.chain.param.CrosTestNet3Param;
import cros.mail.chain.param.CrosTestNetParam;
import cros.mail.chain.param.CrosTestParam;
import cros.mail.chain.param.CrosUnitTestParam;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptCodes;

import javax.annotation.*;


import java.io.*;
import java.math.*;
import java.util.*;
import static cros.mail.chain.core.Coin.*;

public abstract class NetworkParams implements Serializable {

	public static final int PROTOCOL_VERSION = 70001;

	public static final byte[] SATOSHI_KEY = Utils.HEX.decode(
			"04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");

	public static final String ID_MAINNET = "org.bitcoin.production";

	public static final String ID_TESTNET = "org.bitcoin.test";

	public static final String ID_REGTEST = "org.bitcoin.regtest";

	public static final String ID_UNITTESTNET = "org.bitcoinj.unittest";

	public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";

	public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";

	public static final String PAYMENT_PROTOCOL_ID_UNIT_TESTS = "unittest";
	public static final String PAYMENT_PROTOCOL_ID_REGTEST = "regtest";

	protected Block genesisBlock;
	protected BigInteger maxTarget;
	protected int port;
	protected long packetMagic;
	protected int addressHeader;
	protected int p2shHeader;
	protected int dumpedPrivateKeyHeader;
	protected int interval;
	protected int targetTimespan;
	protected byte[] alertSigningKey;
	protected int bip32HeaderPub;
	protected int bip32HeaderPriv;

	protected String id;

	protected int spendableCoinbaseDepth;
	protected int subsidyDecreaseBlockCount;

	protected int[] acceptableAddressCodes;
	protected String[] dnsSeeds;
	protected int[] addrSeeds;
	protected HttpServerDiscovery.Details[] httpSeeds = {};
	protected Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();

	protected NetworkParams() {
		alertSigningKey = SATOSHI_KEY;
		genesisBlock = createGenesis(this);
	}

	private static Block createGenesis(NetworkParams n) {
		Block genesisBlock = new Block(n);
		Transaction t = new Transaction(n);
		try {

			byte[] bytes = Utils.HEX.decode(
					"04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
			t.addInput(new TxInput(n, t, bytes));
			ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
			ChainScript.writeBytes(scriptPubKeyBytes, Utils.HEX.decode(
					"04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
			scriptPubKeyBytes.write(ChainScriptCodes.OP_CHECKSIG);
			t.addOutput(new TxOutput(n, t, FIFTY_COINS, scriptPubKeyBytes.toByteArray()));
		} catch (Exception e) {

			throw new RuntimeException(e);
		}
		genesisBlock.addTransaction(t);
		return genesisBlock;
	}

	public static final int TARGET_TIMESPAN = 14 * 24 * 60 * 60;
	public static final int TARGET_SPACING = 10 * 60;
	public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;

	public static final int BIP16_ENFORCE_TIME = 1333238400;

	public static final long MAX_COINS = 21000000;

	public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);

	@Deprecated
	public static NetworkParams testNet() {
		return CrosTestNet3Param.get();
	}

	@Deprecated
	public static NetworkParams testNet2() {
		return CrosTestNetParam.get();
	}

	@Deprecated
	public static NetworkParams testNet3() {
		return CrosTestNet3Param.get();
	}

	@Deprecated
	public static NetworkParams prodNet() {
		return CrosMainNetParam.get();
	}

	@Deprecated
	public static NetworkParams unitTests() {
		return CrosUnitTestParam.get();
	}

	@Deprecated
	public static NetworkParams regTests() {
		return CrosTestParam.get();
	}

	public String getId() {
		return id;
	}

	public abstract String getPaymentProtocolId();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NetworkParams other = (NetworkParams) o;
		return getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Nullable
	public static NetworkParams fromID(String id) {
		if (id.equals(ID_MAINNET)) {
			return CrosMainNetParam.get();
		} else if (id.equals(ID_TESTNET)) {
			return CrosTestNet3Param.get();
		} else if (id.equals(ID_UNITTESTNET)) {
			return CrosUnitTestParam.get();
		} else if (id.equals(ID_REGTEST)) {
			return CrosTestParam.get();
		} else {
			return null;
		}
	}

	@Nullable
	public static NetworkParams fromPmtProtocolID(String pmtProtocolId) {
		if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
			return CrosMainNetParam.get();
		} else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
			return CrosTestNet3Param.get();
		} else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_UNIT_TESTS)) {
			return CrosUnitTestParam.get();
		} else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_REGTEST)) {
			return CrosTestParam.get();
		} else {
			return null;
		}
	}

	public int getSpendableCoinbaseDepth() {
		return spendableCoinbaseDepth;
	}

	public abstract void checkDifficultyTransitions(StoredDataBlock storedPrev, Block next, final BlockData blockData)
			throws VeriException, BlockDataException;

	public boolean passesCheckpoint(int height, Sha256Hash hash) {
		Sha256Hash checkpointHash = checkpoints.get(height);
		return checkpointHash == null || checkpointHash.equals(hash);
	}

	public boolean isCheckpoint(int height) {
		Sha256Hash checkpointHash = checkpoints.get(height);
		return checkpointHash != null;
	}

	public int getSubsidyDecreaseBlockCount() {
		return subsidyDecreaseBlockCount;
	}

	public String[] getDnsSeeds() {
		return dnsSeeds;
	}

	public int[] getAddrSeeds() {
		return addrSeeds;
	}

	public HttpServerDiscovery.Details[] getHttpSeeds() {
		return httpSeeds;
	}

	public Block getGenesisBlock() {
		return genesisBlock;
	}

	public int getPort() {
		return port;
	}

	public long getPacketMagic() {
		return packetMagic;
	}

	public int getAddressHeader() {
		return addressHeader;
	}

	public int getP2SHHeader() {
		return p2shHeader;
	}

	public int getDumpedPrivateKeyHeader() {
		return dumpedPrivateKeyHeader;
	}

	public int getTargetTimespan() {
		return targetTimespan;
	}

	public int[] getAcceptableAddressCodes() {
		return acceptableAddressCodes;
	}

	public boolean allowEmptyPeerChain() {
		return true;
	}

	public int getInterval() {
		return interval;
	}

	public BigInteger getMaxTarget() {
		return maxTarget;
	}

	public byte[] getAlertSigningKey() {
		return alertSigningKey;
	}

	public int getBip32HeaderPub() {
		return bip32HeaderPub;
	}

	public int getBip32HeaderPriv() {
		return bip32HeaderPriv;
	}

	public abstract Coin getMaxMoney();

	public abstract Coin getMinNonDustOutput();

	public abstract MoneyFormat getMonetaryFormat();

	public abstract String getUriScheme();

	public abstract boolean hasMaxMoney();
}
