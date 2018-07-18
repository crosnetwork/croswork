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


import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.protobuf.*;

import cros.mail.chain.blockdata.InvalidWalletException;
import cros.mail.chain.core.Address;
import cros.mail.chain.core.BloomFilter;
import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Utils;
import cros.mail.chain.encrypt.Child;
import cros.mail.chain.encrypt.DeterKey;
import cros.mail.chain.encrypt.HDUtils;
import cros.mail.chain.encrypt.KeyCrypt;
import cros.mail.chain.encrypt.LinuxSecureRandom;
import cros.mail.chain.misc.ListenerRegister;
import cros.mail.chain.misc.Threading;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptBuilder;

import org.slf4j.*;
import org.spongycastle.crypto.params.*;

import javax.annotation.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.*;

public class CrosKeyChainGroup implements KeyPackage {

	static {

		if (Utils.isAndroidRuntime())
			new LinuxSecureRandom();
	}

	private static final Logger log = LoggerFactory.getLogger(CrosKeyChainGroup.class);

	private BaseKeyChain basic;
	private NetworkParams params;
	protected final LinkedList<DeterKeyChain> chains;

	private final EnumMap<CrosKeyChain.KeyPurpose, DeterKey> currentKeys;
	private final EnumMap<CrosKeyChain.KeyPurpose, Address> currentAddresses;
	@Nullable
	private KeyCrypt keyCrypt;
	private int lookaheadSize = -1;
	private int lookaheadThreshold = -1;

	public CrosKeyChainGroup(NetworkParams params) {
		this(params, null, new ArrayList<DeterKeyChain>(1), null, null);
	}

	public CrosKeyChainGroup(NetworkParams params, DeterSeed seed) {
		this(params, null, ImmutableList.of(new DeterKeyChain(seed)), null, null);
	}

	public CrosKeyChainGroup(NetworkParams params, DeterKey watchKey) {
		this(params, null, ImmutableList.of(DeterKeyChain.watch(watchKey)), null, null);
	}

	public CrosKeyChainGroup(NetworkParams params, DeterKey watchKey, long creationTimeSecondsSecs) {
		this(params, null, ImmutableList.of(DeterKeyChain.watch(watchKey, creationTimeSecondsSecs)), null,
				null);
	}

	private CrosKeyChainGroup(NetworkParams params, @Nullable BaseKeyChain baseKeyChain,
			List<DeterKeyChain> chains, @Nullable EnumMap<CrosKeyChain.KeyPurpose, DeterKey> currentKeys,
			@Nullable KeyCrypt crypter) {
		this.params = params;
		this.basic = baseKeyChain == null ? new BaseKeyChain() : baseKeyChain;
		this.chains = new LinkedList<DeterKeyChain>(checkNotNull(chains));
		this.keyCrypt = crypter;
		this.currentKeys = currentKeys == null ? new EnumMap<CrosKeyChain.KeyPurpose, DeterKey>(CrosKeyChain.KeyPurpose.class)
				: currentKeys;
		this.currentAddresses = new EnumMap<CrosKeyChain.KeyPurpose, Address>(CrosKeyChain.KeyPurpose.class);
		maybeLookaheadScripts();

		if (isMarried()) {
			for (Map.Entry<CrosKeyChain.KeyPurpose, DeterKey> entry : this.currentKeys.entrySet()) {
				Address address = makeP2SHOutputScript(entry.getValue(), getActiveKeyChain()).getToAddress(params);
				currentAddresses.put(entry.getKey(), address);
			}
		}
	}

	private void maybeLookaheadScripts() {
		for (DeterKeyChain chain : chains) {
			chain.maybeLookAheadScripts();
		}
	}

	public void createAndActivateNewHDChain() {

		final DeterKeyChain chain = new DeterKeyChain(new SecureRandom());
		addAndActivateHDChain(chain);
	}

	public void addAndActivateHDChain(DeterKeyChain chain) {
		log.info("Creating and activating a new HD chain: {}", chain);
		for (ListenerRegister<CrosKeyChainListener> registration : basic.getListeners())
			chain.addEventListener(registration.listener, registration.executor);
		if (lookaheadSize >= 0)
			chain.setLookaheadSize(lookaheadSize);
		if (lookaheadThreshold >= 0)
			chain.setLookaheadThreshold(lookaheadThreshold);
		chains.add(chain);
	}

	public DeterKey currentKey(CrosKeyChain.KeyPurpose purpose) {
		DeterKeyChain chain = getActiveKeyChain();
		if (chain.isMarried()) {
			throw new UnsupportedOperationException("Key is not suitable to receive coins for married keychains."
					+ " Use freshAddress to get P2SH address instead");
		}
		DeterKey current = currentKeys.get(purpose);
		if (current == null) {
			current = freshKey(purpose);
			currentKeys.put(purpose, current);
		}
		return current;
	}

	public Address currentAddress(CrosKeyChain.KeyPurpose purpose) {
		DeterKeyChain chain = getActiveKeyChain();
		if (chain.isMarried()) {
			Address current = currentAddresses.get(purpose);
			if (current == null) {
				current = freshAddress(purpose);
				currentAddresses.put(purpose, current);
			}
			return current;
		} else {
			return currentKey(purpose).toAddress(params);
		}
	}

	public DeterKey freshKey(CrosKeyChain.KeyPurpose purpose) {
		return freshKeys(purpose, 1).get(0);
	}

	public List<DeterKey> freshKeys(CrosKeyChain.KeyPurpose purpose, int numberOfKeys) {
		DeterKeyChain chain = getActiveKeyChain();
		if (chain.isMarried()) {
			throw new UnsupportedOperationException("Key is not suitable to receive coins for married keychains."
					+ " Use freshAddress to get P2SH address instead");
		}
		return chain.getKeys(purpose, numberOfKeys);
	}

	public Address freshAddress(CrosKeyChain.KeyPurpose purpose) {
		DeterKeyChain chain = getActiveKeyChain();
		if (chain.isMarried()) {
			ChainScript outputScript = chain.freshOutputScript(purpose);
			checkState(outputScript.isPayToScriptHash());
			Address freshAddress = Address.fromP2SHScript(params, outputScript);
			maybeLookaheadScripts();
			currentAddresses.put(purpose, freshAddress);
			return freshAddress;
		} else {
			return freshKey(purpose).toAddress(params);
		}
	}

	public DeterKeyChain getActiveKeyChain() {
		if (chains.isEmpty()) {
			if (basic.numKeys() > 0) {
				log.warn("No HD chain present but random keys are: you probably deserialized an old wallet.");

				throw new DeterUpgradeException();
			}

			createAndActivateNewHDChain();
		}
		return chains.get(chains.size() - 1);
	}

	public void setLookaheadSize(int lookaheadSize) {
		this.lookaheadSize = lookaheadSize;
		for (DeterKeyChain chain : chains) {
			chain.setLookaheadSize(lookaheadSize);
		}
	}

	public int getLookaheadSize() {
		if (lookaheadSize == -1)
			return getActiveKeyChain().getLookaheadSize();
		else
			return lookaheadSize;
	}

	public void setLookaheadThreshold(int num) {
		for (DeterKeyChain chain : chains) {
			chain.setLookaheadThreshold(num);
		}
	}

	public int getLookaheadThreshold() {
		if (lookaheadThreshold == -1)
			return getActiveKeyChain().getLookaheadThreshold();
		else
			return lookaheadThreshold;
	}

	public int importKeys(List<ECKey> keys) {
		return basic.importKeys(keys);
	}

	public int importKeys(ECKey... keys) {
		return importKeys(ImmutableList.copyOf(keys));
	}

	public boolean checkPassword(CharSequence password) {
		checkState(keyCrypt != null, "Not encrypted");
		return checkAESKey(keyCrypt.deriveKey(password));
	}

	public boolean checkAESKey(KeyParameter aesKey) {
		checkState(keyCrypt != null, "Not encrypted");
		if (basic.numKeys() > 0)
			return basic.checkAESKey(aesKey);
		return getActiveKeyChain().checkAESKey(aesKey);
	}

	public int importKeysAndEncrypt(final List<ECKey> keys, KeyParameter aesKey) {

		checkState(keyCrypt != null, "Not encrypted");
		LinkedList<ECKey> encryptedKeys = Lists.newLinkedList();
		for (ECKey key : keys) {
			if (key.isEncrypted())
				throw new IllegalArgumentException("Cannot provide already encrypted keys");
			encryptedKeys.add(key.encrypt(keyCrypt, aesKey));
		}
		return importKeys(encryptedKeys);
	}

	@Override
	@Nullable
	public RedeemBlockData findRedeemDataFromScriptHash(byte[] scriptHash) {

		for (Iterator<DeterKeyChain> iter = chains.descendingIterator(); iter.hasNext();) {
			DeterKeyChain chain = iter.next();
			RedeemBlockData redeemBlockData = chain.findRedeemDataByScriptHash(ByteString.copyFrom(scriptHash));
			if (redeemBlockData != null)
				return redeemBlockData;
		}
		return null;
	}

	public void markP2SHAddressAsUsed(Address address) {
		checkState(isMarried());
		checkArgument(address.isP2SHAddress());
		RedeemBlockData data = findRedeemDataFromScriptHash(address.getHash160());
		if (data == null)
			return;
		for (ECKey key : data.keys) {
			for (DeterKeyChain chain : chains) {
				DeterKey k = chain.findKeyFromPubKey(key.getPubKey());
				if (k == null)
					continue;
				chain.markKeyAsUsed(k);
				maybeMarkCurrentAddressAsUsed(address);
			}
		}
	}

	@Nullable
	@Override
	public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
		ECKey result;
		if ((result = basic.findKeyFromPubHash(pubkeyHash)) != null)
			return result;
		for (DeterKeyChain chain : chains) {
			if ((result = chain.findKeyFromPubHash(pubkeyHash)) != null)
				return result;
		}
		return null;
	}

	public void markPubKeyHashAsUsed(byte[] pubkeyHash) {
		for (DeterKeyChain chain : chains) {
			DeterKey key;
			if ((key = chain.markPubHashAsUsed(pubkeyHash)) != null) {
				maybeMarkCurrentKeyAsUsed(key);
				return;
			}
		}
	}

	private void maybeMarkCurrentAddressAsUsed(Address address) {
		checkState(isMarried());
		checkArgument(address.isP2SHAddress());
		for (Map.Entry<CrosKeyChain.KeyPurpose, Address> entry : currentAddresses.entrySet()) {
			if (entry.getValue() != null && entry.getValue().equals(address)) {
				log.info("Marking P2SH address as used: {}", address);
				currentAddresses.put(entry.getKey(), freshAddress(entry.getKey()));
				return;
			}
		}
	}

	private void maybeMarkCurrentKeyAsUsed(DeterKey key) {

		for (Map.Entry<CrosKeyChain.KeyPurpose, DeterKey> entry : currentKeys.entrySet()) {
			if (entry.getValue() != null && entry.getValue().equals(key)) {
				log.info("Marking key as used: {}", key);
				currentKeys.put(entry.getKey(), freshKey(entry.getKey()));
				return;
			}
		}
	}

	public boolean hasKey(ECKey key) {
		if (basic.hasKey(key))
			return true;
		for (DeterKeyChain chain : chains)
			if (chain.hasKey(key))
				return true;
		return false;
	}

	@Nullable
	@Override
	public ECKey findKeyFromPubKey(byte[] pubkey) {
		ECKey result;
		if ((result = basic.findKeyFromPubKey(pubkey)) != null)
			return result;
		for (DeterKeyChain chain : chains) {
			if ((result = chain.findKeyFromPubKey(pubkey)) != null)
				return result;
		}
		return null;
	}

	public void markPubKeyAsUsed(byte[] pubkey) {
		for (DeterKeyChain chain : chains) {
			DeterKey key;
			if ((key = chain.markPubKeyAsUsed(pubkey)) != null) {
				maybeMarkCurrentKeyAsUsed(key);
				return;
			}
		}
	}

	public int numKeys() {
		int result = basic.numKeys();
		for (DeterKeyChain chain : chains)
			result += chain.numKeys();
		return result;
	}

	public boolean removeImportedKey(ECKey key) {
		checkNotNull(key);
		checkArgument(!(key instanceof DeterKey));
		return basic.removeKey(key);
	}

	public boolean isMarried() {
		return !chains.isEmpty() && getActiveKeyChain().isMarried();
	}

	public void encrypt(KeyCrypt keyCrypt, KeyParameter aesKey) {
		checkNotNull(keyCrypt);
		checkNotNull(aesKey);

		BaseKeyChain newBasic = basic.toEncrypted(keyCrypt, aesKey);
		List<DeterKeyChain> newChains = new ArrayList<DeterKeyChain>(chains.size());
		if (chains.isEmpty() && basic.numKeys() == 0) {

			createAndActivateNewHDChain();
		}
		for (DeterKeyChain chain : chains)
			newChains.add(chain.toEncrypted(keyCrypt, aesKey));
		this.keyCrypt = keyCrypt;
		basic = newBasic;
		chains.clear();
		chains.addAll(newChains);
	}

	public void decrypt(KeyParameter aesKey) {

		checkNotNull(aesKey);
		BaseKeyChain newBasic = basic.toDecrypted(aesKey);
		List<DeterKeyChain> newChains = new ArrayList<DeterKeyChain>(chains.size());
		for (DeterKeyChain chain : chains)
			newChains.add(chain.toDecrypted(aesKey));

		this.keyCrypt = null;
		basic = newBasic;
		chains.clear();
		chains.addAll(newChains);
	}

	public boolean isEncrypted() {
		return keyCrypt != null;
	}

	public boolean isWatching() {
		BaseKeyChain.State basicState = basic.isWatching();
		BaseKeyChain.State activeState = BaseKeyChain.State.EMPTY;
		if (!chains.isEmpty()) {
			if (getActiveKeyChain().isWatching())
				activeState = BaseKeyChain.State.WATCHING;
			else
				activeState = BaseKeyChain.State.REGULAR;
		}
		if (basicState == BaseKeyChain.State.EMPTY) {
			if (activeState == BaseKeyChain.State.EMPTY)
				throw new IllegalStateException("Empty key chain group: cannot answer isWatching() query");
			return activeState == BaseKeyChain.State.WATCHING;
		} else if (activeState == BaseKeyChain.State.EMPTY)
			return basicState == BaseKeyChain.State.WATCHING;
		else {
			if (activeState != basicState)
				throw new IllegalStateException("Mix of watching and non-watching keys in wallet");
			return activeState == BaseKeyChain.State.WATCHING;
		}
	}

	@Nullable
	public KeyCrypt getKeyCrypter() {
		return keyCrypt;
	}

	public List<ECKey> getImportedKeys() {
		return basic.getKeys();
	}

	public long getEarliestKeyCreationTime() {
		long time = basic.getEarliestKeyCreationTime();
		for (DeterKeyChain chain : chains)
			time = Math.min(time, chain.getEarliestKeyCreationTime());
		return time;
	}

	public int getBloomFilterElementCount() {
		int result = basic.numBloomFilterEntries();
		for (DeterKeyChain chain : chains) {
			result += chain.numBloomFilterEntries();
		}
		return result;
	}

	public BloomFilter getBloomFilter(int size, double falsePositiveRate, long nTweak) {
		BloomFilter filter = new BloomFilter(size, falsePositiveRate, nTweak);
		if (basic.numKeys() > 0)
			filter.merge(basic.getFilter(size, falsePositiveRate, nTweak));

		for (DeterKeyChain chain : chains) {
			filter.merge(chain.getFilter(size, falsePositiveRate, nTweak));
		}
		return filter;
	}

	public boolean isRequiringUpdateAllBloomFilter() {
		throw new UnsupportedOperationException();
	}

	private ChainScript makeP2SHOutputScript(DeterKey followedKey, DeterKeyChain chain) {
		return ChainScriptBuilder.createP2SHOutputScript(chain.getRedeemData(followedKey).redeemScript);
	}

	public void addEventListener(CrosKeyChainListener listener) {
		addEventListener(listener, Threading.USER_THREAD);
	}

	public void addEventListener(CrosKeyChainListener listener, Executor executor) {
		checkNotNull(listener);
		checkNotNull(executor);
		basic.addEventListener(listener, executor);
		for (DeterKeyChain chain : chains)
			chain.addEventListener(listener, executor);
	}

	public boolean removeEventListener(CrosKeyChainListener listener) {
		checkNotNull(listener);
		for (DeterKeyChain chain : chains)
			chain.removeEventListener(listener);
		return basic.removeEventListener(listener);
	}

	public List<Protos.Key> serializeToProtobuf() {
		List<Protos.Key> result;
		if (basic != null)
			result = basic.serializeToProtobuf();
		else
			result = Lists.newArrayList();
		for (DeterKeyChain chain : chains) {
			List<Protos.Key> protos = chain.serializeToProtobuf();
			result.addAll(protos);
		}
		return result;
	}

	static CrosKeyChainGroup fromProtobufUnencrypted(NetworkParams params, List<Protos.Key> keys)
			throws InvalidWalletException {
		return fromProtobufUnencrypted(params, keys, new DefaultBaseKeyChainFactory());
	}

	public static CrosKeyChainGroup fromProtobufUnencrypted(NetworkParams params, List<Protos.Key> keys,
			CrosKeyChainFactory factory) throws InvalidWalletException {
		BaseKeyChain baseKeyChain = BaseKeyChain.fromProtobufUnencrypted(keys);
		List<DeterKeyChain> chains = DeterKeyChain.fromProtobuf(keys, null, factory);
		EnumMap<CrosKeyChain.KeyPurpose, DeterKey> currentKeys = null;
		if (!chains.isEmpty())
			currentKeys = createCurrentKeysMap(chains);
		extractFollowingKeychains(chains);
		return new CrosKeyChainGroup(params, baseKeyChain, chains, currentKeys, null);
	}

	static CrosKeyChainGroup fromProtobufEncrypted(NetworkParams params, List<Protos.Key> keys, KeyCrypt crypter)
			throws InvalidWalletException {
		return fromProtobufEncrypted(params, keys, crypter, new DefaultBaseKeyChainFactory());
	}

	public static CrosKeyChainGroup fromProtobufEncrypted(NetworkParams params, List<Protos.Key> keys, KeyCrypt crypter,
			CrosKeyChainFactory factory) throws InvalidWalletException {
		checkNotNull(crypter);
		BaseKeyChain baseKeyChain = BaseKeyChain.fromProtobufEncrypted(keys, crypter);
		List<DeterKeyChain> chains = DeterKeyChain.fromProtobuf(keys, crypter, factory);
		EnumMap<CrosKeyChain.KeyPurpose, DeterKey> currentKeys = null;
		if (!chains.isEmpty())
			currentKeys = createCurrentKeysMap(chains);
		extractFollowingKeychains(chains);
		return new CrosKeyChainGroup(params, baseKeyChain, chains, currentKeys, crypter);
	}

	public DeterKeyChain upgradeToDeterministic(long keyRotationTimeSecs, @Nullable KeyParameter aesKey)
			throws DeterUpgradePassword, TotalRandomKeysRotating {
		checkState(basic.numKeys() > 0);
		checkArgument(keyRotationTimeSecs >= 0);

		ECKey keyToUse = basic.findOldestKeyAfter(keyRotationTimeSecs - 1);
		if (keyToUse == null)
			throw new TotalRandomKeysRotating();

		if (keyToUse.isEncrypted()) {
			if (aesKey == null) {

				throw new DeterUpgradePassword();
			}
			keyToUse = keyToUse.decrypt(aesKey);
		} else if (aesKey != null) {
			throw new IllegalStateException("AES Key was provided but wallet is not encrypted.");
		}

		if (chains.isEmpty()) {
			log.info("Auto-upgrading pre-HD wallet to HD!");
		} else {
			log.info("Wallet with existing HD chain is being re-upgraded due to change in key rotation time.");
		}
		log.info("Instantiating new HD chain using oldest non-rotating private key (address: {})",
				keyToUse.toAddress(params));
		byte[] entropy = checkNotNull(keyToUse.getSecretBytes());

		checkState(entropy.length >= DeterSeed.DEFAULT_SEED_ENTROPY_BITS / 8);

		entropy = Arrays.copyOfRange(entropy, 0, DeterSeed.DEFAULT_SEED_ENTROPY_BITS / 8);
		checkState(entropy.length == DeterSeed.DEFAULT_SEED_ENTROPY_BITS / 8);
		String passphrase = "";
		DeterKeyChain chain = new DeterKeyChain(entropy, passphrase, keyToUse.getCreationTimeSeconds());
		if (aesKey != null) {
			chain = chain.toEncrypted(checkNotNull(basic.getKeyCrypter()), aesKey);
		}
		chains.add(chain);
		return chain;
	}

	public boolean isDeterministicUpgradeRequired() {
		return basic.numKeys() > 0 && chains.isEmpty();
	}

	private static EnumMap<CrosKeyChain.KeyPurpose, DeterKey> createCurrentKeysMap(List<DeterKeyChain> chains) {
		DeterKeyChain activeChain = chains.get(chains.size() - 1);

		EnumMap<CrosKeyChain.KeyPurpose, DeterKey> currentKeys = new EnumMap<CrosKeyChain.KeyPurpose, DeterKey>(
				CrosKeyChain.KeyPurpose.class);

		if (activeChain.getIssuedExternalKeys() > 0) {
			DeterKey currentExternalKey = activeChain.getKeyByPath(
					HDUtils.append(HDUtils.concat(activeChain.getAccountPath(), DeterKeyChain.EXTERNAL_SUBPATH),
							new Child(activeChain.getIssuedExternalKeys() - 1)));
			currentKeys.put(CrosKeyChain.KeyPurpose.RECEIVE_FUNDS, currentExternalKey);
		}

		if (activeChain.getIssuedInternalKeys() > 0) {
			DeterKey currentInternalKey = activeChain.getKeyByPath(
					HDUtils.append(HDUtils.concat(activeChain.getAccountPath(), DeterKeyChain.INTERNAL_SUBPATH),
							new Child(activeChain.getIssuedInternalKeys() - 1)));
			currentKeys.put(CrosKeyChain.KeyPurpose.CHANGE, currentInternalKey);
		}
		return currentKeys;
	}

	private static void extractFollowingKeychains(List<DeterKeyChain> chains) {

		List<DeterKeyChain> followingChains = Lists.newArrayList();
		for (Iterator<DeterKeyChain> it = chains.iterator(); it.hasNext();) {
			DeterKeyChain chain = it.next();
			if (chain.isFollowing()) {
				followingChains.add(chain);
				it.remove();
			} else if (!followingChains.isEmpty()) {
				if (!(chain instanceof CoupledKeyChain))
					throw new IllegalStateException();
				((CoupledKeyChain) chain).setFollowingKeyChains(followingChains);
				followingChains = Lists.newArrayList();
			}
		}
	}

	public String toString(boolean includePrivateKeys) {
		final StringBuilder builder = new StringBuilder();
		if (basic != null) {
			List<ECKey> keys = basic.getKeys();
			Collections.sort(keys, ECKey.AGE_COMPARATOR);
			for (ECKey key : keys)
				key.formatKeyWithAddress(includePrivateKeys, builder, params);
		}
		List<String> chainStrs = Lists.newLinkedList();
		for (DeterKeyChain chain : chains) {
			chainStrs.add(chain.toString(includePrivateKeys, params));
		}
		builder.append(Joiner.on(String.format(Locale.US, "%n")).join(chainStrs));
		return builder.toString();
	}

	public List<DeterKeyChain> getDeterministicKeyChains() {
		return new ArrayList<DeterKeyChain>(chains);
	}

	public int getCombinedKeyLookaheadEpochs() {
		int epoch = 0;
		for (DeterKeyChain chain : chains)
			epoch += chain.getKeyLookaheadEpoch();
		return epoch;
	}
}
