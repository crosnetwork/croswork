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
import cros.mail.chain.encrypt.*;
import cros.mail.chain.misc.ListenerRegister;
import cros.mail.chain.misc.Threading;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.*;

public class BaseKeyChain implements EncryptKeyChain {
	private final ReentrantLock lock = Threading.lock("BaseKeyChain");

	private final LinkedHashMap<ByteString, ECKey> hashToKeys;
	private final LinkedHashMap<ByteString, ECKey> pubkeyToKeys;
	@Nullable
	private final KeyCrypt keyCrypt;
	private boolean isWatching;

	private final CopyOnWriteArrayList<ListenerRegister<CrosKeyChainListener>> listeners;

	public BaseKeyChain() {
		this(null);
	}

	public BaseKeyChain(@Nullable KeyCrypt crypter) {
		this.keyCrypt = crypter;
		hashToKeys = new LinkedHashMap<ByteString, ECKey>();
		pubkeyToKeys = new LinkedHashMap<ByteString, ECKey>();
		listeners = new CopyOnWriteArrayList<ListenerRegister<CrosKeyChainListener>>();
	}

	@Override
	@Nullable
	public KeyCrypt getKeyCrypter() {
		lock.lock();
		try {
			return keyCrypt;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ECKey getKey(@Nullable KeyPurpose ignored) {
		lock.lock();
		try {
			if (hashToKeys.isEmpty()) {
				checkState(keyCrypt == null);
				final ECKey key = new ECKey();
				importKeyLocked(key);
				queueOnKeysAdded(ImmutableList.of(key));
			}
			return hashToKeys.values().iterator().next();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<ECKey> getKeys(@Nullable KeyPurpose purpose, int numberOfKeys) {
		checkArgument(numberOfKeys > 0);
		lock.lock();
		try {
			if (hashToKeys.size() < numberOfKeys) {
				checkState(keyCrypt == null);

				List<ECKey> keys = new ArrayList<ECKey>();
				for (int i = 0; i < numberOfKeys - hashToKeys.size(); i++) {
					keys.add(new ECKey());
				}

				ImmutableList<ECKey> immutableKeys = ImmutableList.copyOf(keys);
				importKeysLocked(immutableKeys);
				queueOnKeysAdded(immutableKeys);
			}

			List<ECKey> keysToReturn = new ArrayList<ECKey>();
			int count = 0;
			while (hashToKeys.values().iterator().hasNext() && numberOfKeys != count) {
				keysToReturn.add(hashToKeys.values().iterator().next());
				count++;
			}
			return keysToReturn;
		} finally {
			lock.unlock();
		}
	}

	public List<ECKey> getKeys() {
		lock.lock();
		try {
			return new ArrayList<ECKey>(hashToKeys.values());
		} finally {
			lock.unlock();
		}
	}

	public int importKeys(ECKey... keys) {
		return importKeys(ImmutableList.copyOf(keys));
	}

	public int importKeys(List<? extends ECKey> keys) {
		lock.lock();
		try {

			for (ECKey key : keys) {
				checkKeyEncryptionStateMatches(key);
			}
			List<ECKey> actuallyAdded = new ArrayList<ECKey>(keys.size());
			for (final ECKey key : keys) {
				if (hasKey(key))
					continue;
				actuallyAdded.add(key);
				importKeyLocked(key);
			}
			if (actuallyAdded.size() > 0)
				queueOnKeysAdded(actuallyAdded);
			return actuallyAdded.size();
		} finally {
			lock.unlock();
		}
	}

	private void checkKeyEncryptionStateMatches(ECKey key) {
		if (keyCrypt == null && key.isEncrypted())
			throw new KeyCryptException("Key is encrypted but chain is not");
		else if (keyCrypt != null && !key.isEncrypted())
			throw new KeyCryptException("Key is not encrypted but chain is");
		else if (keyCrypt != null && key.getKeyCrypter() != null && !key.getKeyCrypter().equals(keyCrypt))
			throw new KeyCryptException("Key encrypted under different parameters to chain");
	}

	private void importKeyLocked(ECKey key) {
		if (hashToKeys.isEmpty()) {
			isWatching = key.isWatching();
		} else {
			if (key.isWatching() && !isWatching)
				throw new IllegalArgumentException("Key is watching but chain is not");
			if (!key.isWatching() && isWatching)
				throw new IllegalArgumentException("Key is not watching but chain is");
		}
		ECKey previousKey = pubkeyToKeys.put(ByteString.copyFrom(key.getPubKey()), key);
		hashToKeys.put(ByteString.copyFrom(key.getPubKeyHash()), key);
		checkState(previousKey == null);
	}

	private void importKeysLocked(List<ECKey> keys) {
		for (ECKey key : keys) {
			importKeyLocked(key);
		}
	}

	public void importKey(ECKey key) {
		lock.lock();
		try {
			checkKeyEncryptionStateMatches(key);
			if (hasKey(key))
				return;
			importKeyLocked(key);
			queueOnKeysAdded(ImmutableList.of(key));
		} finally {
			lock.unlock();
		}
	}

	public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
		lock.lock();
		try {
			return hashToKeys.get(ByteString.copyFrom(pubkeyHash));
		} finally {
			lock.unlock();
		}
	}

	public ECKey findKeyFromPubKey(byte[] pubkey) {
		lock.lock();
		try {
			return pubkeyToKeys.get(ByteString.copyFrom(pubkey));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean hasKey(ECKey key) {
		return findKeyFromPubKey(key.getPubKey()) != null;
	}

	@Override
	public int numKeys() {
		return pubkeyToKeys.size();
	}

	public enum State {
		EMPTY, WATCHING, REGULAR
	}

	public State isWatching() {
		lock.lock();
		try {
			if (hashToKeys.isEmpty())
				return State.EMPTY;
			return isWatching ? State.WATCHING : State.REGULAR;
		} finally {
			lock.unlock();
		}
	}

	public boolean removeKey(ECKey key) {
		lock.lock();
		try {
			boolean a = hashToKeys.remove(ByteString.copyFrom(key.getPubKeyHash())) != null;
			boolean b = pubkeyToKeys.remove(ByteString.copyFrom(key.getPubKey())) != null;
			checkState(a == b);
			return a;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long getEarliestKeyCreationTime() {
		lock.lock();
		try {
			long time = Long.MAX_VALUE;
			for (ECKey key : hashToKeys.values())
				time = Math.min(key.getCreationTimeSeconds(), time);
			return time;
		} finally {
			lock.unlock();
		}
	}

	public List<ListenerRegister<CrosKeyChainListener>> getListeners() {
		return new ArrayList<ListenerRegister<CrosKeyChainListener>>(listeners);
	}

	Map<ECKey, Protos.Key.Builder> serializeToEditableProtobufs() {
		Map<ECKey, Protos.Key.Builder> result = new LinkedHashMap<ECKey, Protos.Key.Builder>();
		for (ECKey ecKey : hashToKeys.values()) {
			Protos.Key.Builder protoKey = serializeEncryptableItem(ecKey);
			protoKey.setPublicKey(ByteString.copyFrom(ecKey.getPubKey()));
			result.put(ecKey, protoKey);
		}
		return result;
	}

	@Override
	public List<Protos.Key> serializeToProtobuf() {
		Collection<Protos.Key.Builder> builders = serializeToEditableProtobufs().values();
		List<Protos.Key> result = new ArrayList<Protos.Key>(builders.size());
		for (Protos.Key.Builder builder : builders)
			result.add(builder.build());
		return result;
	}

	static Protos.Key.Builder serializeEncryptableItem(EncryptItem item) {
		Protos.Key.Builder proto = Protos.Key.newBuilder();
		proto.setCreationTimestamp(item.getCreationTimeSeconds() * 1000);
		if (item.isEncrypted() && item.getEncryptedData() != null) {

			EncryptData data = item.getEncryptedData();
			proto.getEncryptedDataBuilder().setEncryptedPrivateKey(ByteString.copyFrom(data.encryptedBytes))
					.setInitialisationVector(ByteString.copyFrom(data.initialisationVector));

			checkState(item.getEncryptionType() == Protos.Wallet.EncryptionType.ENCRYPTED_SCRYPT_AES);
			proto.setType(Protos.Key.Type.ENCRYPTED_SCRYPT_AES);
		} else {
			final byte[] secret = item.getSecretBytes();

			if (secret != null)
				proto.setSecretBytes(ByteString.copyFrom(secret));
			proto.setType(Protos.Key.Type.ORIGINAL);
		}
		return proto;
	}

	public static BaseKeyChain fromProtobufUnencrypted(List<Protos.Key> keys) throws InvalidWalletException {
		BaseKeyChain chain = new BaseKeyChain();
		chain.deserializeFromProtobuf(keys);
		return chain;
	}

	public static BaseKeyChain fromProtobufEncrypted(List<Protos.Key> keys, KeyCrypt crypter)
			throws InvalidWalletException {
		BaseKeyChain chain = new BaseKeyChain(checkNotNull(crypter));
		chain.deserializeFromProtobuf(keys);
		return chain;
	}

	private void deserializeFromProtobuf(List<Protos.Key> keys) throws InvalidWalletException {
		lock.lock();
		try {
			checkState(hashToKeys.isEmpty(), "Tried to deserialize into a non-empty chain");
			for (Protos.Key key : keys) {
				if (key.getType() != Protos.Key.Type.ORIGINAL && key.getType() != Protos.Key.Type.ENCRYPTED_SCRYPT_AES)
					continue;
				boolean encrypted = key.getType() == Protos.Key.Type.ENCRYPTED_SCRYPT_AES;
				byte[] priv = key.hasSecretBytes() ? key.getSecretBytes().toByteArray() : null;
				if (!key.hasPublicKey())
					throw new InvalidWalletException("Public key missing");
				byte[] pub = key.getPublicKey().toByteArray();
				ECKey ecKey;
				if (encrypted) {
					checkState(keyCrypt != null,
							"This wallet is encrypted but encrypt() was not called prior to deserialization");
					if (!key.hasEncryptedData())
						throw new InvalidWalletException("Encrypted private key data missing");
					Protos.EncryptedData proto = key.getEncryptedData();
					EncryptData e = new EncryptData(proto.getInitialisationVector().toByteArray(),
							proto.getEncryptedPrivateKey().toByteArray());
					ecKey = ECKey.fromEncrypted(e, keyCrypt, pub);
				} else {
					if (priv != null)
						ecKey = ECKey.fromPrivateAndPrecalculatedPublic(priv, pub);
					else
						ecKey = ECKey.fromPublicOnly(pub);
				}
				ecKey.setCreationTimeSeconds((key.getCreationTimestamp() + 500) / 1000);
				importKeyLocked(ecKey);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addEventListener(CrosKeyChainListener listener) {
		addEventListener(listener, Threading.USER_THREAD);
	}

	@Override
	public void addEventListener(CrosKeyChainListener listener, Executor executor) {
		listeners.add(new ListenerRegister<CrosKeyChainListener>(listener, executor));
	}

	@Override
	public boolean removeEventListener(CrosKeyChainListener listener) {
		return ListenerRegister.removeFromList(listener, listeners);
	}

	private void queueOnKeysAdded(final List<ECKey> keys) {
		checkState(lock.isHeldByCurrentThread());
		for (final ListenerRegister<CrosKeyChainListener> registration : listeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onKeysAdded(keys);
				}
			});
		}
	}

	@Override
	public BaseKeyChain toEncrypted(CharSequence password) {
		checkNotNull(password);
		checkArgument(password.length() > 0);
		KeyCrypt scrypt = new KeyCrypterScrypt();
		KeyParameter derivedKey = scrypt.deriveKey(password);
		return toEncrypted(scrypt, derivedKey);
	}

	@Override
	public BaseKeyChain toEncrypted(KeyCrypt keyCrypt, KeyParameter aesKey) {
		lock.lock();
		try {
			checkNotNull(keyCrypt);
			checkState(this.keyCrypt == null, "Key chain is already encrypted");
			BaseKeyChain encrypted = new BaseKeyChain(keyCrypt);
			for (ECKey key : hashToKeys.values()) {
				ECKey encryptedKey = key.encrypt(keyCrypt, aesKey);

				if (!ECKey.encryptionIsReversible(key, encryptedKey, keyCrypt, aesKey))
					throw new KeyCryptException("The key " + key.toString()
							+ " cannot be successfully decrypted after encryption so aborting wallet encryption.");
				encrypted.importKeyLocked(encryptedKey);
			}
			return encrypted;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public BaseKeyChain toDecrypted(CharSequence password) {
		checkNotNull(keyCrypt, "Wallet is already decrypted");
		KeyParameter aesKey = keyCrypt.deriveKey(password);
		return toDecrypted(aesKey);
	}

	@Override
	public BaseKeyChain toDecrypted(KeyParameter aesKey) {
		lock.lock();
		try {
			checkState(keyCrypt != null, "Wallet is already decrypted");

			if (numKeys() > 0 && !checkAESKey(aesKey))
				throw new KeyCryptException("Password/key was incorrect.");
			BaseKeyChain decrypted = new BaseKeyChain();
			for (ECKey key : hashToKeys.values()) {
				decrypted.importKeyLocked(key.decrypt(aesKey));
			}
			return decrypted;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean checkPassword(CharSequence password) {
		checkNotNull(password);
		checkState(keyCrypt != null, "Key chain not encrypted");
		return checkAESKey(keyCrypt.deriveKey(password));
	}

	@Override
	public boolean checkAESKey(KeyParameter aesKey) {
		lock.lock();
		try {

			if (hashToKeys.isEmpty())
				return false;
			checkState(keyCrypt != null, "Key chain is not encrypted");

			ECKey first = null;
			for (ECKey key : hashToKeys.values()) {
				if (key.isEncrypted()) {
					first = key;
					break;
				}
			}
			checkState(first != null, "No encrypted keys in the wallet");

			try {
				ECKey rebornKey = first.decrypt(aesKey);
				return Arrays.equals(first.getPubKey(), rebornKey.getPubKey());
			} catch (KeyCryptException e) {

				return false;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public BloomFilter getFilter(int size, double falsePositiveRate, long tweak) {
		lock.lock();
		try {
			BloomFilter filter = new BloomFilter(size, falsePositiveRate, tweak);
			for (ECKey key : hashToKeys.values())
				filter.insert(key);
			return filter;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int numBloomFilterEntries() {
		return numKeys() * 2;
	}

	@Nullable
	public ECKey findOldestKeyAfter(long timeSecs) {
		lock.lock();
		try {
			ECKey oldest = null;
			for (ECKey key : hashToKeys.values()) {
				final long keyTime = key.getCreationTimeSeconds();
				if (keyTime > timeSecs) {
					if (oldest == null || oldest.getCreationTimeSeconds() > keyTime)
						oldest = key;
				}
			}
			return oldest;
		} finally {
			lock.unlock();
		}
	}

	public List<ECKey> findKeysBefore(long timeSecs) {
		lock.lock();
		try {
			List<ECKey> results = Lists.newLinkedList();
			for (ECKey key : hashToKeys.values()) {
				final long keyTime = key.getCreationTimeSeconds();
				if (keyTime < timeSecs) {
					results.add(key);
				}
			}
			return results;
		} finally {
			lock.unlock();
		}
	}
}
