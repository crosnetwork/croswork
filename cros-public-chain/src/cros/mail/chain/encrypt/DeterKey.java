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

import cros.mail.chain.core.*;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.math.ec.ECPoint;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import static com.google.common.base.Preconditions.*;
import static cros.mail.chain.core.Utils.HEX;

public class DeterKey extends ECKey {

	public static final Comparator<ECKey> CHILDNUM_ORDER = new Comparator<ECKey>() {
		@Override
		public int compare(ECKey k1, ECKey k2) {
			Child cn1 = ((DeterKey) k1).getChildNumber();
			Child cn2 = ((DeterKey) k2).getChildNumber();
			return cn1.compareTo(cn2);
		}
	};

	private static final long serialVersionUID = 1L;

	private final DeterKey parent;
	private final ImmutableList<Child> childNumberPath;
	private final int depth;
	private int parentFingerprint;

	private final byte[] chainCode;

	public DeterKey(ImmutableList<Child> childNumberPath, byte[] chainCode, LazyECPoint publicAsPoint,
			@Nullable BigInteger priv, @Nullable DeterKey parent) {
		super(priv, compressPoint(checkNotNull(publicAsPoint)));
		checkArgument(chainCode.length == 32);
		this.parent = parent;
		this.childNumberPath = checkNotNull(childNumberPath);
		this.chainCode = Arrays.copyOf(chainCode, chainCode.length);
		this.depth = parent == null ? 0 : parent.depth + 1;
		this.parentFingerprint = (parent != null) ? parent.getFingerprint() : 0;
	}

	public DeterKey(ImmutableList<Child> childNumberPath, byte[] chainCode, ECPoint publicAsPoint,
			@Nullable BigInteger priv, @Nullable DeterKey parent) {
		this(childNumberPath, chainCode, new LazyECPoint(publicAsPoint), priv, parent);
	}

	public DeterKey(ImmutableList<Child> childNumberPath, byte[] chainCode, BigInteger priv,
			@Nullable DeterKey parent) {
		super(priv, compressPoint(ECKey.publicPointFromPrivate(priv)));
		checkArgument(chainCode.length == 32);
		this.parent = parent;
		this.childNumberPath = checkNotNull(childNumberPath);
		this.chainCode = Arrays.copyOf(chainCode, chainCode.length);
		this.depth = parent == null ? 0 : parent.depth + 1;
		this.parentFingerprint = (parent != null) ? parent.getFingerprint() : 0;
	}

	public DeterKey(ImmutableList<Child> childNumberPath, byte[] chainCode, KeyCrypt crypter, LazyECPoint pub,
			EncryptData priv, @Nullable DeterKey parent) {
		this(childNumberPath, chainCode, pub, null, parent);
		this.encryptedPrivateKey = checkNotNull(priv);
		this.keyCrypt = checkNotNull(crypter);
	}

	private int ascertainParentFingerprint(DeterKey parentKey, int parentFingerprint) throws IllegalArgumentException {
		if (parentFingerprint != 0) {
			if (parent != null)
				checkArgument(parent.getFingerprint() == parentFingerprint, "parent fingerprint mismatch",
						Integer.toHexString(parent.getFingerprint()), Integer.toHexString(parentFingerprint));
			return parentFingerprint;
		} else
			return 0;
	}

	private DeterKey(ImmutableList<Child> childNumberPath, byte[] chainCode, LazyECPoint publicAsPoint,
			@Nullable DeterKey parent, int depth, int parentFingerprint) {
		super(null, compressPoint(checkNotNull(publicAsPoint)));
		checkArgument(chainCode.length == 32);
		this.parent = parent;
		this.childNumberPath = checkNotNull(childNumberPath);
		this.chainCode = Arrays.copyOf(chainCode, chainCode.length);
		this.depth = depth;
		this.parentFingerprint = ascertainParentFingerprint(parent, parentFingerprint);
	}

	private DeterKey(ImmutableList<Child> childNumberPath, byte[] chainCode, BigInteger priv, @Nullable DeterKey parent,
			int depth, int parentFingerprint) {
		super(priv, compressPoint(ECKey.publicPointFromPrivate(priv)));
		checkArgument(chainCode.length == 32);
		this.parent = parent;
		this.childNumberPath = checkNotNull(childNumberPath);
		this.chainCode = Arrays.copyOf(chainCode, chainCode.length);
		this.depth = depth;
		this.parentFingerprint = ascertainParentFingerprint(parent, parentFingerprint);
	}

	public DeterKey(DeterKey keyToClone, DeterKey newParent) {
		super(keyToClone.priv, keyToClone.pub.get());
		this.parent = newParent;
		this.childNumberPath = keyToClone.childNumberPath;
		this.chainCode = keyToClone.chainCode;
		this.encryptedPrivateKey = keyToClone.encryptedPrivateKey;
		this.depth = this.childNumberPath.size();
		this.parentFingerprint = this.parent.getFingerprint();
	}

	public ImmutableList<Child> getPath() {
		return childNumberPath;
	}

	public String getPathAsString() {
		return HDUtils.formatPath(getPath());
	}

	public int getDepth() {
		return depth;
	}

	public Child getChildNumber() {
		return childNumberPath.size() == 0 ? Child.ZERO : childNumberPath.get(childNumberPath.size() - 1);
	}

	public byte[] getChainCode() {
		return chainCode;
	}

	public byte[] getIdentifier() {
		return Utils.sha256hash160(getPubKey());
	}

	public int getFingerprint() {

		return ByteBuffer.wrap(Arrays.copyOfRange(getIdentifier(), 0, 4)).getInt();
	}

	@Nullable
	public DeterKey getParent() {
		return parent;
	}

	public int getParentFingerprint() {
		return parentFingerprint;
	}

	public byte[] getPrivKeyBytes33() {
		byte[] bytes33 = new byte[33];
		byte[] priv = getPrivKeyBytes();
		System.arraycopy(priv, 0, bytes33, 33 - priv.length, priv.length);
		return bytes33;
	}

	public DeterKey dropPrivateBytes() {
		if (isPubKeyOnly())
			return this;
		else
			return new DeterKey(getPath(), getChainCode(), pub, null, parent);
	}

	public DeterKey dropParent() {
		DeterKey key = new DeterKey(getPath(), getChainCode(), pub, priv, null);
		key.parentFingerprint = parentFingerprint;
		return key;
	}

	static byte[] addChecksum(byte[] input) {
		int inputLength = input.length;
		byte[] checksummed = new byte[inputLength + 4];
		System.arraycopy(input, 0, checksummed, 0, inputLength);
		byte[] checksum = Sha256Hash.hashTwice(input);
		System.arraycopy(checksum, 0, checksummed, inputLength, 4);
		return checksummed;
	}

	@Override
	public DeterKey encrypt(KeyCrypt keyCrypt, KeyParameter aesKey) throws KeyCryptException {
		throw new UnsupportedOperationException("Must supply a new parent for encryption");
	}

	public DeterKey encrypt(KeyCrypt keyCrypt, KeyParameter aesKey, @Nullable DeterKey newParent)
			throws KeyCryptException {

		checkNotNull(keyCrypt);
		if (newParent != null)
			checkArgument(newParent.isEncrypted());
		final byte[] privKeyBytes = getPrivKeyBytes();
		checkState(privKeyBytes != null, "Private key is not available");
		EncryptData encryptedPrivateKey = keyCrypt.encrypt(privKeyBytes, aesKey);
		DeterKey key = new DeterKey(childNumberPath, chainCode, keyCrypt, pub, encryptedPrivateKey, newParent);
		if (newParent == null)
			key.setCreationTimeSeconds(getCreationTimeSeconds());
		return key;
	}

	@Override
	public boolean isPubKeyOnly() {
		return super.isPubKeyOnly() && (parent == null || parent.isPubKeyOnly());
	}

	@Override
	public boolean hasPrivKey() {
		return findParentWithPrivKey() != null;
	}

	@Nullable
	@Override
	public byte[] getSecretBytes() {
		return priv != null ? getPrivKeyBytes() : null;
	}

	@Override
	public boolean isEncrypted() {
		return priv == null && (super.isEncrypted() || (parent != null && parent.isEncrypted()));
	}

	@Override
	@Nullable
	public KeyCrypt getKeyCrypter() {
		if (keyCrypt != null)
			return keyCrypt;
		else if (parent != null)
			return parent.getKeyCrypter();
		else
			return null;
	}

	@Override
	public ECDSASignature sign(Sha256Hash input, @Nullable KeyParameter aesKey) throws KeyCryptException {
		if (isEncrypted()) {

			return super.sign(input, aesKey);
		} else {

			final BigInteger privateKey = findOrDerivePrivateKey();
			if (privateKey == null) {

				throw new MissingPrivateKeyException();
			}
			return super.doSign(input, privateKey);
		}
	}

	@Override
	public DeterKey decrypt(KeyCrypt keyCrypt, KeyParameter aesKey) throws KeyCryptException {
		checkNotNull(keyCrypt);

		if (this.keyCrypt != null && !this.keyCrypt.equals(keyCrypt))
			throw new KeyCryptException(
					"The keyCrypt being used to decrypt the key is different to the one that was used to encrypt it");
		BigInteger privKey = findOrDeriveEncryptedPrivateKey(keyCrypt, aesKey);
		DeterKey key = new DeterKey(childNumberPath, chainCode, privKey, parent);
		if (!Arrays.equals(key.getPubKey(), getPubKey()))
			throw new KeyCryptException("Provided AES key is wrong");
		if (parent == null)
			key.setCreationTimeSeconds(getCreationTimeSeconds());
		return key;
	}

	@Override
	public DeterKey decrypt(KeyParameter aesKey) throws KeyCryptException {
		return (DeterKey) super.decrypt(aesKey);
	}

	private BigInteger findOrDeriveEncryptedPrivateKey(KeyCrypt keyCrypt, KeyParameter aesKey) {
		if (encryptedPrivateKey != null)
			return new BigInteger(1, keyCrypt.decrypt(encryptedPrivateKey, aesKey));

		DeterKey cursor = parent;
		while (cursor != null) {
			if (cursor.encryptedPrivateKey != null)
				break;
			cursor = cursor.parent;
		}
		if (cursor == null)
			throw new KeyCryptException("Neither this key nor its parents have an encrypted private key");
		byte[] parentalPrivateKeyBytes = keyCrypt.decrypt(cursor.encryptedPrivateKey, aesKey);
		return derivePrivateKeyDownwards(cursor, parentalPrivateKeyBytes);
	}

	private DeterKey findParentWithPrivKey() {
		DeterKey cursor = this;
		while (cursor != null) {
			if (cursor.priv != null)
				break;
			cursor = cursor.parent;
		}
		return cursor;
	}

	@Nullable
	private BigInteger findOrDerivePrivateKey() {
		DeterKey cursor = findParentWithPrivKey();
		if (cursor == null)
			return null;
		return derivePrivateKeyDownwards(cursor, cursor.priv.toByteArray());
	}

	private BigInteger derivePrivateKeyDownwards(DeterKey cursor, byte[] parentalPrivateKeyBytes) {
		DeterKey downCursor = new DeterKey(cursor.childNumberPath, cursor.chainCode, cursor.pub,
				new BigInteger(1, parentalPrivateKeyBytes), cursor.parent);

		ImmutableList<Child> path = childNumberPath.subList(cursor.getPath().size(), childNumberPath.size());
		for (Child num : path) {
			downCursor = HDKeyD.deriveChildKey(downCursor, num);
		}

		if (!downCursor.pub.equals(pub))
			throw new KeyCryptException("Could not decrypt bytes");
		return checkNotNull(downCursor.priv);
	}

	public DeterKey derive(int child) {
		return HDKeyD.deriveChildKey(this, new Child(child, true));
	}

	@Override
	public BigInteger getPrivKey() {
		final BigInteger key = findOrDerivePrivateKey();
		checkState(key != null, "Private key bytes not available");
		return key;
	}

	public byte[] serializePublic(NetworkParams params) {
		return serialize(params, true);
	}

	public byte[] serializePrivate(NetworkParams params) {
		return serialize(params, false);
	}

	private byte[] serialize(NetworkParams params, boolean pub) {
		ByteBuffer ser = ByteBuffer.allocate(78);
		ser.putInt(pub ? params.getBip32HeaderPub() : params.getBip32HeaderPriv());
		ser.put((byte) getDepth());
		ser.putInt(getParentFingerprint());
		ser.putInt(getChildNumber().i());
		ser.put(getChainCode());
		ser.put(pub ? getPubKey() : getPrivKeyBytes33());
		checkState(ser.position() == 78);
		return ser.array();
	}

	public String serializePubB58(NetworkParams params) {
		return toBase58(serialize(params, true));
	}

	public String serializePrivB58(NetworkParams params) {
		return toBase58(serialize(params, false));
	}

	static String toBase58(byte[] ser) {
		return Base58.encode(addChecksum(ser));
	}

	public static DeterKey deserializeB58(String base58, NetworkParams params) {
		return deserializeB58(null, base58, params);
	}

	public static DeterKey deserializeB58(@Nullable DeterKey parent, String base58, NetworkParams params) {
		try {
			return deserialize(params, Base58.decodeChecked(base58), parent);
		} catch (NoAddressException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static DeterKey deserialize(NetworkParams params, byte[] serializedKey) {
		return deserialize(params, serializedKey, null);
	}

	public static DeterKey deserialize(NetworkParams params, byte[] serializedKey, @Nullable DeterKey parent) {
		ByteBuffer buffer = ByteBuffer.wrap(serializedKey);
		int header = buffer.getInt();
		if (header != params.getBip32HeaderPriv() && header != params.getBip32HeaderPub())
			throw new IllegalArgumentException("Unknown header bytes: " + toBase58(serializedKey).substring(0, 4));
		boolean pub = header == params.getBip32HeaderPub();
		int depth = buffer.get() & 0xFF;
		final int parentFingerprint = buffer.getInt();
		final int i = buffer.getInt();
		final Child child = new Child(i);
		ImmutableList<Child> path;
		if (parent != null) {
			if (parentFingerprint == 0)
				throw new IllegalArgumentException("Parent was provided but this key doesn't have one");
			if (parent.getFingerprint() != parentFingerprint)
				throw new IllegalArgumentException("Parent fingerprints don't match");
			path = HDUtils.append(parent.getPath(), child);
			if (path.size() != depth)
				throw new IllegalArgumentException("Depth does not match");
		} else {
			if (depth >= 1)

				path = ImmutableList.of(child);
			else
				path = ImmutableList.of();
		}
		byte[] chainCode = new byte[32];
		buffer.get(chainCode);
		byte[] data = new byte[33];
		buffer.get(data);
		checkArgument(!buffer.hasRemaining(), "Found unexpected data in key");
		if (pub) {
			return new DeterKey(path, chainCode, new LazyECPoint(ECKey.CURVE.getCurve(), data), parent, depth,
					parentFingerprint);
		} else {
			return new DeterKey(path, chainCode, new BigInteger(1, data), parent, depth, parentFingerprint);
		}
	}

	@Override
	public long getCreationTimeSeconds() {
		if (parent != null)
			return parent.getCreationTimeSeconds();
		else
			return super.getCreationTimeSeconds();
	}

	@Override
	public void setCreationTimeSeconds(long newCreationTimeSeconds) {
		if (parent != null)
			throw new IllegalStateException("Creation time can only be set on root keys.");
		else
			super.setCreationTimeSeconds(newCreationTimeSeconds);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DeterKey other = (DeterKey) o;

		return super.equals(other) && Arrays.equals(this.chainCode, other.chainCode)
				&& Objects.equal(this.childNumberPath, other.childNumberPath);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + childNumberPath.hashCode();
		result = 31 * result + Arrays.hashCode(chainCode);
		return result;
	}

	@Override
	public String toString() {
		final ToStringHelper helper = Objects.toStringHelper(this).omitNullValues();
		helper.add("pub", Utils.HEX.encode(pub.getEncoded()));
		helper.add("chainCode", HEX.encode(chainCode));
		helper.add("path", getPathAsString());
		if (creationTimeSeconds > 0)
			helper.add("creationTimeSeconds", creationTimeSeconds);
		helper.add("isEncrypted", isEncrypted());
		helper.add("isPubKeyOnly", isPubKeyOnly());
		return helper.toString();
	}

	@Override
	public void formatKeyWithAddress(boolean includePrivateKeys, StringBuilder builder, NetworkParams params) {
		final Address address = toAddress(params);
		builder.append("  addr:").append(address);
		builder.append("  hash160:").append(Utils.HEX.encode(getPubKeyHash()));
		builder.append("  (").append(getPathAsString()).append(")\n");
		if (includePrivateKeys) {
			builder.append("  ").append(toStringWithPrivate(params)).append("\n");
		}
	}
}
