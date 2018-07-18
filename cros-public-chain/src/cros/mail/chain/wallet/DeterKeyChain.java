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

import cros.mail.chain.blockdata.InvalidWalletException;
import cros.mail.chain.core.BloomFilter;
import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Utils;
import cros.mail.chain.encrypt.*;
import cros.mail.chain.misc.Threading;
import cros.mail.chain.script.ChainScript;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

@SuppressWarnings("PublicStaticCollectionField")
public class DeterKeyChain implements EncryptKeyChain {
	private static final Logger log = LoggerFactory.getLogger(DeterKeyChain.class);
	public static final String DEFAULT_PASSPHRASE_FOR_MNEMONIC = "";

	protected final ReentrantLock lock = Threading.lock("DeterKeyChain");

	private DHierarchy hierarchy;
	@Nullable
	private DeterKey rootKey;
	@Nullable
	private DeterSeed seed;

	private long creationTimeSeconds = MnemonicCode.BIP39_STANDARDISATION_TIME_SECS;

	public static final ImmutableList<Child> ACCOUNT_ZERO_PATH = ImmutableList.of(Child.ZERO_HARDENED);
	public static final ImmutableList<Child> EXTERNAL_SUBPATH = ImmutableList.of(Child.ZERO);
	public static final ImmutableList<Child> INTERNAL_SUBPATH = ImmutableList.of(Child.ONE);
	public static final ImmutableList<Child> EXTERNAL_PATH = HDUtils.concat(ACCOUNT_ZERO_PATH, EXTERNAL_SUBPATH);
	public static final ImmutableList<Child> INTERNAL_PATH = HDUtils.concat(ACCOUNT_ZERO_PATH, INTERNAL_SUBPATH);

	public static final ImmutableList<Child> BIP44_ACCOUNT_ZERO_PATH = ImmutableList.of(new Child(44, true),
			Child.ZERO_HARDENED, Child.ZERO_HARDENED);

	private static final int LAZY_CALCULATE_LOOKAHEAD = -1;
	protected int lookaheadSize = 100;

	protected int lookaheadThreshold = calcDefaultLookaheadThreshold();

	private int calcDefaultLookaheadThreshold() {
		return lookaheadSize / 3;
	}

	private DeterKey externalKey, internalKey;

	private int issuedExternalKeys, issuedInternalKeys;

	private int keyLookaheadEpoch;

	private final BaseKeyChain baseKeyChain;

	private boolean isFollowing;

	protected int sigsRequiredToSpend = 1;

	public static class Builder<T extends Builder<T>> {
		protected SecureRandom random;
		protected int bits = 128;
		protected String passphrase;
		protected long seedCreationTimeSecs;
		protected byte[] entropy;
		protected DeterSeed seed;
		protected DeterKey watchingKey;

		protected Builder() {
		}

		@SuppressWarnings("unchecked")
		protected T self() {
			return (T) this;
		}

		public T entropy(byte[] entropy) {
			this.entropy = entropy;
			return self();
		}

		public T seed(DeterSeed seed) {
			this.seed = seed;
			return self();
		}

		public T random(SecureRandom random, int bits) {
			this.random = random;
			this.bits = bits;
			return self();
		}

		public T random(SecureRandom random) {
			this.random = random;
			return self();
		}

		public T watchingKey(DeterKey watchingKey) {
			this.watchingKey = watchingKey;
			return self();
		}

		public T seedCreationTimeSecs(long seedCreationTimeSecs) {
			this.seedCreationTimeSecs = seedCreationTimeSecs;
			return self();
		}

		public T passphrase(String passphrase) {

			this.passphrase = passphrase;
			return self();
		}

		public DeterKeyChain build() {
			checkState(random != null || entropy != null || seed != null || watchingKey != null,
					"Must provide either entropy or random or seed or watchingKey");
			checkState(passphrase == null || seed == null, "Passphrase must not be specified with seed");
			DeterKeyChain chain;

			if (random != null) {

				chain = new DeterKeyChain(random, bits, getPassphrase(), seedCreationTimeSecs);
			} else if (entropy != null) {
				chain = new DeterKeyChain(entropy, getPassphrase(), seedCreationTimeSecs);
			} else if (seed != null) {
				chain = new DeterKeyChain(seed);
			} else {
				chain = new DeterKeyChain(watchingKey, seedCreationTimeSecs);
			}

			return chain;
		}

		protected String getPassphrase() {
			return passphrase != null ? passphrase : DEFAULT_PASSPHRASE_FOR_MNEMONIC;
		}
	}

	public static Builder<?> builder() {
		return new Builder();
	}

	public DeterKeyChain(SecureRandom random) {
		this(random, DeterSeed.DEFAULT_SEED_ENTROPY_BITS, DEFAULT_PASSPHRASE_FOR_MNEMONIC,
				Utils.currentTimeSeconds());
	}

	public DeterKeyChain(SecureRandom random, int bits) {
		this(random, bits, DEFAULT_PASSPHRASE_FOR_MNEMONIC, Utils.currentTimeSeconds());
	}

	public DeterKeyChain(SecureRandom random, int bits, String passphrase, long seedCreationTimeSecs) {
		this(new DeterSeed(random, bits, passphrase, seedCreationTimeSecs));
	}

	public DeterKeyChain(byte[] entropy, String passphrase, long seedCreationTimeSecs) {
		this(new DeterSeed(entropy, passphrase, seedCreationTimeSecs));
	}

	protected DeterKeyChain(DeterSeed seed) {
		this(seed, null);
	}

	public DeterKeyChain(DeterKey watchingKey, long creationTimeSeconds) {
		checkArgument(watchingKey.isPubKeyOnly(),
				"Private subtrees not currently supported: if you got this key from DKC.getWatchingKey() then use .dropPrivate().dropParent() on it first.");
		checkArgument(watchingKey.getPath().size() == getAccountPath().size(),
				"You can only watch an account key currently");
		baseKeyChain = new BaseKeyChain();
		this.creationTimeSeconds = creationTimeSeconds;
		this.seed = null;
		rootKey = null;
		addToBasicChain(watchingKey);
		hierarchy = new DHierarchy(watchingKey);
		initializeHierarchyUnencrypted(watchingKey);
	}

	public DeterKeyChain(DeterKey watchingKey) {
		this(watchingKey, Utils.currentTimeSeconds());
	}

	protected DeterKeyChain(DeterKey watchKey, boolean isFollowing) {
		this(watchKey, Utils.currentTimeSeconds());
		this.isFollowing = isFollowing;
	}

	public static DeterKeyChain watchAndFollow(DeterKey watchKey) {
		return new DeterKeyChain(watchKey, true);
	}

	public static DeterKeyChain watch(DeterKey accountKey) {
		return watch(accountKey, DHierarchy.BIP32_STANDARDISATION_TIME_SECS);
	}

	public static DeterKeyChain watch(DeterKey accountKey, long seedCreationTimeSecs) {
		return new DeterKeyChain(accountKey, seedCreationTimeSecs);
	}

	protected DeterKeyChain(DeterSeed seed, @Nullable KeyCrypt crypter) {
		this.seed = seed;
		baseKeyChain = new BaseKeyChain(crypter);
		if (!seed.isEncrypted()) {
			rootKey = HDKeyD.createMasterPrivateKey(checkNotNull(seed.getSeedBytes()));
			rootKey.setCreationTimeSeconds(seed.getCreationTimeSeconds());
			addToBasicChain(rootKey);
			hierarchy = new DHierarchy(rootKey);
			for (int i = 1; i <= getAccountPath().size(); i++) {
				addToBasicChain(hierarchy.get(getAccountPath().subList(0, i), false, true));
			}
			initializeHierarchyUnencrypted(rootKey);
		}

	}

	protected DeterKeyChain(KeyCrypt crypter, KeyParameter aesKey, DeterKeyChain chain) {

		checkNotNull(chain.rootKey);
		checkNotNull(chain.seed);

		checkArgument(!chain.rootKey.isEncrypted(), "Chain already encrypted");

		this.issuedExternalKeys = chain.issuedExternalKeys;
		this.issuedInternalKeys = chain.issuedInternalKeys;

		this.lookaheadSize = chain.lookaheadSize;
		this.lookaheadThreshold = chain.lookaheadThreshold;

		this.seed = chain.seed.encrypt(crypter, aesKey);
		baseKeyChain = new BaseKeyChain(crypter);

		rootKey = chain.rootKey.encrypt(crypter, aesKey, null);
		hierarchy = new DHierarchy(rootKey);
		baseKeyChain.importKey(rootKey);

		for (int i = 1; i < getAccountPath().size(); i++) {
			encryptNonLeaf(aesKey, chain, rootKey, getAccountPath().subList(0, i));
		}
		DeterKey account = encryptNonLeaf(aesKey, chain, rootKey, getAccountPath());
		externalKey = encryptNonLeaf(aesKey, chain, account, HDUtils.concat(getAccountPath(), EXTERNAL_SUBPATH));
		internalKey = encryptNonLeaf(aesKey, chain, account, HDUtils.concat(getAccountPath(), INTERNAL_SUBPATH));

		for (ECKey eckey : chain.baseKeyChain.getKeys()) {
			DeterKey key = (DeterKey) eckey;
			if (key.getPath().size() != getAccountPath().size() + 2)
				continue;
			DeterKey parent = hierarchy.get(checkNotNull(key.getParent()).getPath(), false, false);

			key = new DeterKey(key.dropPrivateBytes(), parent);
			hierarchy.putKey(key);
			baseKeyChain.importKey(key);
		}
	}

	protected ImmutableList<Child> getAccountPath() {
		return ACCOUNT_ZERO_PATH;
	}

	private DeterKey encryptNonLeaf(KeyParameter aesKey, DeterKeyChain chain, DeterKey parent,
			ImmutableList<Child> path) {
		DeterKey key = chain.hierarchy.get(path, false, false);
		key = key.encrypt(checkNotNull(baseKeyChain.getKeyCrypter()), aesKey, parent);
		hierarchy.putKey(key);
		baseKeyChain.importKey(key);
		return key;
	}

	private void initializeHierarchyUnencrypted(DeterKey baseKey) {
		externalKey = hierarchy.deriveChild(getAccountPath(), false, false, Child.ZERO);
		internalKey = hierarchy.deriveChild(getAccountPath(), false, false, Child.ONE);
		addToBasicChain(externalKey);
		addToBasicChain(internalKey);
	}

	@Override
	public DeterKey getKey(KeyPurpose purpose) {
		return getKeys(purpose, 1).get(0);
	}

	@Override
	public List<DeterKey> getKeys(KeyPurpose purpose, int numberOfKeys) {
		checkArgument(numberOfKeys > 0);
		lock.lock();
		try {
			DeterKey parentKey;
			int index;
			switch (purpose) {

			case RECEIVE_FUNDS:
			case REFUND:
				issuedExternalKeys += numberOfKeys;
				index = issuedExternalKeys;
				parentKey = externalKey;
				break;
			case AUTHENTICATION:
			case CHANGE:
				issuedInternalKeys += numberOfKeys;
				index = issuedInternalKeys;
				parentKey = internalKey;
				break;
			default:
				throw new UnsupportedOperationException();
			}

			List<DeterKey> lookahead = maybeLookAhead(parentKey, index, 0, 0);
			baseKeyChain.importKeys(lookahead);
			List<DeterKey> keys = new ArrayList<DeterKey>(numberOfKeys);
			for (int i = 0; i < numberOfKeys; i++) {
				ImmutableList<Child> path = HDUtils.append(parentKey.getPath(),
						new Child(index - numberOfKeys + i, false));
				DeterKey k = hierarchy.get(path, false, false);

				checkForBitFlip(k);
				keys.add(k);
			}
			return keys;
		} finally {
			lock.unlock();
		}
	}

	private void checkForBitFlip(DeterKey k) {
		DeterKey parent = checkNotNull(k.getParent());
		byte[] rederived = HDKeyD.deriveChildKeyBytesFromPublic(parent, k.getChildNumber(),
				HDKeyD.PublicDeriveMode.WITH_INVERSION).keyBytes;
		byte[] actual = k.getPubKey();
		if (!Arrays.equals(rederived, actual))
			throw new IllegalStateException(String.format(Locale.US, "Bit-flip check failed: %s vs %s",
					Arrays.toString(rederived), Arrays.toString(actual)));
	}

	private void addToBasicChain(DeterKey key) {
		baseKeyChain.importKeys(ImmutableList.of(key));
	}

	public DeterKey markKeyAsUsed(DeterKey k) {
		int numChildren = k.getChildNumber().i() + 1;

		if (k.getParent() == internalKey) {
			if (issuedInternalKeys < numChildren) {
				issuedInternalKeys = numChildren;
				maybeLookAhead();
			}
		} else if (k.getParent() == externalKey) {
			if (issuedExternalKeys < numChildren) {
				issuedExternalKeys = numChildren;
				maybeLookAhead();
			}
		}
		return k;
	}

	public DeterKey findKeyFromPubHash(byte[] pubkeyHash) {
		lock.lock();
		try {
			return (DeterKey) baseKeyChain.findKeyFromPubHash(pubkeyHash);
		} finally {
			lock.unlock();
		}
	}

	public DeterKey findKeyFromPubKey(byte[] pubkey) {
		lock.lock();
		try {
			return (DeterKey) baseKeyChain.findKeyFromPubKey(pubkey);
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public DeterKey markPubHashAsUsed(byte[] pubkeyHash) {
		lock.lock();
		try {
			DeterKey k = (DeterKey) baseKeyChain.findKeyFromPubHash(pubkeyHash);
			if (k != null)
				markKeyAsUsed(k);
			return k;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public DeterKey markPubKeyAsUsed(byte[] pubkey) {
		lock.lock();
		try {
			DeterKey k = (DeterKey) baseKeyChain.findKeyFromPubKey(pubkey);
			if (k != null)
				markKeyAsUsed(k);
			return k;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean hasKey(ECKey key) {
		lock.lock();
		try {
			return baseKeyChain.hasKey(key);
		} finally {
			lock.unlock();
		}
	}

	protected DeterKey getKeyByPath(Child... path) {
		return getKeyByPath(ImmutableList.copyOf(path));
	}

	protected DeterKey getKeyByPath(List<Child> path) {
		return getKeyByPath(path, false);
	}

	public DeterKey getKeyByPath(List<Child> path, boolean create) {
		return hierarchy.get(path, false, create);
	}

	public DeterKey getWatchingKey() {
		return getKeyByPath(getAccountPath());
	}

	public boolean isWatching() {
		return getWatchingKey().isWatching();
	}

	@Override
	public int numKeys() {

		lock.lock();
		try {
			maybeLookAhead();
			return baseKeyChain.numKeys();
		} finally {
			lock.unlock();
		}

	}

	public int numLeafKeysIssued() {
		lock.lock();
		try {
			return issuedExternalKeys + issuedInternalKeys;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long getEarliestKeyCreationTime() {
		return seed != null ? seed.getCreationTimeSeconds() : creationTimeSeconds;
	}

	@Override
	public void addEventListener(CrosKeyChainListener listener) {
		baseKeyChain.addEventListener(listener);
	}

	@Override
	public void addEventListener(CrosKeyChainListener listener, Executor executor) {
		baseKeyChain.addEventListener(listener, executor);
	}

	@Override
	public boolean removeEventListener(CrosKeyChainListener listener) {
		return baseKeyChain.removeEventListener(listener);
	}

	@Nullable
	public List<String> getMnemonicCode() {
		if (seed == null)
			return null;

		lock.lock();
		try {
			return seed.getMnemonicCode();
		} finally {
			lock.unlock();
		}
	}

	public boolean isFollowing() {
		return isFollowing;
	}

	@Override
	public List<Protos.Key> serializeToProtobuf() {
		List<Protos.Key> result = newArrayList();
		lock.lock();
		try {
			result.addAll(serializeMyselfToProtobuf());
		} finally {
			lock.unlock();
		}
		return result;
	}

	protected List<Protos.Key> serializeMyselfToProtobuf() {

		LinkedList<Protos.Key> entries = newLinkedList();
		if (seed != null) {
			Protos.Key.Builder mnemonicEntry = BaseKeyChain.serializeEncryptableItem(seed);
			mnemonicEntry.setType(Protos.Key.Type.DETERMINISTIC_MNEMONIC);
			serializeSeedEncryptableItem(seed, mnemonicEntry);
			entries.add(mnemonicEntry.build());
		}
		Map<ECKey, Protos.Key.Builder> keys = baseKeyChain.serializeToEditableProtobufs();
		for (Map.Entry<ECKey, Protos.Key.Builder> entry : keys.entrySet()) {
			DeterKey key = (DeterKey) entry.getKey();
			Protos.Key.Builder proto = entry.getValue();
			proto.setType(Protos.Key.Type.DETERMINISTIC_KEY);
			final Protos.DeterministicKey.Builder detKey = proto.getDeterministicKeyBuilder();
			detKey.setChainCode(ByteString.copyFrom(key.getChainCode()));
			for (Child num : key.getPath())
				detKey.addPath(num.i());
			if (key.equals(externalKey)) {
				detKey.setIssuedSubkeys(issuedExternalKeys);
				detKey.setLookaheadSize(lookaheadSize);
				detKey.setSigsRequiredToSpend(getSigsRequiredToSpend());
			} else if (key.equals(internalKey)) {
				detKey.setIssuedSubkeys(issuedInternalKeys);
				detKey.setLookaheadSize(lookaheadSize);
				detKey.setSigsRequiredToSpend(getSigsRequiredToSpend());
			}

			if (entries.isEmpty() && isFollowing()) {
				detKey.setIsFollowing(true);
			}
			if (key.getParent() != null) {

				proto.clearCreationTimestamp();
			}
			entries.add(proto.build());
		}
		return entries;
	}

	static List<DeterKeyChain> fromProtobuf(List<Protos.Key> keys, @Nullable KeyCrypt crypter)
			throws InvalidWalletException {
		return fromProtobuf(keys, crypter, new DefaultBaseKeyChainFactory());
	}

	public static List<DeterKeyChain> fromProtobuf(List<Protos.Key> keys, @Nullable KeyCrypt crypter,
			CrosKeyChainFactory factory) throws InvalidWalletException {
		List<DeterKeyChain> chains = newLinkedList();
		DeterSeed seed = null;
		DeterKeyChain chain = null;

		int lookaheadSize = -1;
		int sigsRequiredToSpend = 1;

		PeekingIterator<Protos.Key> iter = Iterators.peekingIterator(keys.iterator());
		while (iter.hasNext()) {
			Protos.Key key = iter.next();
			final Protos.Key.Type t = key.getType();
			if (t == Protos.Key.Type.DETERMINISTIC_MNEMONIC) {
				if (chain != null) {
					checkState(lookaheadSize >= 0);
					chain.setLookaheadSize(lookaheadSize);
					chain.setSigsRequiredToSpend(sigsRequiredToSpend);
					chain.maybeLookAhead();
					chains.add(chain);
					chain = null;
				}
				long timestamp = key.getCreationTimestamp() / 1000;
				String passphrase = DEFAULT_PASSPHRASE_FOR_MNEMONIC;
				if (key.hasSecretBytes()) {
					if (key.hasEncryptedDeterministicSeed())
						throw new InvalidWalletException("Malformed key proto: " + key.toString());
					byte[] seedBytes = null;
					if (key.hasDeterministicSeed()) {
						seedBytes = key.getDeterministicSeed().toByteArray();
					}
					seed = new DeterSeed(key.getSecretBytes().toStringUtf8(), seedBytes, passphrase, timestamp);
				} else if (key.hasEncryptedData()) {
					if (key.hasDeterministicSeed())
						throw new InvalidWalletException("Malformed key proto: " + key.toString());
					EncryptData data = new EncryptData(key.getEncryptedData().getInitialisationVector().toByteArray(),
							key.getEncryptedData().getEncryptedPrivateKey().toByteArray());
					EncryptData encryptedSeedBytes = null;
					if (key.hasEncryptedDeterministicSeed()) {
						Protos.EncryptedData encryptedSeed = key.getEncryptedDeterministicSeed();
						encryptedSeedBytes = new EncryptData(encryptedSeed.getInitialisationVector().toByteArray(),
								encryptedSeed.getEncryptedPrivateKey().toByteArray());
					}
					seed = new DeterSeed(data, encryptedSeedBytes, timestamp);
				} else {
					throw new InvalidWalletException("Malformed key proto: " + key.toString());
				}
				if (log.isDebugEnabled())
					log.debug("Deserializing: DETERMINISTIC_MNEMONIC: {}", seed);
			} else if (t == Protos.Key.Type.DETERMINISTIC_KEY) {
				if (!key.hasDeterministicKey())
					throw new InvalidWalletException("Deterministic key missing extra data: " + key.toString());
				byte[] chainCode = key.getDeterministicKey().getChainCode().toByteArray();

				LinkedList<Child> path = newLinkedList();
				for (int i : key.getDeterministicKey().getPathList())
					path.add(new Child(i));

				LazyECPoint pubkey = new LazyECPoint(ECKey.CURVE.getCurve(), key.getPublicKey().toByteArray());
				final ImmutableList<Child> immutablePath = ImmutableList.copyOf(path);

				boolean isWatchingAccountKey = false;
				boolean isFollowingKey = false;

				if (key.getDeterministicKey().getIsFollowing()) {
					if (chain != null) {
						checkState(lookaheadSize >= 0);
						chain.setLookaheadSize(lookaheadSize);
						chain.setSigsRequiredToSpend(sigsRequiredToSpend);
						chain.maybeLookAhead();
						chains.add(chain);
						chain = null;
						seed = null;
					}
					isFollowingKey = true;
				}
				if (chain == null) {

					boolean isMarried = !isFollowingKey && !chains.isEmpty()
							&& chains.get(chains.size() - 1).isFollowing();
					if (seed == null) {
						DeterKey accountKey = new DeterKey(immutablePath, chainCode, pubkey, null, null);
						chain = factory.makeWatchingKeyChain(key, iter.peek(), accountKey, isFollowingKey, isMarried);
						isWatchingAccountKey = true;
					} else {
						chain = factory.makeKeyChain(key, iter.peek(), seed, crypter, isMarried);
						chain.lookaheadSize = LAZY_CALCULATE_LOOKAHEAD;

					}
				}

				DeterKey parent = null;
				if (!path.isEmpty() && !isWatchingAccountKey) {
					Child index = path.removeLast();
					parent = chain.hierarchy.get(path, false, false);
					path.add(index);
				}
				DeterKey detkey;
				if (key.hasSecretBytes()) {

					final BigInteger priv = new BigInteger(1, key.getSecretBytes().toByteArray());
					detkey = new DeterKey(immutablePath, chainCode, pubkey, priv, parent);
				} else {
					if (key.hasEncryptedData()) {
						Protos.EncryptedData proto = key.getEncryptedData();
						EncryptData data = new EncryptData(proto.getInitialisationVector().toByteArray(),
								proto.getEncryptedPrivateKey().toByteArray());
						checkNotNull(crypter, "Encountered an encrypted key but no key crypter provided");
						detkey = new DeterKey(immutablePath, chainCode, crypter, pubkey, data, parent);
					} else {

						detkey = new DeterKey(immutablePath, chainCode, pubkey, null, parent);
					}
				}
				if (key.hasCreationTimestamp())
					detkey.setCreationTimeSeconds(key.getCreationTimestamp() / 1000);
				if (log.isDebugEnabled())
					log.debug("Deserializing: DETERMINISTIC_KEY: {}", detkey);
				if (!isWatchingAccountKey) {

					if (path.size() == 0) {

						if (chain.rootKey == null) {
							chain.rootKey = detkey;
							chain.hierarchy = new DHierarchy(detkey);
						}
					} else if (path.size() == chain.getAccountPath().size() + 1) {
						if (detkey.getChildNumber().num() == 0) {
							chain.externalKey = detkey;
							chain.issuedExternalKeys = key.getDeterministicKey().getIssuedSubkeys();
							lookaheadSize = Math.max(lookaheadSize, key.getDeterministicKey().getLookaheadSize());
							sigsRequiredToSpend = key.getDeterministicKey().getSigsRequiredToSpend();
						} else if (detkey.getChildNumber().num() == 1) {
							chain.internalKey = detkey;
							chain.issuedInternalKeys = key.getDeterministicKey().getIssuedSubkeys();
						}
					}
				}
				chain.hierarchy.putKey(detkey);
				chain.baseKeyChain.importKey(detkey);
			}
		}
		if (chain != null) {
			checkState(lookaheadSize >= 0);
			chain.setLookaheadSize(lookaheadSize);
			chain.setSigsRequiredToSpend(sigsRequiredToSpend);
			chain.maybeLookAhead();
			chains.add(chain);
		}
		return chains;
	}

	@Override
	public DeterKeyChain toEncrypted(CharSequence password) {
		checkNotNull(password);
		checkArgument(password.length() > 0);
		checkState(seed != null, "Attempt to encrypt a watching chain.");
		checkState(!seed.isEncrypted());
		KeyCrypt scrypt = new KeyCrypterScrypt();
		KeyParameter derivedKey = scrypt.deriveKey(password);
		return toEncrypted(scrypt, derivedKey);
	}

	@Override
	public DeterKeyChain toEncrypted(KeyCrypt keyCrypt, KeyParameter aesKey) {
		return new DeterKeyChain(keyCrypt, aesKey, this);
	}

	@Override
	public DeterKeyChain toDecrypted(CharSequence password) {
		checkNotNull(password);
		checkArgument(password.length() > 0);
		KeyCrypt crypter = getKeyCrypter();
		checkState(crypter != null, "Chain not encrypted");
		KeyParameter derivedKey = crypter.deriveKey(password);
		return toDecrypted(derivedKey);
	}

	@Override
	public DeterKeyChain toDecrypted(KeyParameter aesKey) {
		checkState(getKeyCrypter() != null, "Key chain not encrypted");
		checkState(seed != null, "Can't decrypt a watching chain");
		checkState(seed.isEncrypted());
		String passphrase = DEFAULT_PASSPHRASE_FOR_MNEMONIC;
		DeterSeed decSeed = seed.decrypt(getKeyCrypter(), passphrase, aesKey);
		DeterKeyChain chain = makeKeyChainFromSeed(decSeed);

		if (!chain.getWatchingKey().getPubKeyPoint().equals(getWatchingKey().getPubKeyPoint()))
			throw new KeyCryptException("Provided AES key is wrong");
		chain.lookaheadSize = lookaheadSize;

		for (ECKey eckey : baseKeyChain.getKeys()) {
			DeterKey key = (DeterKey) eckey;
			if (key.getPath().size() != getAccountPath().size() + 2)
				continue;
			checkState(key.isEncrypted());
			DeterKey parent = chain.hierarchy.get(checkNotNull(key.getParent()).getPath(), false, false);

			key = new DeterKey(key.dropPrivateBytes(), parent);
			chain.hierarchy.putKey(key);
			chain.baseKeyChain.importKey(key);
		}
		chain.issuedExternalKeys = issuedExternalKeys;
		chain.issuedInternalKeys = issuedInternalKeys;
		return chain;
	}

	protected DeterKeyChain makeKeyChainFromSeed(DeterSeed seed) {
		return new DeterKeyChain(seed);
	}

	@Override
	public boolean checkPassword(CharSequence password) {
		checkNotNull(password);
		checkState(getKeyCrypter() != null, "Key chain not encrypted");
		return checkAESKey(getKeyCrypter().deriveKey(password));
	}

	@Override
	public boolean checkAESKey(KeyParameter aesKey) {
		checkState(rootKey != null, "Can't check password for a watching chain");
		checkNotNull(aesKey);
		checkState(getKeyCrypter() != null, "Key chain not encrypted");
		try {
			return rootKey.decrypt(aesKey).getPubKeyPoint().equals(rootKey.getPubKeyPoint());
		} catch (KeyCryptException e) {
			return false;
		}
	}

	@Nullable
	@Override
	public KeyCrypt getKeyCrypter() {
		return baseKeyChain.getKeyCrypter();
	}

	@Override
	public int numBloomFilterEntries() {
		return numKeys() * 2;
	}

	@Override
	public BloomFilter getFilter(int size, double falsePositiveRate, long tweak) {
		lock.lock();
		try {
			checkArgument(size >= numBloomFilterEntries());
			maybeLookAhead();
			return baseKeyChain.getFilter(size, falsePositiveRate, tweak);
		} finally {
			lock.unlock();
		}

	}

	public int getLookaheadSize() {
		lock.lock();
		try {
			return lookaheadSize;
		} finally {
			lock.unlock();
		}
	}

	public void setLookaheadSize(int lookaheadSize) {
		lock.lock();
		try {
			boolean readjustThreshold = this.lookaheadThreshold == calcDefaultLookaheadThreshold();
			this.lookaheadSize = lookaheadSize;
			if (readjustThreshold)
				this.lookaheadThreshold = calcDefaultLookaheadThreshold();
		} finally {
			lock.unlock();
		}
	}

	public void setLookaheadThreshold(int num) {
		lock.lock();
		try {
			if (num >= lookaheadSize)
				throw new IllegalArgumentException("Threshold larger or equal to the lookaheadSize");
			this.lookaheadThreshold = num;
		} finally {
			lock.unlock();
		}
	}

	public int getLookaheadThreshold() {
		lock.lock();
		try {
			if (lookaheadThreshold >= lookaheadSize)
				return 0;
			return lookaheadThreshold;
		} finally {
			lock.unlock();
		}
	}

	public void maybeLookAhead() {
		lock.lock();
		try {
			List<DeterKey> keys = maybeLookAhead(externalKey, issuedExternalKeys);
			keys.addAll(maybeLookAhead(internalKey, issuedInternalKeys));
			if (keys.isEmpty())
				return;
			keyLookaheadEpoch++;

			baseKeyChain.importKeys(keys);
		} finally {
			lock.unlock();
		}
	}

	private List<DeterKey> maybeLookAhead(DeterKey parent, int issued) {
		checkState(lock.isHeldByCurrentThread());
		return maybeLookAhead(parent, issued, getLookaheadSize(), getLookaheadThreshold());
	}

	private List<DeterKey> maybeLookAhead(DeterKey parent, int issued, int lookaheadSize, int lookaheadThreshold) {
		checkState(lock.isHeldByCurrentThread());
		final int numChildren = hierarchy.getNumChildren(parent.getPath());
		final int needed = issued + lookaheadSize + lookaheadThreshold - numChildren;

		if (needed <= lookaheadThreshold)
			return new ArrayList<DeterKey>();

		log.info("{} keys needed for {} = {} issued + {} lookahead size + {} lookahead threshold - {} num children",
				needed, parent.getPathAsString(), issued, lookaheadSize, lookaheadThreshold, numChildren);

		List<DeterKey> result = new ArrayList<DeterKey>(needed);
		long now = System.currentTimeMillis();
		int nextChild = numChildren;
		for (int i = 0; i < needed; i++) {
			DeterKey key = HDKeyD.deriveThisOrNextChildKey(parent, nextChild);
			key = key.dropPrivateBytes();
			hierarchy.putKey(key);
			result.add(key);
			nextChild = key.getChildNumber().num() + 1;
		}
		log.info("Took {} msec", System.currentTimeMillis() - now);
		return result;
	}

	public void maybeLookAheadScripts() {
	}

	public int getIssuedExternalKeys() {
		lock.lock();
		try {
			return issuedExternalKeys;
		} finally {
			lock.unlock();
		}
	}

	public int getIssuedInternalKeys() {
		lock.lock();
		try {
			return issuedInternalKeys;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public DeterSeed getSeed() {
		lock.lock();
		try {
			return seed;
		} finally {
			lock.unlock();
		}
	}

	List<ECKey> getKeys(boolean includeLookahead) {
		List<ECKey> keys = baseKeyChain.getKeys();
		if (!includeLookahead) {
			int treeSize = internalKey.getPath().size();
			List<ECKey> issuedKeys = new LinkedList<ECKey>();
			for (ECKey key : keys) {
				DeterKey detkey = (DeterKey) key;
				DeterKey parent = detkey.getParent();
				if (parent == null)
					continue;
				if (detkey.getPath().size() <= treeSize)
					continue;
				if (parent.equals(internalKey) && detkey.getChildNumber().i() >= issuedInternalKeys)
					continue;
				if (parent.equals(externalKey) && detkey.getChildNumber().i() >= issuedExternalKeys)
					continue;
				issuedKeys.add(detkey);
			}
			return issuedKeys;
		}
		return keys;
	}

	public List<ECKey> getIssuedReceiveKeys() {
		final List<ECKey> keys = new ArrayList<ECKey>(getKeys(false));
		for (Iterator<ECKey> i = keys.iterator(); i.hasNext();) {
			DeterKey parent = ((DeterKey) i.next()).getParent();
			if (parent == null || !externalKey.equals(parent))
				i.remove();
		}
		return keys;
	}

	public List<DeterKey> getLeafKeys() {
		ImmutableList.Builder<DeterKey> keys = ImmutableList.builder();
		for (ECKey key : getKeys(true)) {
			DeterKey deterKey = (DeterKey) key;
			if (deterKey.getPath().size() == getAccountPath().size() + 2) {
				keys.add(deterKey);
			}
		}
		return keys.build();
	}

	static void serializeSeedEncryptableItem(DeterSeed seed, Protos.Key.Builder proto) {

		if (seed.isEncrypted() && seed.getEncryptedSeedData() != null) {
			EncryptData data = seed.getEncryptedSeedData();
			proto.getEncryptedDeterministicSeedBuilder()
					.setEncryptedPrivateKey(ByteString.copyFrom(data.encryptedBytes))
					.setInitialisationVector(ByteString.copyFrom(data.initialisationVector));

			checkState(seed.getEncryptionType() == Protos.Wallet.EncryptionType.ENCRYPTED_SCRYPT_AES);
		} else {
			final byte[] secret = seed.getSeedBytes();
			if (secret != null)
				proto.setDeterministicSeed(ByteString.copyFrom(secret));
		}
	}

	public int getKeyLookaheadEpoch() {
		lock.lock();
		try {
			return keyLookaheadEpoch;
		} finally {
			lock.unlock();
		}
	}

	public boolean isMarried() {
		return false;
	}

	public RedeemBlockData getRedeemData(DeterKey followedKey) {
		throw new UnsupportedOperationException();
	}

	public ChainScript freshOutputScript(KeyPurpose purpose) {
		throw new UnsupportedOperationException();
	}

	public String toString(boolean includePrivateKeys, NetworkParams params) {
		final StringBuilder builder2 = new StringBuilder();
		if (seed != null) {
			if (seed.isEncrypted()) {
				builder2.append(String.format(Locale.US, "Seed is encrypted%n"));
			} else if (includePrivateKeys) {
				final List<String> words = seed.getMnemonicCode();
				builder2.append(String.format(Locale.US, "Seed as words: %s%nSeed as hex:   %s%n", Utils.join(words),
						seed.toHexString()));
			}
			builder2.append(String.format(Locale.US, "Seed birthday: %d  [%s]%n", seed.getCreationTimeSeconds(),
					Utils.dateTimeFormat(seed.getCreationTimeSeconds() * 1000)));
		}
		final DeterKey watchingKey = getWatchingKey();

		if (watchingKey.getParent() != null) {
			builder2.append(String.format(Locale.US, "Key to watch:  %s%n", watchingKey.serializePubB58(params)));
		}
		formatAddresses(includePrivateKeys, params, builder2);
		return builder2.toString();
	}

	protected void formatAddresses(boolean includePrivateKeys, NetworkParams params, StringBuilder builder2) {
		for (ECKey key : getKeys(false))
			key.formatKeyWithAddress(includePrivateKeys, builder2, params);
	}

	public void setSigsRequiredToSpend(int sigsRequiredToSpend) {
		this.sigsRequiredToSpend = sigsRequiredToSpend;
	}

	public int getSigsRequiredToSpend() {
		return sigsRequiredToSpend;
	}

	@Nullable
	public RedeemBlockData findRedeemDataByScriptHash(ByteString bytes) {
		return null;
	}
}
