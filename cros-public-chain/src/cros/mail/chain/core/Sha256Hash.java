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

import com.google.common.io.ByteStreams;
import com.google.common.primitives.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

public class Sha256Hash implements Serializable, Comparable<Sha256Hash> {
	private final byte[] bytes;
	public static final Sha256Hash ZERO_HASH = wrap(new byte[32]);

	@Deprecated
	public Sha256Hash(byte[] rawHashBytes) {
		checkArgument(rawHashBytes.length == 32);
		this.bytes = rawHashBytes;
	}

	@Deprecated
	public Sha256Hash(String hexString) {
		checkArgument(hexString.length() == 64);
		this.bytes = Utils.HEX.decode(hexString);
	}

	@SuppressWarnings("deprecation")
	public static Sha256Hash wrap(byte[] rawHashBytes) {
		return new Sha256Hash(rawHashBytes);
	}

	public static Sha256Hash wrap(String hexString) {
		return wrap(Utils.HEX.decode(hexString));
	}

	@SuppressWarnings("deprecation")
	public static Sha256Hash wrapReversed(byte[] rawHashBytes) {
		return wrap(Utils.reverseBytes(rawHashBytes));
	}

	@Deprecated
	public static Sha256Hash create(byte[] contents) {
		return of(contents);
	}

	public static Sha256Hash of(byte[] contents) {
		return wrap(hash(contents));
	}

	@Deprecated
	public static Sha256Hash createDouble(byte[] contents) {
		return twiceOf(contents);
	}

	public static Sha256Hash twiceOf(byte[] contents) {
		return wrap(hashTwice(contents));
	}

	public static Sha256Hash of(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		try {
			return of(ByteStreams.toByteArray(in));
		} finally {
			in.close();
		}
	}

	public static MessageDigest newDigest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] hash(byte[] input) {
		return hash(input, 0, input.length);
	}

	public static byte[] hash(byte[] input, int offset, int length) {
		MessageDigest digest = newDigest();
		digest.update(input, offset, length);
		return digest.digest();
	}

	public static byte[] hashTwice(byte[] input) {
		return hashTwice(input, 0, input.length);
	}

	public static byte[] hashTwice(byte[] input, int offset, int length) {
		MessageDigest digest = newDigest();
		digest.update(input, offset, length);
		return digest.digest(digest.digest());
	}

	public static byte[] hashTwice(byte[] input1, int offset1, int length1, byte[] input2, int offset2, int length2) {
		MessageDigest digest = newDigest();
		digest.update(input1, offset1, length1);
		digest.update(input2, offset2, length2);
		return digest.digest(digest.digest());
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o != null && getClass() == o.getClass() && Arrays.equals(bytes, ((Sha256Hash) o).bytes);
	}

	@Override
	public int hashCode() {

		return Ints.fromBytes(bytes[28], bytes[29], bytes[30], bytes[31]);
	}

	@Override
	public String toString() {
		return Utils.HEX.encode(bytes);
	}

	public BigInteger toBigInteger() {
		return new BigInteger(1, bytes);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public byte[] getReversedBytes() {
		return Utils.reverseBytes(bytes);
	}

	@Override
	public int compareTo(Sha256Hash o) {
		return this.hashCode() - o.hashCode();
	}
}
