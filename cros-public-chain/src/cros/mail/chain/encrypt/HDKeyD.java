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
import com.google.common.collect.*;

import org.spongycastle.math.ec.*;

import java.math.*;
import java.nio.*;
import java.security.*;
import java.util.*;

import static com.google.common.base.Preconditions.*;

public final class HDKeyD {
	static {

		if (Utils.isAndroidRuntime())
			new LinuxSecureRandom();

		RAND_INT = new BigInteger(256, new SecureRandom());
	}

	private static final BigInteger RAND_INT;

	private HDKeyD() {
	}

	public static final int MAX_CHILD_DERIVATION_ATTEMPTS = 100;

	public static DeterKey createMasterPrivateKey(byte[] seed) throws HDException {
		checkArgument(seed.length > 8, "Seed is too short and could be brute forced");

		byte[] i = HDUtils.hmacSha512(HDUtils.createHmacSha512Digest("Bitcoin seed".getBytes()), seed);

		checkState(i.length == 64, i.length);
		byte[] il = Arrays.copyOfRange(i, 0, 32);
		byte[] ir = Arrays.copyOfRange(i, 32, 64);
		Arrays.fill(i, (byte) 0);
		DeterKey masterPrivKey = createMasterPrivKeyFromBytes(il, ir);
		Arrays.fill(il, (byte) 0);
		Arrays.fill(ir, (byte) 0);

		masterPrivKey.setCreationTimeSeconds(Utils.currentTimeSeconds());
		return masterPrivKey;
	}

	public static DeterKey createMasterPrivKeyFromBytes(byte[] privKeyBytes, byte[] chainCode) throws HDException {
		BigInteger priv = new BigInteger(1, privKeyBytes);
		assertNonZero(priv, "Generated master key is invalid.");
		assertLessThanN(priv, "Generated master key is invalid.");
		return new DeterKey(ImmutableList.<Child>of(), chainCode, priv, null);
	}

	public static DeterKey createMasterPubKeyFromBytes(byte[] pubKeyBytes, byte[] chainCode) {
		return new DeterKey(ImmutableList.<Child>of(), chainCode, new LazyECPoint(ECKey.CURVE.getCurve(), pubKeyBytes),
				null, null);
	}

	public static DeterKey deriveChildKey(DeterKey parent, int childNumber) {
		return deriveChildKey(parent, new Child(childNumber));
	}

	public static DeterKey deriveThisOrNextChildKey(DeterKey parent, int childNumber) {
		int nAttempts = 0;
		Child child = new Child(childNumber);
		boolean isHardened = child.isHardened();
		while (nAttempts < MAX_CHILD_DERIVATION_ATTEMPTS) {
			try {
				child = new Child(child.num() + nAttempts, isHardened);
				return deriveChildKey(parent, child);
			} catch (HDException ignore) {
			}
			nAttempts++;
		}
		throw new HDException(
				"Maximum number of child derivation attempts reached, this is probably an indication of a bug.");

	}

	public static DeterKey deriveChildKey(DeterKey parent, Child child) throws HDException {
		if (!parent.hasPrivKey()) {
			RawKeyBytes rawKey = deriveChildKeyBytesFromPublic(parent, child, PublicDeriveMode.NORMAL);
			return new DeterKey(HDUtils.append(parent.getPath(), child), rawKey.chainCode,
					new LazyECPoint(ECKey.CURVE.getCurve(), rawKey.keyBytes), null, parent);
		} else {
			RawKeyBytes rawKey = deriveChildKeyBytesFromPrivate(parent, child);
			return new DeterKey(HDUtils.append(parent.getPath(), child), rawKey.chainCode,
					new BigInteger(1, rawKey.keyBytes), parent);
		}
	}

	public static RawKeyBytes deriveChildKeyBytesFromPrivate(DeterKey parent, Child child) throws HDException {
		checkArgument(parent.hasPrivKey(), "Parent key must have private key bytes for this method.");
		byte[] parentPublicKey = parent.getPubKeyPoint().getEncoded(true);
		assert parentPublicKey.length == 33 : parentPublicKey.length;
		ByteBuffer data = ByteBuffer.allocate(37);
		if (child.isHardened()) {
			data.put(parent.getPrivKeyBytes33());
		} else {
			data.put(parentPublicKey);
		}
		data.putInt(child.i());
		byte[] i = HDUtils.hmacSha512(parent.getChainCode(), data.array());
		assert i.length == 64 : i.length;
		byte[] il = Arrays.copyOfRange(i, 0, 32);
		byte[] chainCode = Arrays.copyOfRange(i, 32, 64);
		BigInteger ilInt = new BigInteger(1, il);
		assertLessThanN(ilInt, "Illegal derived key: I_L >= n");
		final BigInteger priv = parent.getPrivKey();
		BigInteger ki = priv.add(ilInt).mod(ECKey.CURVE.getN());
		assertNonZero(ki, "Illegal derived key: derived private key equals 0.");
		return new RawKeyBytes(ki.toByteArray(), chainCode);
	}

	public enum PublicDeriveMode {
		NORMAL, WITH_INVERSION
	}

	public static RawKeyBytes deriveChildKeyBytesFromPublic(DeterKey parent, Child child, PublicDeriveMode mode)
			throws HDException {
		checkArgument(!child.isHardened(), "Can't use private derivation with public keys only.");
		byte[] parentPublicKey = parent.getPubKeyPoint().getEncoded(true);
		assert parentPublicKey.length == 33 : parentPublicKey.length;
		ByteBuffer data = ByteBuffer.allocate(37);
		data.put(parentPublicKey);
		data.putInt(child.i());
		byte[] i = HDUtils.hmacSha512(parent.getChainCode(), data.array());
		assert i.length == 64 : i.length;
		byte[] il = Arrays.copyOfRange(i, 0, 32);
		byte[] chainCode = Arrays.copyOfRange(i, 32, 64);
		BigInteger ilInt = new BigInteger(1, il);
		assertLessThanN(ilInt, "Illegal derived key: I_L >= n");

		final BigInteger N = ECKey.CURVE.getN();
		ECPoint Ki;
		switch (mode) {
		case NORMAL:
			Ki = ECKey.publicPointFromPrivate(ilInt).add(parent.getPubKeyPoint());
			break;
		case WITH_INVERSION:

			Ki = ECKey.publicPointFromPrivate(ilInt.add(RAND_INT).mod(N));
			BigInteger additiveInverse = RAND_INT.negate().mod(N);
			Ki = Ki.add(ECKey.publicPointFromPrivate(additiveInverse));
			Ki = Ki.add(parent.getPubKeyPoint());
			break;
		default:
			throw new AssertionError();
		}

		assertNonInfinity(Ki, "Illegal derived key: derived public key equals infinity.");
		return new RawKeyBytes(Ki.getEncoded(true), chainCode);
	}

	private static void assertNonZero(BigInteger integer, String errorMessage) {
		if (integer.equals(BigInteger.ZERO))
			throw new HDException(errorMessage);
	}

	private static void assertNonInfinity(ECPoint point, String errorMessage) {
		if (point.equals(ECKey.CURVE.getCurve().getInfinity()))
			throw new HDException(errorMessage);
	}

	private static void assertLessThanN(BigInteger integer, String errorMessage) {
		if (integer.compareTo(ECKey.CURVE.getN()) > 0)
			throw new HDException(errorMessage);
	}

	public static class RawKeyBytes {
		public final byte[] keyBytes, chainCode;

		public RawKeyBytes(byte[] keyBytes, byte[] chainCode) {
			this.keyBytes = keyBytes;
			this.chainCode = chainCode;
		}
	}
}
