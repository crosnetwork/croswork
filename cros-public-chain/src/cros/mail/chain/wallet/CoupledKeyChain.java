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
package cros.mail.chain.wallet;

import cros.mail.chain.core.BloomFilter;
import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Utils;
import cros.mail.chain.encrypt.DeterKey;
import cros.mail.chain.encrypt.KeyCrypt;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

public class CoupledKeyChain extends DeterKeyChain {

	private LinkedHashMap<ByteString, RedeemBlockData> marriedKeysRedeemData = new LinkedHashMap<ByteString, RedeemBlockData>();

	private List<DeterKeyChain> followingKeyChains;

	public static class Builder<T extends Builder<T>> extends DeterKeyChain.Builder<T> {
		private List<DeterKey> followingKeys;
		private int threshold;

		protected Builder() {
		}

		public T followingKeys(List<DeterKey> followingKeys) {
			this.followingKeys = followingKeys;
			return self();
		}

		public T followingKeys(DeterKey followingKey, DeterKey... followingKeys) {
			this.followingKeys = Lists.asList(followingKey, followingKeys);
			return self();
		}

		public T threshold(int threshold) {
			this.threshold = threshold;
			return self();
		}

		@Override
		public CoupledKeyChain build() {
			checkState(random != null || entropy != null || seed != null || watchingKey != null,
					"Must provide either entropy or random or seed or watchingKey");
			checkNotNull(followingKeys, "followingKeys must be provided");
			CoupledKeyChain chain;
			if (threshold == 0)
				threshold = (followingKeys.size() + 1) / 2 + 1;
			if (random != null) {
				chain = new CoupledKeyChain(random, bits, getPassphrase(), seedCreationTimeSecs);
			} else if (entropy != null) {
				chain = new CoupledKeyChain(entropy, getPassphrase(), seedCreationTimeSecs);
			} else if (seed != null) {
				chain = new CoupledKeyChain(seed);
			} else {
				chain = new CoupledKeyChain(watchingKey, seedCreationTimeSecs);
			}
			chain.addFollowingAccountKeys(followingKeys, threshold);
			return chain;
		}
	}

	public static Builder<?> builder() {
		return new Builder();
	}

	CoupledKeyChain(DeterKey accountKey) {
		super(accountKey, false);
	}

	CoupledKeyChain(DeterKey accountKey, long seedCreationTimeSecs) {
		super(accountKey, seedCreationTimeSecs);
	}

	CoupledKeyChain(DeterSeed seed, KeyCrypt crypter) {
		super(seed, crypter);
	}

	private CoupledKeyChain(SecureRandom random, int bits, String passphrase, long seedCreationTimeSecs) {
		super(random, bits, passphrase, seedCreationTimeSecs);
	}

	private CoupledKeyChain(byte[] entropy, String passphrase, long seedCreationTimeSecs) {
		super(entropy, passphrase, seedCreationTimeSecs);
	}

	private CoupledKeyChain(DeterSeed seed) {
		super(seed);
	}

	void setFollowingKeyChains(List<DeterKeyChain> followingKeyChains) {
		checkArgument(!followingKeyChains.isEmpty());
		this.followingKeyChains = followingKeyChains;
	}

	@Override
	public boolean isMarried() {
		return true;
	}

	@Override
	public ChainScript freshOutputScript(KeyPurpose purpose) {
		DeterKey followedKey = getKey(purpose);
		ImmutableList.Builder<ECKey> keys = ImmutableList.<ECKey>builder().add(followedKey);
		for (DeterKeyChain keyChain : followingKeyChains) {
			DeterKey followingKey = keyChain.getKey(purpose);
			checkState(followedKey.getChildNumber().equals(followingKey.getChildNumber()),
					"Following keychains should be in sync");
			keys.add(followingKey);
		}
		List<ECKey> marriedKeys = keys.build();
		ChainScript redeemScript = ChainScriptBuilder.createRedeemScript(sigsRequiredToSpend, marriedKeys);
		return ChainScriptBuilder.createP2SHOutputScript(redeemScript);
	}

	private List<ECKey> getMarriedKeysWithFollowed(DeterKey followedKey) {
		ImmutableList.Builder<ECKey> keys = ImmutableList.builder();
		for (DeterKeyChain keyChain : followingKeyChains) {
			keyChain.maybeLookAhead();
			keys.add(keyChain.getKeyByPath(followedKey.getPath()));
		}
		keys.add(followedKey);
		return keys.build();
	}

	@Override
	public RedeemBlockData getRedeemData(DeterKey followedKey) {
		checkState(isMarried());
		List<ECKey> marriedKeys = getMarriedKeysWithFollowed(followedKey);
		ChainScript redeemScript = ChainScriptBuilder.createRedeemScript(sigsRequiredToSpend, marriedKeys);
		return RedeemBlockData.of(marriedKeys, redeemScript);
	}

	private void addFollowingAccountKeys(List<DeterKey> followingAccountKeys, int sigsRequiredToSpend) {
		checkArgument(sigsRequiredToSpend <= followingAccountKeys.size() + 1,
				"Multisig threshold can't exceed total number of keys");
		checkState(numLeafKeysIssued() == 0, "Active keychain already has keys in use");
		checkState(followingKeyChains == null);

		List<DeterKeyChain> followingKeyChains = Lists.newArrayList();

		for (DeterKey key : followingAccountKeys) {
			checkArgument(key.getPath().size() == getAccountPath().size(), "Following keys have to be account keys");
			DeterKeyChain chain = DeterKeyChain.watchAndFollow(key);
			if (lookaheadSize >= 0)
				chain.setLookaheadSize(lookaheadSize);
			if (lookaheadThreshold >= 0)
				chain.setLookaheadThreshold(lookaheadThreshold);
			followingKeyChains.add(chain);
		}

		this.sigsRequiredToSpend = sigsRequiredToSpend;
		this.followingKeyChains = followingKeyChains;
	}

	@Override
	public void setLookaheadSize(int lookaheadSize) {
		lock.lock();
		try {
			super.setLookaheadSize(lookaheadSize);
			if (followingKeyChains != null) {
				for (DeterKeyChain followingChain : followingKeyChains) {
					followingChain.setLookaheadSize(lookaheadSize);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<Protos.Key> serializeToProtobuf() {
		List<Protos.Key> result = newArrayList();
		lock.lock();
		try {
			for (DeterKeyChain chain : followingKeyChains) {
				result.addAll(chain.serializeMyselfToProtobuf());
			}
			result.addAll(serializeMyselfToProtobuf());
		} finally {
			lock.unlock();
		}
		return result;
	}

	@Override
	protected void formatAddresses(boolean includePrivateKeys, NetworkParams params, StringBuilder builder2) {
		for (DeterKeyChain followingChain : followingKeyChains) {
			builder2.append(String.format(Locale.US, "Following chain:  %s%n",
					followingChain.getWatchingKey().serializePubB58(params)));
		}
		builder2.append(String.format(Locale.US, "%n"));
		for (RedeemBlockData redeemBlockData : marriedKeysRedeemData.values())
			formatScript(ChainScriptBuilder.createP2SHOutputScript(redeemBlockData.redeemScript), builder2, params);
	}

	private void formatScript(ChainScript chainScript, StringBuilder builder, NetworkParams params) {
		builder.append("  addr:");
		builder.append(chainScript.getToAddress(params));
		builder.append("  hash160:");
		builder.append(Utils.HEX.encode(chainScript.getPubKeyHash()));
		if (chainScript.getCreationTimeSeconds() > 0)
			builder.append("  creationTimeSeconds:").append(chainScript.getCreationTimeSeconds());
		builder.append("\n");
	}

	@Override
	public void maybeLookAheadScripts() {
		super.maybeLookAheadScripts();
		int numLeafKeys = getLeafKeys().size();

		checkState(marriedKeysRedeemData.size() <= numLeafKeys,
				"Number of scripts is greater than number of leaf keys");
		if (marriedKeysRedeemData.size() == numLeafKeys)
			return;

		maybeLookAhead();
		for (DeterKey followedKey : getLeafKeys()) {
			RedeemBlockData redeemBlockData = getRedeemData(followedKey);
			ChainScript scriptPubKey = ChainScriptBuilder.createP2SHOutputScript(redeemBlockData.redeemScript);
			marriedKeysRedeemData.put(ByteString.copyFrom(scriptPubKey.getPubKeyHash()), redeemBlockData);
		}
	}

	@Nullable
	@Override
	public RedeemBlockData findRedeemDataByScriptHash(ByteString bytes) {
		return marriedKeysRedeemData.get(bytes);
	}

	@Override
	public BloomFilter getFilter(int size, double falsePositiveRate, long tweak) {
		lock.lock();
		BloomFilter filter;
		try {
			filter = new BloomFilter(size, falsePositiveRate, tweak);
			for (Map.Entry<ByteString, RedeemBlockData> entry : marriedKeysRedeemData.entrySet()) {
				filter.insert(entry.getKey().toByteArray());
				filter.insert(entry.getValue().redeemScript.getProgram());
			}
		} finally {
			lock.unlock();
		}
		return filter;
	}

	@Override
	public int numBloomFilterEntries() {
		maybeLookAhead();
		return getLeafKeys().size() * 2;
	}
}
