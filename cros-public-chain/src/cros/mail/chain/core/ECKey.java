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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Comparator;

import javax.annotation.Nullable;
import org.bitcoin.NativeSecp256k1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.ASN1TaggedObject;
import org.spongycastle.asn1.DERBitString;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.spongycastle.util.encoders.Base64;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedBytes;

import cros.mail.chain.encrypt.EncryptData;
import cros.mail.chain.encrypt.EncryptItem;
import cros.mail.chain.encrypt.KeyCrypt;
import cros.mail.chain.encrypt.KeyCryptException;
import cros.mail.chain.encrypt.LazyECPoint;
import cros.mail.chain.encrypt.Signature;
import cros.mail.chain.wallet.Protos;
/**
 * 
 * @author CROS
 *
 */
public class ECKey implements EncryptItem, Serializable {
	private static final Logger log = LoggerFactory.getLogger(ECKey.class);

	public static final Comparator<ECKey> AGE_COMPARATOR = new Comparator<ECKey>() {

		@Override
		public int compare(ECKey k1, ECKey k2) {
			if (k1.creationTimeSeconds == k2.creationTimeSeconds)
				return 0;
			else
				return k1.creationTimeSeconds > k2.creationTimeSeconds ? 1 : -1;
		}
	};

	public static final Comparator<ECKey> PUBKEY_COMPARATOR = new Comparator<ECKey>() {
		private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

		@Override
		public int compare(ECKey k1, ECKey k2) {
			return comparator.compare(k1.getPubKey(), k2.getPubKey());
		}
	};

	private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

	public static final ECDomainParameters CURVE;

	public static final BigInteger HALF_CURVE_ORDER;

	private static final SecureRandom secureRandom;
	private static final long serialVersionUID = -728224901792295832L;

	static {

		if (Utils.isAndroidRuntime())
			new SecureRandom();

		FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
		CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
				CURVE_PARAMS.getH());
		HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
		secureRandom = new SecureRandom();
	}

	protected final BigInteger priv;
	protected final LazyECPoint pub;

	protected long creationTimeSeconds;

	protected KeyCrypt keyCrypt;
	protected EncryptData encryptedPrivateKey;

	private transient byte[] pubKeyHash;

	public ECKey() {
		this(secureRandom);
	}

	public ECKey(SecureRandom secureRandom) {
		ECKeyPairGenerator generator = new ECKeyPairGenerator();
		ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
		generator.init(keygenParams);
		AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
		ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
		ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
		priv = privParams.getD();
		pub = new LazyECPoint(CURVE.getCurve(), pubParams.getQ().getEncoded(true));
		creationTimeSeconds = Utils.currentTimeSeconds();
	}

	protected ECKey(@Nullable BigInteger priv, ECPoint pub) {
		if (priv != null) {

			checkArgument(!priv.equals(BigInteger.ZERO));
			checkArgument(!priv.equals(BigInteger.ONE));
		}
		this.priv = priv;
		this.pub = new LazyECPoint(checkNotNull(pub));
	}

	protected ECKey(@Nullable BigInteger priv, LazyECPoint pub) {
		this.priv = priv;
		this.pub = checkNotNull(pub);
	}

	public static ECPoint compressPoint(ECPoint point) {
		return getPointWithCompression(point, true);
	}

	public static LazyECPoint compressPoint(LazyECPoint point) {
		return point.isCompressed() ? point : new LazyECPoint(compressPoint(point.get()));
	}

	public static ECPoint decompressPoint(ECPoint point) {
		return getPointWithCompression(point, false);
	}

	public static LazyECPoint decompressPoint(LazyECPoint point) {
		return !point.isCompressed() ? point : new LazyECPoint(decompressPoint(point.get()));
	}

	private static ECPoint getPointWithCompression(ECPoint point, boolean compressed) {
		if (point.isCompressed() == compressed)
			return point;
		point = point.normalize();
		BigInteger x = point.getAffineXCoord().toBigInteger();
		BigInteger y = point.getAffineYCoord().toBigInteger();
		return CURVE.getCurve().createPoint(x, y, compressed);
	}

	public static ECKey fromASN1(byte[] asn1privkey) {
		return extractKeyFromASN1(asn1privkey);
	}

	public static ECKey fromPrivate(BigInteger privKey) {
		return fromPrivate(privKey, true);
	}

	public static ECKey fromPrivate(BigInteger privKey, boolean compressed) {
		ECPoint point = publicPointFromPrivate(privKey);
		return new ECKey(privKey, getPointWithCompression(point, compressed));
	}

	public static ECKey fromPrivate(byte[] privKeyBytes) {
		return fromPrivate(new BigInteger(1, privKeyBytes));
	}

	public static ECKey fromPrivate(byte[] privKeyBytes, boolean compressed) {
		return fromPrivate(new BigInteger(1, privKeyBytes), compressed);
	}

	public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
		return new ECKey(priv, pub);
	}

	public static ECKey fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
		checkNotNull(priv);
		checkNotNull(pub);
		return new ECKey(new BigInteger(1, priv), CURVE.getCurve().decodePoint(pub));
	}

	public static ECKey fromPublicOnly(ECPoint pub) {
		return new ECKey(null, pub);
	}

	public static ECKey fromPublicOnly(byte[] pub) {
		return new ECKey(null, CURVE.getCurve().decodePoint(pub));
	}

	public ECKey decompress() {
		if (!pub.isCompressed())
			return this;
		else
			return new ECKey(priv, decompressPoint(pub.get()));
	}

	@Deprecated
	public ECKey(@Nullable byte[] privKeyBytes, @Nullable byte[] pubKey) {
		this(privKeyBytes == null ? null : new BigInteger(1, privKeyBytes), pubKey);
	}

	@Deprecated
	public ECKey(EncryptData encryptedPrivateKey, byte[] pubKey, KeyCrypt keyCrypt) {
		this((byte[]) null, pubKey);

		this.keyCrypt = checkNotNull(keyCrypt);
		this.encryptedPrivateKey = encryptedPrivateKey;
	}

	public static ECKey fromEncrypted(EncryptData encryptedPrivateKey, KeyCrypt crypter, byte[] pubKey) {
		ECKey key = fromPublicOnly(pubKey);
		key.encryptedPrivateKey = checkNotNull(encryptedPrivateKey);
		key.keyCrypt = checkNotNull(crypter);
		return key;
	}

	@Deprecated
	public ECKey(@Nullable BigInteger privKey, @Nullable byte[] pubKey, boolean compressed) {
		if (privKey == null && pubKey == null)
			throw new IllegalArgumentException("ECKey requires at least private or public key");
		this.priv = privKey;
		if (pubKey == null) {

			ECPoint point = publicPointFromPrivate(privKey);
			point = getPointWithCompression(point, compressed);
			this.pub = new LazyECPoint(point);
		} else {

			this.pub = new LazyECPoint(CURVE.getCurve(), pubKey);
		}
	}

	@Deprecated
	private ECKey(@Nullable BigInteger privKey, @Nullable byte[] pubKey) {
		this(privKey, pubKey, false);
	}

	public boolean isPubKeyOnly() {
		return priv == null;
	}

	public boolean hasPrivKey() {
		return priv != null;
	}

	public boolean isWatching() {
		return isPubKeyOnly() && !isEncrypted();
	}

	public byte[] toASN1() {
		try {
			byte[] privKeyBytes = getPrivKeyBytes();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(400);

			DERSequenceGenerator seq = new DERSequenceGenerator(baos);
			seq.addObject(new ASN1Integer(1));
			seq.addObject(new DEROctetString(privKeyBytes));
			seq.addObject(new DERTaggedObject(0, CURVE_PARAMS.toASN1Primitive()));
			seq.addObject(new DERTaggedObject(1, new DERBitString(getPubKey())));
			seq.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
		ECPoint point = publicPointFromPrivate(privKey);
		return point.getEncoded(compressed);
	}

	public static ECPoint publicPointFromPrivate(BigInteger privKey) {

		if (privKey.bitLength() > CURVE.getN().bitLength()) {
			privKey = privKey.mod(CURVE.getN());
		}
		return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
	}

	public byte[] getPubKeyHash() {
		if (pubKeyHash == null)
			pubKeyHash = Utils.sha256hash160(this.pub.getEncoded());
		return pubKeyHash;
	}

	public byte[] getPubKey() {
		return pub.getEncoded();
	}

	public ECPoint getPubKeyPoint() {
		return pub.get();
	}

	public BigInteger getPrivKey() {
		if (priv == null)
			throw new MissingPrivateKeyException();
		return priv;
	}

	public boolean isCompressed() {
		return pub.isCompressed();
	}

	public Address toAddress(NetworkParams params) {
		return new Address(params, getPubKeyHash());
	}

	public static class ECDSASignature {

		public final BigInteger r, s;

		public ECDSASignature(BigInteger r, BigInteger s) {
			this.r = r;
			this.s = s;
		}

		public boolean isCanonical() {
			return s.compareTo(HALF_CURVE_ORDER) <= 0;
		}

		public ECDSASignature toCanonicalised() {
			if (!isCanonical()) {

				return new ECDSASignature(r, CURVE.getN().subtract(s));
			} else {
				return this;
			}
		}

		public byte[] encodeToDER() {
			try {
				return derByteStream().toByteArray();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public static ECDSASignature decodeFromDER(byte[] bytes) {
			ASN1InputStream decoder = null;
			try {
				decoder = new ASN1InputStream(bytes);
				DLSequence seq = (DLSequence) decoder.readObject();
				if (seq == null)
					throw new RuntimeException("Reached past end of ASN.1 stream.");
				ASN1Integer r, s;
				try {
					r = (ASN1Integer) seq.getObjectAt(0);
					s = (ASN1Integer) seq.getObjectAt(1);
				} catch (ClassCastException e) {
					throw new IllegalArgumentException(e);
				}

				return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (decoder != null)
					try {
						decoder.close();
					} catch (IOException x) {
					}
			}
		}

		protected ByteArrayOutputStream derByteStream() throws IOException {

			ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
			DERSequenceGenerator seq = new DERSequenceGenerator(bos);
			seq.addObject(new ASN1Integer(r));
			seq.addObject(new ASN1Integer(s));
			seq.close();
			return bos;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ECDSASignature other = (ECDSASignature) o;
			return r.equals(other.r) && s.equals(other.s);
		}

		@Override
		public int hashCode() {
			int result = r.hashCode();
			result = 31 * result + s.hashCode();
			return result;
		}
	}

	public ECDSASignature sign(Sha256Hash input) throws KeyCryptException {
		return sign(input, null);
	}

	@VisibleForTesting
	public static boolean FAKE_SIGNATURES = false;

	public ECDSASignature sign(Sha256Hash input, @Nullable KeyParameter aesKey) throws KeyCryptException {
		KeyCrypt crypter = getKeyCrypter();
		if (crypter != null) {
			if (aesKey == null)
				throw new KeyIsEncryptedException();
			return decrypt(aesKey).sign(input);
		} else {

			if (priv == null)
				throw new MissingPrivateKeyException();
		}
		return doSign(input, priv);
	}

	protected ECDSASignature doSign(Sha256Hash input, BigInteger privateKeyForSigning) {
		if (FAKE_SIGNATURES)
			return Signature.dummy();
		checkNotNull(privateKeyForSigning);
		ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
		ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
		signer.init(true, privKey);
		BigInteger[] components = signer.generateSignature(input.getBytes());
		return new ECDSASignature(components[0], components[1]).toCanonicalised();
	}

	public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
		if (FAKE_SIGNATURES)
			return true;

		if (NativeSecp256k1.enabled)
			return NativeSecp256k1.verify(data, signature.encodeToDER(), pub);

		ECDSASigner signer = new ECDSASigner();
		ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
		signer.init(false, params);
		try {
			return signer.verifySignature(data, signature.r, signature.s);
		} catch (NullPointerException e) {

			log.error("Caught NPE inside bouncy castle");
			e.printStackTrace();
			return false;
		}
	}

	public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
		if (NativeSecp256k1.enabled)
			return NativeSecp256k1.verify(data, signature, pub);
		return verify(data, ECDSASignature.decodeFromDER(signature), pub);
	}

	public boolean verify(byte[] hash, byte[] signature) {
		return ECKey.verify(hash, signature, getPubKey());
	}

	public boolean verify(Sha256Hash sigHash, ECDSASignature signature) {
		return ECKey.verify(sigHash.getBytes(), signature, getPubKey());
	}

	public void verifyOrThrow(byte[] hash, byte[] signature) throws SignatureException {
		if (!verify(hash, signature))
			throw new SignatureException();
	}

	public void verifyOrThrow(Sha256Hash sigHash, ECDSASignature signature) throws SignatureException {
		if (!ECKey.verify(sigHash.getBytes(), signature, getPubKey()))
			throw new SignatureException();
	}

	public static boolean isPubKeyCanonical(byte[] pubkey) {
		if (pubkey.length < 33)
			return false;
		if (pubkey[0] == 0x04) {

			if (pubkey.length != 65)
				return false;
		} else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {

			if (pubkey.length != 33)
				return false;
		} else
			return false;
		return true;
	}

	private static ECKey extractKeyFromASN1(byte[] asn1privkey) {

		try {
			ASN1InputStream decoder = new ASN1InputStream(asn1privkey);
			DLSequence seq = (DLSequence) decoder.readObject();
			checkArgument(decoder.readObject() == null, "Input contains extra bytes");
			decoder.close();

			checkArgument(seq.size() == 4, "Input does not appear to be an ASN.1 OpenSSL EC private key");

			checkArgument(((ASN1Integer) seq.getObjectAt(0)).getValue().equals(BigInteger.ONE),
					"Input is of wrong version");

			byte[] privbits = ((ASN1OctetString) seq.getObjectAt(1)).getOctets();
			BigInteger privkey = new BigInteger(1, privbits);

			ASN1TaggedObject pubkey = (ASN1TaggedObject) seq.getObjectAt(3);
			checkArgument(pubkey.getTagNo() == 1, "Input has 'publicKey' with bad tag number");
			byte[] pubbits = ((DERBitString) pubkey.getObject()).getBytes();
			checkArgument(pubbits.length == 33 || pubbits.length == 65, "Input has 'publicKey' with invalid length");
			int encoding = pubbits[0] & 0xFF;

			checkArgument(encoding >= 2 && encoding <= 4, "Input has 'publicKey' with invalid encoding");

			boolean compressed = (pubbits.length == 33);
			ECKey key = new ECKey(privkey, null, compressed);
			if (!Arrays.equals(key.getPubKey(), pubbits))
				throw new IllegalArgumentException("Public key in ASN.1 structure does not match private key.");
			return key;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String signMessage(String message) throws KeyCryptException {
		return signMessage(message, null);
	}

	public String signMessage(String message, @Nullable KeyParameter aesKey) throws KeyCryptException {
		byte[] data = Utils.formatMessageForSigning(message);
		Sha256Hash hash = Sha256Hash.twiceOf(data);
		ECDSASignature sig = sign(hash, aesKey);

		int recId = -1;
		for (int i = 0; i < 4; i++) {
			ECKey k = ECKey.recoverFromSignature(i, sig, hash, isCompressed());
			if (k != null && k.pub.equals(pub)) {
				recId = i;
				break;
			}
		}
		if (recId == -1)
			throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
		int headerByte = recId + 27 + (isCompressed() ? 4 : 0);
		byte[] sigData = new byte[65];
		sigData[0] = (byte) headerByte;
		System.arraycopy(Utils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
		System.arraycopy(Utils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);
		return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
	}

	public static ECKey signedMessageToKey(String message, String signatureBase64) throws SignatureException {
		byte[] signatureEncoded;
		try {
			signatureEncoded = Base64.decode(signatureBase64);
		} catch (RuntimeException e) {

			throw new SignatureException("Could not decode base64", e);
		}

		if (signatureEncoded.length < 65)
			throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
		int header = signatureEncoded[0] & 0xFF;

		if (header < 27 || header > 34)
			throw new SignatureException("Header byte out of range: " + header);
		BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
		BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
		ECDSASignature sig = new ECDSASignature(r, s);
		byte[] messageBytes = Utils.formatMessageForSigning(message);

		Sha256Hash messageHash = Sha256Hash.twiceOf(messageBytes);
		boolean compressed = false;
		if (header >= 31) {
			compressed = true;
			header -= 4;
		}
		int recId = header - 27;
		ECKey key = ECKey.recoverFromSignature(recId, sig, messageHash, compressed);
		if (key == null)
			throw new SignatureException("Could not recover public key from signature");
		return key;
	}

	public void verifyMessage(String message, String signatureBase64) throws SignatureException {
		ECKey key = ECKey.signedMessageToKey(message, signatureBase64);
		if (!key.pub.equals(pub))
			throw new SignatureException("Signature did not match for message");
	}

	@Nullable
	public static ECKey recoverFromSignature(int recId, ECDSASignature sig, Sha256Hash message, boolean compressed) {
		Preconditions.checkArgument(recId >= 0, "recId must be positive");
		Preconditions.checkArgument(sig.r.signum() >= 0, "r must be positive");
		Preconditions.checkArgument(sig.s.signum() >= 0, "s must be positive");
		Preconditions.checkNotNull(message);

		BigInteger n = CURVE.getN();
		BigInteger i = BigInteger.valueOf((long) recId / 2);
		BigInteger x = sig.r.add(i.multiply(n));

		BigInteger prime = SecP256K1Curve.q;
		if (x.compareTo(prime) >= 0) {

			return null;
		}

		ECPoint R = decompressKey(x, (recId & 1) == 1);

		if (!R.multiply(n).isInfinity())
			return null;

		BigInteger e = message.toBigInteger();

		BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
		BigInteger rInv = sig.r.modInverse(n);
		BigInteger srInv = rInv.multiply(sig.s).mod(n);
		BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
		ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
		return ECKey.fromPublicOnly(q.getEncoded(compressed));
	}

	private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
		X9IntegerConverter x9 = new X9IntegerConverter();
		byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
		compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
		return CURVE.getCurve().decodePoint(compEnc);
	}

	public byte[] getPrivKeyBytes() {
		return Utils.bigIntegerToBytes(getPrivKey(), 32);
	}

	public PrivateKey getPrivateKeyEncoded(NetworkParams params) {
		return new PrivateKey(params, getPrivKeyBytes(), isCompressed());
	}

	@Override
	public long getCreationTimeSeconds() {
		return creationTimeSeconds;
	}

	public void setCreationTimeSeconds(long newCreationTimeSeconds) {
		if (newCreationTimeSeconds < 0)
			throw new IllegalArgumentException("Cannot set creation time to negative value: " + newCreationTimeSeconds);
		creationTimeSeconds = newCreationTimeSeconds;
	}

	public ECKey encrypt(KeyCrypt keyCrypt, KeyParameter aesKey) throws KeyCryptException {
		checkNotNull(keyCrypt);
		final byte[] privKeyBytes = getPrivKeyBytes();
		EncryptData encryptedPrivateKey = keyCrypt.encrypt(privKeyBytes, aesKey);
		ECKey result = ECKey.fromEncrypted(encryptedPrivateKey, keyCrypt, getPubKey());
		result.setCreationTimeSeconds(creationTimeSeconds);
		return result;
	}

	public ECKey decrypt(KeyCrypt keyCrypt, KeyParameter aesKey) throws KeyCryptException {
		checkNotNull(keyCrypt);

		if (this.keyCrypt != null && !this.keyCrypt.equals(keyCrypt))
			throw new KeyCryptException(
					"The keyCrypt being used to decrypt the key is different to the one that was used to encrypt it");
		checkState(encryptedPrivateKey != null, "This key is not encrypted");
		byte[] unencryptedPrivateKey = keyCrypt.decrypt(encryptedPrivateKey, aesKey);
		ECKey key = ECKey.fromPrivate(unencryptedPrivateKey);
		if (!isCompressed())
			key = key.decompress();
		if (!Arrays.equals(key.getPubKey(), getPubKey()))
			throw new KeyCryptException("Provided AES key is wrong");
		key.setCreationTimeSeconds(creationTimeSeconds);
		return key;
	}

	public ECKey decrypt(KeyParameter aesKey) throws KeyCryptException {
		final KeyCrypt crypter = getKeyCrypter();
		if (crypter == null)
			throw new KeyCryptException("No key crypter available");
		return decrypt(crypter, aesKey);
	}

	public ECKey maybeDecrypt(@Nullable KeyParameter aesKey) throws KeyCryptException {
		return isEncrypted() && aesKey != null ? decrypt(aesKey) : this;
	}

	public static boolean encryptionIsReversible(ECKey originalKey, ECKey encryptedKey, KeyCrypt keyCrypt,
			KeyParameter aesKey) {
		try {
			ECKey rebornUnencryptedKey = encryptedKey.decrypt(keyCrypt, aesKey);
			byte[] originalPrivateKeyBytes = originalKey.getPrivKeyBytes();
			byte[] rebornKeyBytes = rebornUnencryptedKey.getPrivKeyBytes();
			if (!Arrays.equals(originalPrivateKeyBytes, rebornKeyBytes)) {
				log.error("The check that encryption could be reversed failed for {}", originalKey);
				return false;
			}
			return true;
		} catch (KeyCryptException kce) {
			log.error(kce.getMessage());
			return false;
		}
	}

	@Override
	public boolean isEncrypted() {
		return keyCrypt != null && encryptedPrivateKey != null && encryptedPrivateKey.encryptedBytes.length > 0;
	}

	@Nullable
	@Override
	public Protos.Wallet.EncryptionType getEncryptionType() {
		return keyCrypt != null ? keyCrypt.getUnderstoodEncryptionType() : Protos.Wallet.EncryptionType.UNENCRYPTED;
	}

	@Override
	@Nullable
	public byte[] getSecretBytes() {
		if (hasPrivKey())
			return getPrivKeyBytes();
		else
			return null;
	}

	@Nullable
	@Override
	public EncryptData getEncryptedData() {
		return getEncryptedPrivateKey();
	}

	@Nullable
	public EncryptData getEncryptedPrivateKey() {
		return encryptedPrivateKey;
	}

	@Nullable
	public KeyCrypt getKeyCrypter() {
		return keyCrypt;
	}

	public static class MissingPrivateKeyException extends RuntimeException {
	}

	public static class KeyIsEncryptedException extends MissingPrivateKeyException {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof ECKey))
			return false;

		ECKey other = (ECKey) o;

		return Objects.equal(this.priv, other.priv) && Objects.equal(this.pub, other.pub)
				&& Objects.equal(this.creationTimeSeconds, other.creationTimeSeconds)
				&& Objects.equal(this.keyCrypt, other.keyCrypt)
				&& Objects.equal(this.encryptedPrivateKey, other.encryptedPrivateKey);
	}

	@Override
	public int hashCode() {

		byte[] bits = getPubKey();
		return (bits[0] & 0xFF) | ((bits[1] & 0xFF) << 8) | ((bits[2] & 0xFF) << 16) | ((bits[3] & 0xFF) << 24);
	}

	@Override
	public String toString() {
		return toString(false, null);
	}

	public String toStringWithPrivate(NetworkParams params) {
		return toString(true, params);
	}

	public String getPrivateKeyAsHex() {
		return Utils.HEX.encode(getPrivKeyBytes());
	}

	public String getPublicKeyAsHex() {
		return Utils.HEX.encode(pub.getEncoded());
	}

	public String getPrivateKeyAsWiF(NetworkParams params) {
		return getPrivateKeyEncoded(params).toString();
	}

	private String toString(boolean includePrivate, NetworkParams params) {
		final ToStringHelper helper = Objects.toStringHelper(this).omitNullValues();
		helper.add("pub HEX", getPublicKeyAsHex());
		if (includePrivate) {
			try {
				helper.add("priv HEX", getPrivateKeyAsHex());
				helper.add("priv WIF", getPrivateKeyAsWiF(params));
			} catch (IllegalStateException e) {

			}
		}
		if (creationTimeSeconds > 0)
			helper.add("creationTimeSeconds", creationTimeSeconds);
		helper.add("keyCrypt", keyCrypt);
		if (includePrivate)
			helper.add("encryptedPrivateKey", encryptedPrivateKey);
		helper.add("isEncrypted", isEncrypted());
		helper.add("isPubKeyOnly", isPubKeyOnly());
		return helper.toString();
	}

	public void formatKeyWithAddress(boolean includePrivateKeys, StringBuilder builder, NetworkParams params) {
		final Address address = toAddress(params);
		builder.append("  addr:");
		builder.append(address.toString());
		builder.append("  hash160:");
		builder.append(Utils.HEX.encode(getPubKeyHash()));
		if (creationTimeSeconds > 0)
			builder.append("  creationTimeSeconds:").append(creationTimeSeconds);
		builder.append("\n");
		if (includePrivateKeys) {
			builder.append("  ");
			builder.append(toStringWithPrivate(params));
			builder.append("\n");
		}
	}
}
