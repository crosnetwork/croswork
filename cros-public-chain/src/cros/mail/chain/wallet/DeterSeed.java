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
import cros.mail.chain.core.Utils;
import cros.mail.chain.encrypt.*;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.List;

import static cros.mail.chain.core.Utils.HEX;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class DeterSeed implements EncryptItem {

	public static final int DEFAULT_SEED_ENTROPY_BITS = 128;
	public static final int MAX_SEED_ENTROPY_BITS = 512;

	@Nullable
	private final byte[] seed;
	@Nullable
	private final List<String> mnemonicCode;
	@Nullable
	private final EncryptData encryptedMnemonicCode;
	@Nullable
	private EncryptData encryptedSeed;
	private final long creationTimeSeconds;

	public DeterSeed(String mnemonicCode, byte[] seed, String passphrase, long creationTimeSeconds)
			throws InvalidWalletException {
		this(decodeMnemonicCode(mnemonicCode), seed, passphrase, creationTimeSeconds);
	}

	public DeterSeed(byte[] seed, List<String> mnemonic, long creationTimeSeconds) {
		this.seed = checkNotNull(seed);
		this.mnemonicCode = checkNotNull(mnemonic);
		this.encryptedMnemonicCode = null;
		this.creationTimeSeconds = creationTimeSeconds;
	}

	public DeterSeed(EncryptData encryptedMnemonic, @Nullable EncryptData encryptedSeed,
			long creationTimeSeconds) {
		this.seed = null;
		this.mnemonicCode = null;
		this.encryptedMnemonicCode = checkNotNull(encryptedMnemonic);
		this.encryptedSeed = encryptedSeed;
		this.creationTimeSeconds = creationTimeSeconds;
	}

	public DeterSeed(List<String> mnemonicCode, @Nullable byte[] seed, String passphrase,
			long creationTimeSeconds) {
		this((seed != null ? seed : MnemonicCode.toSeed(mnemonicCode, checkNotNull(passphrase))), mnemonicCode,
				creationTimeSeconds);
	}

	public DeterSeed(SecureRandom random, int bits, String passphrase, long creationTimeSeconds) {
		this(getEntropy(random, bits), checkNotNull(passphrase), creationTimeSeconds);
	}

	public DeterSeed(byte[] entropy, String passphrase, long creationTimeSeconds) {
		checkArgument(entropy.length % 4 == 0, "entropy size in bits not divisible by 32");
		checkArgument(entropy.length * 8 >= DEFAULT_SEED_ENTROPY_BITS, "entropy size too small");
		checkNotNull(passphrase);

		try {
			this.mnemonicCode = MnemonicCode.INSTANCE.toMnemonic(entropy);
		} catch (MnemonicException.MnemonicLengthException e) {

			throw new RuntimeException(e);
		}
		this.seed = MnemonicCode.toSeed(mnemonicCode, passphrase);
		this.encryptedMnemonicCode = null;
		this.creationTimeSeconds = creationTimeSeconds;
	}

	private static byte[] getEntropy(SecureRandom random, int bits) {
		checkArgument(bits <= MAX_SEED_ENTROPY_BITS, "requested entropy size too large");

		byte[] seed = new byte[bits / 8];
		random.nextBytes(seed);
		return seed;
	}

	@Override
	public boolean isEncrypted() {
		checkState(mnemonicCode != null || encryptedMnemonicCode != null);
		return encryptedMnemonicCode != null;
	}

	@Override
	public String toString() {
		return isEncrypted() ? "DeterSeed [encrypted]"
				: "DeterSeed " + toHexString() + " " + Utils.join(mnemonicCode);
	}

	@Nullable
	public String toHexString() {
		return seed != null ? HEX.encode(seed) : null;
	}

	@Nullable
	@Override
	public byte[] getSecretBytes() {
		return getMnemonicAsBytes();
	}

	@Nullable
	public byte[] getSeedBytes() {
		return seed;
	}

	@Nullable
	@Override
	public EncryptData getEncryptedData() {
		return encryptedMnemonicCode;
	}

	@Override
	public Protos.Wallet.EncryptionType getEncryptionType() {
		return Protos.Wallet.EncryptionType.ENCRYPTED_SCRYPT_AES;
	}

	@Nullable
	public EncryptData getEncryptedSeedData() {
		return encryptedSeed;
	}

	@Override
	public long getCreationTimeSeconds() {
		return creationTimeSeconds;
	}

	public DeterSeed encrypt(KeyCrypt keyCrypt, KeyParameter aesKey) {
		checkState(encryptedMnemonicCode == null, "Trying to encrypt seed twice");
		checkState(mnemonicCode != null, "Mnemonic missing so cannot encrypt");
		EncryptData encryptedMnemonic = keyCrypt.encrypt(getMnemonicAsBytes(), aesKey);
		EncryptData encryptedSeed = keyCrypt.encrypt(seed, aesKey);
		return new DeterSeed(encryptedMnemonic, encryptedSeed, creationTimeSeconds);
	}

	private byte[] getMnemonicAsBytes() {
		return Utils.join(mnemonicCode).getBytes(Charsets.UTF_8);
	}

	public DeterSeed decrypt(KeyCrypt crypter, String passphrase, KeyParameter aesKey) {
		checkState(isEncrypted());
		checkNotNull(encryptedMnemonicCode);
		List<String> mnemonic = decodeMnemonicCode(crypter.decrypt(encryptedMnemonicCode, aesKey));
		byte[] seed = encryptedSeed == null ? null : crypter.decrypt(encryptedSeed, aesKey);
		return new DeterSeed(mnemonic, seed, passphrase, creationTimeSeconds);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DeterSeed seed = (DeterSeed) o;

		if (creationTimeSeconds != seed.creationTimeSeconds)
			return false;
		if (encryptedMnemonicCode != null) {
			if (seed.encryptedMnemonicCode == null)
				return false;
			if (!encryptedMnemonicCode.equals(seed.encryptedMnemonicCode))
				return false;
		} else {
			if (!mnemonicCode.equals(seed.mnemonicCode))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = encryptedMnemonicCode != null ? encryptedMnemonicCode.hashCode() : mnemonicCode.hashCode();
		result = 31 * result + (int) (creationTimeSeconds ^ (creationTimeSeconds >>> 32));
		return result;
	}

	public void check() throws MnemonicException {
		if (mnemonicCode != null)
			MnemonicCode.INSTANCE.check(mnemonicCode);
	}

	byte[] getEntropyBytes() throws MnemonicException {
		return MnemonicCode.INSTANCE.toEntropy(mnemonicCode);
	}

	@Nullable
	public List<String> getMnemonicCode() {
		return mnemonicCode;
	}

	private static List<String> decodeMnemonicCode(byte[] mnemonicCode) {
		return decodeMnemonicCode(Utils.toString(mnemonicCode, "UTF-8"));
	}

	private static List<String> decodeMnemonicCode(String mnemonicCode) {
		return Splitter.on(" ").splitToList(mnemonicCode);
	}
}
