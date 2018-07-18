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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.Transaction;
import cros.mail.chain.core.VeriException;


public class Signature extends ECKey.ECDSASignature {

	public final int sighashFlags;

	public Signature(BigInteger r, BigInteger s) {
		this(r, s, Transaction.SigHash.ALL.ordinal() + 1);
	}

	public Signature(BigInteger r, BigInteger s, int sighashFlags) {
		super(r, s);
		this.sighashFlags = sighashFlags;
	}

	public Signature(ECKey.ECDSASignature signature, Transaction.SigHash mode, boolean anyoneCanPay) {
		super(signature.r, signature.s);
		sighashFlags = calcSigHashValue(mode, anyoneCanPay);
	}

	public static Signature dummy() {
		BigInteger val = ECKey.HALF_CURVE_ORDER;
		return new Signature(val, val);
	}

	public static int calcSigHashValue(Transaction.SigHash mode, boolean anyoneCanPay) {
		int sighashFlags = mode.ordinal() + 1;
		if (anyoneCanPay)
			sighashFlags |= Transaction.SIGHASH_ANYONECANPAY_VALUE;
		return sighashFlags;
	}

	public static boolean isEncodingCanonical(byte[] signature) {

		if (signature.length < 9 || signature.length > 73)
			return false;

		int hashType = signature[signature.length - 1] & ~Transaction.SIGHASH_ANYONECANPAY_VALUE;
		if (hashType < (Transaction.SigHash.ALL.ordinal() + 1) || hashType > (Transaction.SigHash.SINGLE.ordinal() + 1))
			return false;

		if ((signature[0] & 0xff) != 0x30 || (signature[1] & 0xff) != signature.length - 3)
			return false;

		int lenR = signature[3] & 0xff;
		if (5 + lenR >= signature.length || lenR == 0)
			return false;
		int lenS = signature[5 + lenR] & 0xff;
		if (lenR + lenS + 7 != signature.length || lenS == 0)
			return false;

		if (signature[4 - 2] != 0x02 || (signature[4] & 0x80) == 0x80)
			return false;
		if (lenR > 1 && signature[4] == 0x00 && (signature[4 + 1] & 0x80) != 0x80)
			return false;

		if (signature[6 + lenR - 2] != 0x02 || (signature[6 + lenR] & 0x80) == 0x80)
			return false;
		if (lenS > 1 && signature[6 + lenR] == 0x00 && (signature[6 + lenR + 1] & 0x80) != 0x80)
			return false;

		return true;
	}

	public boolean anyoneCanPay() {
		return (sighashFlags & Transaction.SIGHASH_ANYONECANPAY_VALUE) != 0;
	}

	public Transaction.SigHash sigHashMode() {
		final int mode = sighashFlags & 0x1f;
		if (mode == Transaction.SigHash.NONE.ordinal() + 1)
			return Transaction.SigHash.NONE;
		else if (mode == Transaction.SigHash.SINGLE.ordinal() + 1)
			return Transaction.SigHash.SINGLE;
		else
			return Transaction.SigHash.ALL;
	}

	public byte[] encodeToBitcoin() {
		try {
			ByteArrayOutputStream bos = derByteStream();
			bos.write(sighashFlags);
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ECKey.ECDSASignature toCanonicalised() {
		return new Signature(super.toCanonicalised(), sigHashMode(), anyoneCanPay());
	}

	public static Signature decodeFromBitcoin(byte[] bytes, boolean requireCanonical) throws VeriException {

		if (requireCanonical && !isEncodingCanonical(bytes))
			throw new VeriException("Signature encoding is not canonical.");
		ECKey.ECDSASignature sig;
		try {
			sig = ECKey.ECDSASignature.decodeFromDER(bytes);
		} catch (IllegalArgumentException e) {
			throw new VeriException("Could not decode DER", e);
		}

		return new Signature(sig.r, sig.s, bytes[bytes.length - 1]);
	}
}
