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

import cros.mail.chain.core.Utils;
import cros.mail.chain.wallet.Protos;
import cros.mail.chain.wallet.Protos.ScryptParameters;
import cros.mail.chain.wallet.Protos.Wallet.EncryptionType;
import com.google.common.base.Objects;
import com.google.protobuf.ByteString;
import com.lambdaworks.crypto.SCrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public class KeyCrypterScrypt implements KeyCrypt, Serializable {

	private static final Logger log = LoggerFactory.getLogger(KeyCrypterScrypt.class);
	private static final long serialVersionUID = 949662512049152670L;

	public static final int KEY_LENGTH = 32;

	public static final int BLOCK_LENGTH = 16;

	public static final int SALT_LENGTH = 8;

	static {

		if (Utils.isAndroidRuntime())
			new SecureRandom();

		secureRandom = new SecureRandom();
	}

	private static final transient SecureRandom secureRandom;

	public static byte[] randomSalt() {
		byte[] salt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(salt);
		return salt;
	}

	private final transient ScryptParameters scryptParameters;

	public KeyCrypterScrypt() {
		Protos.ScryptParameters.Builder scryptParametersBuilder = Protos.ScryptParameters.newBuilder()
				.setSalt(ByteString.copyFrom(randomSalt()));
		this.scryptParameters = scryptParametersBuilder.build();
	}

	public KeyCrypterScrypt(int iterations) {
		Protos.ScryptParameters.Builder scryptParametersBuilder = Protos.ScryptParameters.newBuilder()
				.setSalt(ByteString.copyFrom(randomSalt())).setN(iterations);
		this.scryptParameters = scryptParametersBuilder.build();
	}

	public KeyCrypterScrypt(ScryptParameters scryptParameters) {
		this.scryptParameters = checkNotNull(scryptParameters);

		if (scryptParameters.getSalt() == null || scryptParameters.getSalt().toByteArray() == null
				|| scryptParameters.getSalt().toByteArray().length == 0) {
			log.warn(
					"You are using a ScryptParameters with no salt. Your encryption may be vulnerable to a dictionary attack.");
		}
	}

	@Override
	public KeyParameter deriveKey(CharSequence password) throws KeyCryptException {
		byte[] passwordBytes = null;
		try {
			passwordBytes = convertToByteArray(password);
			byte[] salt = new byte[0];
			if (scryptParameters.getSalt() != null) {
				salt = scryptParameters.getSalt().toByteArray();
			} else {

				log.warn(
						"You are using a ScryptParameters with no salt. Your encryption may be vulnerable to a dictionary attack.");
			}

			byte[] keyBytes = SCrypt.scrypt(passwordBytes, salt, (int) scryptParameters.getN(), scryptParameters.getR(),
					scryptParameters.getP(), KEY_LENGTH);
			return new KeyParameter(keyBytes);
		} catch (Exception e) {
			throw new KeyCryptException("Could not generate key from password and salt.", e);
		} finally {

			if (passwordBytes != null) {
				java.util.Arrays.fill(passwordBytes, (byte) 0);
			}
		}
	}

	@Override
	public EncryptData encrypt(byte[] plainBytes, KeyParameter aesKey) throws KeyCryptException {
		checkNotNull(plainBytes);
		checkNotNull(aesKey);

		try {

			byte[] iv = new byte[BLOCK_LENGTH];
			secureRandom.nextBytes(iv);

			ParametersWithIV keyWithIv = new ParametersWithIV(aesKey, iv);

			BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
			cipher.init(true, keyWithIv);
			byte[] encryptedBytes = new byte[cipher.getOutputSize(plainBytes.length)];
			final int length1 = cipher.processBytes(plainBytes, 0, plainBytes.length, encryptedBytes, 0);
			final int length2 = cipher.doFinal(encryptedBytes, length1);

			return new EncryptData(iv, Arrays.copyOf(encryptedBytes, length1 + length2));
		} catch (Exception e) {
			throw new KeyCryptException("Could not encrypt bytes.", e);
		}
	}

	@Override
	public byte[] decrypt(EncryptData dataToDecrypt, KeyParameter aesKey) throws KeyCryptException {
		checkNotNull(dataToDecrypt);
		checkNotNull(aesKey);

		try {
			ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(aesKey.getKey()),
					dataToDecrypt.initialisationVector);

			BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
			cipher.init(false, keyWithIv);

			byte[] cipherBytes = dataToDecrypt.encryptedBytes;
			byte[] decryptedBytes = new byte[cipher.getOutputSize(cipherBytes.length)];
			final int length1 = cipher.processBytes(cipherBytes, 0, cipherBytes.length, decryptedBytes, 0);
			final int length2 = cipher.doFinal(decryptedBytes, length1);

			return Arrays.copyOf(decryptedBytes, length1 + length2);
		} catch (Exception e) {
			throw new KeyCryptException("Could not decrypt bytes", e);
		}
	}

	private static byte[] convertToByteArray(CharSequence charSequence) {
		checkNotNull(charSequence);

		byte[] byteArray = new byte[charSequence.length() << 1];
		for (int i = 0; i < charSequence.length(); i++) {
			int bytePosition = i << 1;
			byteArray[bytePosition] = (byte) ((charSequence.charAt(i) & 0xFF00) >> 8);
			byteArray[bytePosition + 1] = (byte) (charSequence.charAt(i) & 0x00FF);
		}
		return byteArray;
	}

	public ScryptParameters getScryptParameters() {
		return scryptParameters;
	}

	@Override
	public EncryptionType getUnderstoodEncryptionType() {
		return EncryptionType.ENCRYPTED_SCRYPT_AES;
	}

	@Override
	public String toString() {
		return "Scrypt/AES";
	}

	@Override
	public int hashCode() {
		return com.google.common.base.Objects.hashCode(scryptParameters);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		KeyCrypterScrypt other = (KeyCrypterScrypt) o;
		return Objects.equal(scryptParameters, other.scryptParameters);
	}
}
