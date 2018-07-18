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
package cros.mail.chain.script;

import com.google.common.collect.Lists;

import cros.mail.chain.core.Address;
import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.ProtocolException;
import cros.mail.chain.core.ScriptException;
import cros.mail.chain.core.Sha256Hash;
import cros.mail.chain.core.Transaction;
import cros.mail.chain.core.UnsafeOutput;
import cros.mail.chain.core.Utils;
import cros.mail.chain.encrypt.Signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.google.common.base.Preconditions.*;
import static cros.mail.chain.script.ChainScriptCodes.*;

public class ChainScript {

	public enum ScriptType {

		NO_TYPE, P2PKH, PUB_KEY, P2SH
	}

	public enum VerifyFlag {
		P2SH, NULLDUMMY
	}

	public static final EnumSet<VerifyFlag> ALL_VERIFY_FLAGS = EnumSet.allOf(VerifyFlag.class);

	private static final Logger log = LoggerFactory.getLogger(ChainScript.class);
	public static final long MAX_SCRIPT_ELEMENT_SIZE = 520;
	public static final int SIG_SIZE = 75;

	public static final int MAX_P2SH_SIGOPS = 15;

	protected List<ChainScriptChunk> chunks;

	protected byte[] program;

	private long creationTimeSeconds;

	private ChainScript() {
		chunks = Lists.newArrayList();
	}

	ChainScript(List<ChainScriptChunk> chunks) {
		this.chunks = Collections.unmodifiableList(new ArrayList<ChainScriptChunk>(chunks));
		creationTimeSeconds = Utils.currentTimeSeconds();
	}

	public ChainScript(byte[] programBytes) throws ScriptException {
		program = programBytes;
		parse(programBytes);
		creationTimeSeconds = 0;
	}

	public ChainScript(byte[] programBytes, long creationTimeSeconds) throws ScriptException {
		program = programBytes;
		parse(programBytes);
		this.creationTimeSeconds = creationTimeSeconds;
	}

	public long getCreationTimeSeconds() {
		return creationTimeSeconds;
	}

	public void setCreationTimeSeconds(long creationTimeSeconds) {
		this.creationTimeSeconds = creationTimeSeconds;
	}

	@Override
	public String toString() {
		return Utils.join(chunks);
	}

	public byte[] getProgram() {
		try {

			if (program != null)
				return Arrays.copyOf(program, program.length);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (ChainScriptChunk chunk : chunks) {
				chunk.write(bos);
			}
			program = bos.toByteArray();
			return program;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<ChainScriptChunk> getChunks() {
		return Collections.unmodifiableList(chunks);
	}

	private static final ChainScriptChunk[] STANDARD_TRANSACTION_SCRIPT_CHUNKS = {
			new ChainScriptChunk(ChainScriptCodes.OP_DUP, null, 0),
			new ChainScriptChunk(ChainScriptCodes.OP_HASH160, null, 1),
			new ChainScriptChunk(ChainScriptCodes.OP_EQUALVERIFY, null, 23),
			new ChainScriptChunk(ChainScriptCodes.OP_CHECKSIG, null, 24), };

	private void parse(byte[] program) throws ScriptException {
		chunks = new ArrayList<ChainScriptChunk>(5);
		ByteArrayInputStream bis = new ByteArrayInputStream(program);
		int initialSize = bis.available();
		while (bis.available() > 0) {
			int startLocationInProgram = initialSize - bis.available();
			int opcode = bis.read();

			long dataToRead = -1;
			if (opcode >= 0 && opcode < OP_PUSHDATA1) {

				dataToRead = opcode;
			} else if (opcode == OP_PUSHDATA1) {
				if (bis.available() < 1)
					throw new ScriptException("Unexpected end of script");
				dataToRead = bis.read();
			} else if (opcode == OP_PUSHDATA2) {

				if (bis.available() < 2)
					throw new ScriptException("Unexpected end of script");
				dataToRead = bis.read() | (bis.read() << 8);
			} else if (opcode == OP_PUSHDATA4) {

				if (bis.available() < 4)
					throw new ScriptException("Unexpected end of script");
				dataToRead = ((long) bis.read()) | (((long) bis.read()) << 8) | (((long) bis.read()) << 16)
						| (((long) bis.read()) << 24);
			}

			ChainScriptChunk chunk;
			if (dataToRead == -1) {
				chunk = new ChainScriptChunk(opcode, null, startLocationInProgram);
			} else {
				if (dataToRead > bis.available())
					throw new ScriptException("Push of data element that is larger than remaining data");
				byte[] data = new byte[(int) dataToRead];
				checkState(dataToRead == 0 || bis.read(data, 0, (int) dataToRead) == dataToRead);
				chunk = new ChainScriptChunk(opcode, data, startLocationInProgram);
			}

			for (ChainScriptChunk c : STANDARD_TRANSACTION_SCRIPT_CHUNKS) {
				if (c.equals(chunk))
					chunk = c;
			}
			chunks.add(chunk);
		}
	}

	public boolean isSentToRawPubKey() {
		return chunks.size() == 2 && chunks.get(1).equalsOpCode(OP_CHECKSIG) && !chunks.get(0).isOpCode()
				&& chunks.get(0).data.length > 1;
	}

	public boolean isSentToAddress() {
		return chunks.size() == 5 && chunks.get(0).equalsOpCode(OP_DUP) && chunks.get(1).equalsOpCode(OP_HASH160)
				&& chunks.get(2).data.length == Address.LENGTH && chunks.get(3).equalsOpCode(OP_EQUALVERIFY)
				&& chunks.get(4).equalsOpCode(OP_CHECKSIG);
	}

	@Deprecated
	public boolean isSentToP2SH() {
		return isPayToScriptHash();
	}

	public byte[] getPubKeyHash() throws ScriptException {
		if (isSentToAddress())
			return chunks.get(2).data;
		else if (isPayToScriptHash())
			return chunks.get(1).data;
		else
			throw new ScriptException("ChainScript not in the standard scriptPubKey form");
	}

	public byte[] getPubKey() throws ScriptException {
		if (chunks.size() != 2) {
			throw new ScriptException("ChainScript not of right size, expecting 2 but got " + chunks.size());
		}
		final ChainScriptChunk chunk0 = chunks.get(0);
		final byte[] chunk0data = chunk0.data;
		final ChainScriptChunk chunk1 = chunks.get(1);
		final byte[] chunk1data = chunk1.data;
		if (chunk0data != null && chunk0data.length > 2 && chunk1data != null && chunk1data.length > 2) {

			return chunk1data;
		} else if (chunk1.equalsOpCode(OP_CHECKSIG) && chunk0data != null && chunk0data.length > 2) {

			return chunk0data;
		} else {
			throw new ScriptException("ChainScript did not match expected form: " + this);
		}
	}

	@Deprecated
	public Address getFromAddress(NetworkParams params) throws ScriptException {
		return new Address(params, Utils.sha256hash160(getPubKey()));
	}

	public Address getToAddress(NetworkParams params) throws ScriptException {
		return getToAddress(params, false);
	}

	public Address getToAddress(NetworkParams params, boolean forcePayToPubKey) throws ScriptException {
		if (isSentToAddress())
			return new Address(params, getPubKeyHash());
		else if (isPayToScriptHash())
			return Address.fromP2SHScript(params, this);
		else if (forcePayToPubKey && isSentToRawPubKey())
			return ECKey.fromPublicOnly(getPubKey()).toAddress(params);
		else
			throw new ScriptException("Cannot cast this script to a pay-to-address type");
	}

	public static void writeBytes(OutputStream os, byte[] buf) throws IOException {
		if (buf.length < OP_PUSHDATA1) {
			os.write(buf.length);
			os.write(buf);
		} else if (buf.length < 256) {
			os.write(OP_PUSHDATA1);
			os.write(buf.length);
			os.write(buf);
		} else if (buf.length < 65536) {
			os.write(OP_PUSHDATA2);
			os.write(0xFF & (buf.length));
			os.write(0xFF & (buf.length >> 8));
			os.write(buf);
		} else {
			throw new RuntimeException("Unimplemented");
		}
	}

	public static byte[] createMultiSigOutputScript(int threshold, List<ECKey> pubkeys) {
		checkArgument(threshold > 0);
		checkArgument(threshold <= pubkeys.size());
		checkArgument(pubkeys.size() <= 16);
		if (pubkeys.size() > 3) {
			log.warn("Creating a multi-signature output that is non-standard: {} pubkeys, should be <= 3",
					pubkeys.size());
		}
		try {
			ByteArrayOutputStream bits = new ByteArrayOutputStream();
			bits.write(encodeToOpN(threshold));
			for (ECKey key : pubkeys) {
				writeBytes(bits, key.getPubKey());
			}
			bits.write(encodeToOpN(pubkeys.size()));
			bits.write(OP_CHECKMULTISIG);
			return bits.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] createInputScript(byte[] signature, byte[] pubkey) {
		try {

			ByteArrayOutputStream bits = new UnsafeOutput(signature.length + pubkey.length + 2);
			writeBytes(bits, signature);
			writeBytes(bits, pubkey);
			return bits.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] createInputScript(byte[] signature) {
		try {

			ByteArrayOutputStream bits = new UnsafeOutput(signature.length + 2);
			writeBytes(bits, signature);
			return bits.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ChainScript createEmptyInputScript(@Nullable ECKey key, @Nullable ChainScript redeemScript) {
		if (isSentToAddress()) {
			checkArgument(key != null, "Key required to create pay-to-address input script");
			return ChainScriptBuilder.createInputScript(null, key);
		} else if (isSentToRawPubKey()) {
			return ChainScriptBuilder.createInputScript(null);
		} else if (isPayToScriptHash()) {
			checkArgument(redeemScript != null, "Redeem script required to create P2SH input script");
			return ChainScriptBuilder.createP2SHMultiSigInputScript(null, redeemScript);
		} else {
			throw new ScriptException("Do not understand script type: " + this);
		}
	}

	public ChainScript getScriptSigWithSignature(ChainScript scriptSig, byte[] sigBytes, int index) {
		int sigsPrefixCount = 0;
		int sigsSuffixCount = 0;
		if (isPayToScriptHash()) {
			sigsPrefixCount = 1;
			sigsSuffixCount = 1;
		} else if (isSentToMultiSig()) {
			sigsPrefixCount = 1;
		} else if (isSentToAddress()) {
			sigsSuffixCount = 1;
		}
		return ChainScriptBuilder.updateScriptWithSignature(scriptSig, sigBytes, index, sigsPrefixCount,
				sigsSuffixCount);
	}

	public int getSigInsertionIndex(Sha256Hash hash, ECKey signingKey) {

		List<ChainScriptChunk> existingChunks = chunks.subList(1, chunks.size() - 1);
		ChainScriptChunk redeemScriptChunk = chunks.get(chunks.size() - 1);
		checkNotNull(redeemScriptChunk.data);
		ChainScript redeemScript = new ChainScript(redeemScriptChunk.data);

		int sigCount = 0;
		int myIndex = redeemScript.findKeyInRedeem(signingKey);
		for (ChainScriptChunk chunk : existingChunks) {
			if (chunk.opcode == OP_0) {

			} else {
				checkNotNull(chunk.data);
				if (myIndex < redeemScript.findSigInRedeem(chunk.data, hash))
					return sigCount;
				sigCount++;
			}
		}
		return sigCount;
	}

	private int findKeyInRedeem(ECKey key) {
		checkArgument(chunks.get(0).isOpCode());
		int numKeys = ChainScript.decodeFromOpN(chunks.get(chunks.size() - 2).opcode);
		for (int i = 0; i < numKeys; i++) {
			if (Arrays.equals(chunks.get(1 + i).data, key.getPubKey())) {
				return i;
			}
		}

		throw new IllegalStateException("Could not find matching key " + key.toString() + " in script " + this);
	}

	public List<ECKey> getPubKeys() {
		if (!isSentToMultiSig())
			throw new ScriptException("Only usable for multisig scripts.");

		ArrayList<ECKey> result = Lists.newArrayList();
		int numKeys = ChainScript.decodeFromOpN(chunks.get(chunks.size() - 2).opcode);
		for (int i = 0; i < numKeys; i++)
			result.add(ECKey.fromPublicOnly(chunks.get(1 + i).data));
		return result;
	}

	private int findSigInRedeem(byte[] signatureBytes, Sha256Hash hash) {
		checkArgument(chunks.get(0).isOpCode());
		int numKeys = ChainScript.decodeFromOpN(chunks.get(chunks.size() - 2).opcode);
		Signature signature = Signature.decodeFromBitcoin(signatureBytes, true);
		for (int i = 0; i < numKeys; i++) {
			if (ECKey.fromPublicOnly(chunks.get(i + 1).data).verify(hash, signature)) {
				return i;
			}
		}

		throw new IllegalStateException("Could not find matching key for signature on " + hash.toString() + " sig "
				+ Utils.HEX.encode(signatureBytes));
	}

	private static int getSigOpCount(List<ChainScriptChunk> chunks, boolean accurate) throws ScriptException {
		int sigOps = 0;
		int lastOpCode = OP_INVALIDOPCODE;
		for (ChainScriptChunk chunk : chunks) {
			if (chunk.isOpCode()) {
				switch (chunk.opcode) {
				case OP_CHECKSIG:
				case OP_CHECKSIGVERIFY:
					sigOps++;
					break;
				case OP_CHECKMULTISIG:
				case OP_CHECKMULTISIGVERIFY:
					if (accurate && lastOpCode >= OP_1 && lastOpCode <= OP_16)
						sigOps += decodeFromOpN(lastOpCode);
					else
						sigOps += 20;
					break;
				default:
					break;
				}
				lastOpCode = chunk.opcode;
			}
		}
		return sigOps;
	}

	static int decodeFromOpN(int opcode) {
		checkArgument((opcode == OP_0 || opcode == OP_1NEGATE) || (opcode >= OP_1 && opcode <= OP_16),
				"decodeFromOpN called on non OP_N opcode");
		if (opcode == OP_0)
			return 0;
		else if (opcode == OP_1NEGATE)
			return -1;
		else
			return opcode + 1 - OP_1;
	}

	static int encodeToOpN(int value) {
		checkArgument(value >= -1 && value <= 16,
				"encodeToOpN called for " + value + " which we cannot encode in an opcode.");
		if (value == 0)
			return OP_0;
		else if (value == -1)
			return OP_1NEGATE;
		else
			return value - 1 + OP_1;
	}

	public static int getSigOpCount(byte[] program) throws ScriptException {
		ChainScript chainScript = new ChainScript();
		try {
			chainScript.parse(program);
		} catch (ScriptException e) {

		}
		return getSigOpCount(chainScript.chunks, false);
	}

	public static long getP2SHSigOpCount(byte[] scriptSig) throws ScriptException {
		ChainScript chainScript = new ChainScript();
		try {
			chainScript.parse(scriptSig);
		} catch (ScriptException e) {

		}
		for (int i = chainScript.chunks.size() - 1; i >= 0; i--)
			if (!chainScript.chunks.get(i).isOpCode()) {
				ChainScript subScript = new ChainScript();
				subScript.parse(chainScript.chunks.get(i).data);
				return getSigOpCount(subScript.chunks, true);
			}
		return 0;
	}

	public int getNumberOfSignaturesRequiredToSpend() {
		if (isSentToMultiSig()) {

			ChainScriptChunk nChunk = chunks.get(0);
			return ChainScript.decodeFromOpN(nChunk.opcode);
		} else if (isSentToAddress() || isSentToRawPubKey()) {

			return 1;
		} else if (isPayToScriptHash()) {
			throw new IllegalStateException("For P2SH number of signatures depends on redeem script");
		} else {
			throw new IllegalStateException("Unsupported script type");
		}
	}

	public int getNumberOfBytesRequiredToSpend(@Nullable ECKey pubKey, @Nullable ChainScript redeemScript) {
		if (isPayToScriptHash()) {

			checkArgument(redeemScript != null, "P2SH script requires redeemScript to be spent");
			return redeemScript.getNumberOfSignaturesRequiredToSpend() * SIG_SIZE + redeemScript.getProgram().length;
		} else if (isSentToMultiSig()) {

			return getNumberOfSignaturesRequiredToSpend() * SIG_SIZE + 1;
		} else if (isSentToRawPubKey()) {

			return SIG_SIZE;
		} else if (isSentToAddress()) {

			int uncompressedPubKeySize = 65;
			return SIG_SIZE + (pubKey != null ? pubKey.getPubKey().length : uncompressedPubKeySize);
		} else {
			throw new IllegalStateException("Unsupported script type");
		}
	}

	public boolean isPayToScriptHash() {

		byte[] program = getProgram();
		return program.length == 23 && (program[0] & 0xff) == OP_HASH160 && (program[1] & 0xff) == 0x14
				&& (program[22] & 0xff) == OP_EQUAL;
	}

	public boolean isSentToMultiSig() {
		if (chunks.size() < 4)
			return false;
		ChainScriptChunk chunk = chunks.get(chunks.size() - 1);

		if (!chunk.isOpCode())
			return false;
		if (!(chunk.equalsOpCode(OP_CHECKMULTISIG) || chunk.equalsOpCode(OP_CHECKMULTISIGVERIFY)))
			return false;
		try {

			ChainScriptChunk m = chunks.get(chunks.size() - 2);
			if (!m.isOpCode())
				return false;
			int numKeys = decodeFromOpN(m.opcode);
			if (numKeys < 1 || chunks.size() != 3 + numKeys)
				return false;
			for (int i = 1; i < chunks.size() - 2; i++) {
				if (chunks.get(i).isOpCode())
					return false;
			}

			if (decodeFromOpN(chunks.get(0).opcode) < 1)
				return false;
		} catch (IllegalStateException e) {
			return false;
		}
		return true;
	}

	private static boolean equalsRange(byte[] a, int start, byte[] b) {
		if (start + b.length > a.length)
			return false;
		for (int i = 0; i < b.length; i++)
			if (a[i + start] != b[i])
				return false;
		return true;
	}

	public static byte[] removeAllInstancesOf(byte[] inputScript, byte[] chunkToRemove) {

		UnsafeOutput bos = new UnsafeOutput(inputScript.length);

		int cursor = 0;
		while (cursor < inputScript.length) {
			boolean skip = equalsRange(inputScript, cursor, chunkToRemove);

			int opcode = inputScript[cursor++] & 0xFF;
			int additionalBytes = 0;
			if (opcode >= 0 && opcode < OP_PUSHDATA1) {
				additionalBytes = opcode;
			} else if (opcode == OP_PUSHDATA1) {
				additionalBytes = (0xFF & inputScript[cursor]) + 1;
			} else if (opcode == OP_PUSHDATA2) {
				additionalBytes = ((0xFF & inputScript[cursor]) | ((0xFF & inputScript[cursor + 1]) << 8)) + 2;
			} else if (opcode == OP_PUSHDATA4) {
				additionalBytes = ((0xFF & inputScript[cursor]) | ((0xFF & inputScript[cursor + 1]) << 8)
						| ((0xFF & inputScript[cursor + 1]) << 16) | ((0xFF & inputScript[cursor + 1]) << 24)) + 4;
			}
			if (!skip) {
				try {
					bos.write(opcode);
					bos.write(Arrays.copyOfRange(inputScript, cursor, cursor + additionalBytes));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			cursor += additionalBytes;
		}
		return bos.toByteArray();
	}

	public static byte[] removeAllInstancesOfOp(byte[] inputScript, int opCode) {
		return removeAllInstancesOf(inputScript, new byte[] { (byte) opCode });
	}

	private static boolean castToBool(byte[] data) {
		for (int i = 0; i < data.length; i++) {

			if (data[i] != 0)
				return !(i == data.length - 1 && (data[i] & 0xFF) == 0x80);
		}
		return false;
	}

	private static BigInteger castToBigInteger(byte[] chunk) throws ScriptException {
		if (chunk.length > 4)
			throw new ScriptException("ChainScript attempted to use an integer larger than 4 bytes");
		return Utils.decodeMPI(Utils.reverseBytes(chunk), false);
	}

	public boolean isOpReturn() {
		return chunks.size() == 2 && chunks.get(0).equalsOpCode(OP_RETURN);
	}

	public static void executeScript(@Nullable Transaction txContainingThis, long index, ChainScript chainScript,
			LinkedList<byte[]> stack, boolean enforceNullDummy) throws ScriptException {
		int opCount = 0;
		int lastCodeSepLocation = 0;

		LinkedList<byte[]> altstack = new LinkedList<byte[]>();
		LinkedList<Boolean> ifStack = new LinkedList<Boolean>();

		for (ChainScriptChunk chunk : chainScript.chunks) {
			boolean shouldExecute = !ifStack.contains(false);

			if (chunk.opcode == OP_0) {
				if (!shouldExecute)
					continue;

				stack.add(new byte[] {});
			} else if (!chunk.isOpCode()) {
				if (chunk.data.length > MAX_SCRIPT_ELEMENT_SIZE)
					throw new ScriptException("Attempted to push a data string larger than 520 bytes");

				if (!shouldExecute)
					continue;

				stack.add(chunk.data);
			} else {
				int opcode = chunk.opcode;
				if (opcode > OP_16) {
					opCount++;
					if (opCount > 201)
						throw new ScriptException("More script operations than is allowed");
				}

				if (opcode == OP_VERIF || opcode == OP_VERNOTIF)
					throw new ScriptException("ChainScript included OP_VERIF or OP_VERNOTIF");

				if (opcode == OP_CAT || opcode == OP_SUBSTR || opcode == OP_LEFT || opcode == OP_RIGHT
						|| opcode == OP_INVERT || opcode == OP_AND || opcode == OP_OR || opcode == OP_XOR
						|| opcode == OP_2MUL || opcode == OP_2DIV || opcode == OP_MUL || opcode == OP_DIV
						|| opcode == OP_MOD || opcode == OP_LSHIFT || opcode == OP_RSHIFT)
					throw new ScriptException("ChainScript included a disabled ChainScript Op.");

				switch (opcode) {
				case OP_IF:
					if (!shouldExecute) {
						ifStack.add(false);
						continue;
					}
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_IF on an empty stack");
					ifStack.add(castToBool(stack.pollLast()));
					continue;
				case OP_NOTIF:
					if (!shouldExecute) {
						ifStack.add(false);
						continue;
					}
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_NOTIF on an empty stack");
					ifStack.add(!castToBool(stack.pollLast()));
					continue;
				case OP_ELSE:
					if (ifStack.isEmpty())
						throw new ScriptException("Attempted OP_ELSE without OP_IF/NOTIF");
					ifStack.add(!ifStack.pollLast());
					continue;
				case OP_ENDIF:
					if (ifStack.isEmpty())
						throw new ScriptException("Attempted OP_ENDIF without OP_IF/NOTIF");
					ifStack.pollLast();
					continue;
				}

				if (!shouldExecute)
					continue;

				switch (opcode) {

				case OP_1NEGATE:
					stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false)));
					break;
				case OP_1:
				case OP_2:
				case OP_3:
				case OP_4:
				case OP_5:
				case OP_6:
				case OP_7:
				case OP_8:
				case OP_9:
				case OP_10:
				case OP_11:
				case OP_12:
				case OP_13:
				case OP_14:
				case OP_15:
				case OP_16:
					stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(decodeFromOpN(opcode)), false)));
					break;
				case OP_NOP:
					break;
				case OP_VERIFY:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_VERIFY on an empty stack");
					if (!castToBool(stack.pollLast()))
						throw new ScriptException("OP_VERIFY failed");
					break;
				case OP_RETURN:
					throw new ScriptException("ChainScript called OP_RETURN");
				case OP_TOALTSTACK:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_TOALTSTACK on an empty stack");
					altstack.add(stack.pollLast());
					break;
				case OP_FROMALTSTACK:
					if (altstack.size() < 1)
						throw new ScriptException("Attempted OP_TOALTSTACK on an empty altstack");
					stack.add(altstack.pollLast());
					break;
				case OP_2DROP:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_2DROP on a stack with size < 2");
					stack.pollLast();
					stack.pollLast();
					break;
				case OP_2DUP:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_2DUP on a stack with size < 2");
					Iterator<byte[]> it2DUP = stack.descendingIterator();
					byte[] OP2DUPtmpChunk2 = it2DUP.next();
					stack.add(it2DUP.next());
					stack.add(OP2DUPtmpChunk2);
					break;
				case OP_3DUP:
					if (stack.size() < 3)
						throw new ScriptException("Attempted OP_3DUP on a stack with size < 3");
					Iterator<byte[]> it3DUP = stack.descendingIterator();
					byte[] OP3DUPtmpChunk3 = it3DUP.next();
					byte[] OP3DUPtmpChunk2 = it3DUP.next();
					stack.add(it3DUP.next());
					stack.add(OP3DUPtmpChunk2);
					stack.add(OP3DUPtmpChunk3);
					break;
				case OP_2OVER:
					if (stack.size() < 4)
						throw new ScriptException("Attempted OP_2OVER on a stack with size < 4");
					Iterator<byte[]> it2OVER = stack.descendingIterator();
					it2OVER.next();
					it2OVER.next();
					byte[] OP2OVERtmpChunk2 = it2OVER.next();
					stack.add(it2OVER.next());
					stack.add(OP2OVERtmpChunk2);
					break;
				case OP_2ROT:
					if (stack.size() < 6)
						throw new ScriptException("Attempted OP_2ROT on a stack with size < 6");
					byte[] OP2ROTtmpChunk6 = stack.pollLast();
					byte[] OP2ROTtmpChunk5 = stack.pollLast();
					byte[] OP2ROTtmpChunk4 = stack.pollLast();
					byte[] OP2ROTtmpChunk3 = stack.pollLast();
					byte[] OP2ROTtmpChunk2 = stack.pollLast();
					byte[] OP2ROTtmpChunk1 = stack.pollLast();
					stack.add(OP2ROTtmpChunk3);
					stack.add(OP2ROTtmpChunk4);
					stack.add(OP2ROTtmpChunk5);
					stack.add(OP2ROTtmpChunk6);
					stack.add(OP2ROTtmpChunk1);
					stack.add(OP2ROTtmpChunk2);
					break;
				case OP_2SWAP:
					if (stack.size() < 4)
						throw new ScriptException("Attempted OP_2SWAP on a stack with size < 4");
					byte[] OP2SWAPtmpChunk4 = stack.pollLast();
					byte[] OP2SWAPtmpChunk3 = stack.pollLast();
					byte[] OP2SWAPtmpChunk2 = stack.pollLast();
					byte[] OP2SWAPtmpChunk1 = stack.pollLast();
					stack.add(OP2SWAPtmpChunk3);
					stack.add(OP2SWAPtmpChunk4);
					stack.add(OP2SWAPtmpChunk1);
					stack.add(OP2SWAPtmpChunk2);
					break;
				case OP_IFDUP:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_IFDUP on an empty stack");
					if (castToBool(stack.getLast()))
						stack.add(stack.getLast());
					break;
				case OP_DEPTH:
					stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(stack.size()), false)));
					break;
				case OP_DROP:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_DROP on an empty stack");
					stack.pollLast();
					break;
				case OP_DUP:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_DUP on an empty stack");
					stack.add(stack.getLast());
					break;
				case OP_NIP:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_NIP on a stack with size < 2");
					byte[] OPNIPtmpChunk = stack.pollLast();
					stack.pollLast();
					stack.add(OPNIPtmpChunk);
					break;
				case OP_OVER:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_OVER on a stack with size < 2");
					Iterator<byte[]> itOVER = stack.descendingIterator();
					itOVER.next();
					stack.add(itOVER.next());
					break;
				case OP_PICK:
				case OP_ROLL:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_PICK/OP_ROLL on an empty stack");
					long val = castToBigInteger(stack.pollLast()).longValue();
					if (val < 0 || val >= stack.size())
						throw new ScriptException("OP_PICK/OP_ROLL attempted to get data deeper than stack size");
					Iterator<byte[]> itPICK = stack.descendingIterator();
					for (long i = 0; i < val; i++)
						itPICK.next();
					byte[] OPROLLtmpChunk = itPICK.next();
					if (opcode == OP_ROLL)
						itPICK.remove();
					stack.add(OPROLLtmpChunk);
					break;
				case OP_ROT:
					if (stack.size() < 3)
						throw new ScriptException("Attempted OP_ROT on a stack with size < 3");
					byte[] OPROTtmpChunk3 = stack.pollLast();
					byte[] OPROTtmpChunk2 = stack.pollLast();
					byte[] OPROTtmpChunk1 = stack.pollLast();
					stack.add(OPROTtmpChunk2);
					stack.add(OPROTtmpChunk3);
					stack.add(OPROTtmpChunk1);
					break;
				case OP_SWAP:
				case OP_TUCK:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_SWAP on a stack with size < 2");
					byte[] OPSWAPtmpChunk2 = stack.pollLast();
					byte[] OPSWAPtmpChunk1 = stack.pollLast();
					stack.add(OPSWAPtmpChunk2);
					stack.add(OPSWAPtmpChunk1);
					if (opcode == OP_TUCK)
						stack.add(OPSWAPtmpChunk2);
					break;
				case OP_CAT:
				case OP_SUBSTR:
				case OP_LEFT:
				case OP_RIGHT:
					throw new ScriptException("Attempted to use disabled ChainScript Op.");
				case OP_SIZE:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_SIZE on an empty stack");
					stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(stack.getLast().length), false)));
					break;
				case OP_INVERT:
				case OP_AND:
				case OP_OR:
				case OP_XOR:
					throw new ScriptException("Attempted to use disabled ChainScript Op.");
				case OP_EQUAL:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_EQUALVERIFY on a stack with size < 2");
					stack.add(Arrays.equals(stack.pollLast(), stack.pollLast()) ? new byte[] { 1 } : new byte[] { 0 });
					break;
				case OP_EQUALVERIFY:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_EQUALVERIFY on a stack with size < 2");
					if (!Arrays.equals(stack.pollLast(), stack.pollLast()))
						throw new ScriptException("OP_EQUALVERIFY: non-equal data");
					break;
				case OP_1ADD:
				case OP_1SUB:
				case OP_NEGATE:
				case OP_ABS:
				case OP_NOT:
				case OP_0NOTEQUAL:
					if (stack.size() < 1)
						throw new ScriptException("Attempted a numeric op on an empty stack");
					BigInteger numericOPnum = castToBigInteger(stack.pollLast());

					switch (opcode) {
					case OP_1ADD:
						numericOPnum = numericOPnum.add(BigInteger.ONE);
						break;
					case OP_1SUB:
						numericOPnum = numericOPnum.subtract(BigInteger.ONE);
						break;
					case OP_NEGATE:
						numericOPnum = numericOPnum.negate();
						break;
					case OP_ABS:
						if (numericOPnum.signum() < 0)
							numericOPnum = numericOPnum.negate();
						break;
					case OP_NOT:
						if (numericOPnum.equals(BigInteger.ZERO))
							numericOPnum = BigInteger.ONE;
						else
							numericOPnum = BigInteger.ZERO;
						break;
					case OP_0NOTEQUAL:
						if (numericOPnum.equals(BigInteger.ZERO))
							numericOPnum = BigInteger.ZERO;
						else
							numericOPnum = BigInteger.ONE;
						break;
					default:
						throw new AssertionError("Unreachable");
					}

					stack.add(Utils.reverseBytes(Utils.encodeMPI(numericOPnum, false)));
					break;
				case OP_2MUL:
				case OP_2DIV:
					throw new ScriptException("Attempted to use disabled ChainScript Op.");
				case OP_ADD:
				case OP_SUB:
				case OP_BOOLAND:
				case OP_BOOLOR:
				case OP_NUMEQUAL:
				case OP_NUMNOTEQUAL:
				case OP_LESSTHAN:
				case OP_GREATERTHAN:
				case OP_LESSTHANOREQUAL:
				case OP_GREATERTHANOREQUAL:
				case OP_MIN:
				case OP_MAX:
					if (stack.size() < 2)
						throw new ScriptException("Attempted a numeric op on a stack with size < 2");
					BigInteger numericOPnum2 = castToBigInteger(stack.pollLast());
					BigInteger numericOPnum1 = castToBigInteger(stack.pollLast());

					BigInteger numericOPresult;
					switch (opcode) {
					case OP_ADD:
						numericOPresult = numericOPnum1.add(numericOPnum2);
						break;
					case OP_SUB:
						numericOPresult = numericOPnum1.subtract(numericOPnum2);
						break;
					case OP_BOOLAND:
						if (!numericOPnum1.equals(BigInteger.ZERO) && !numericOPnum2.equals(BigInteger.ZERO))
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_BOOLOR:
						if (!numericOPnum1.equals(BigInteger.ZERO) || !numericOPnum2.equals(BigInteger.ZERO))
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_NUMEQUAL:
						if (numericOPnum1.equals(numericOPnum2))
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_NUMNOTEQUAL:
						if (!numericOPnum1.equals(numericOPnum2))
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_LESSTHAN:
						if (numericOPnum1.compareTo(numericOPnum2) < 0)
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_GREATERTHAN:
						if (numericOPnum1.compareTo(numericOPnum2) > 0)
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_LESSTHANOREQUAL:
						if (numericOPnum1.compareTo(numericOPnum2) <= 0)
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_GREATERTHANOREQUAL:
						if (numericOPnum1.compareTo(numericOPnum2) >= 0)
							numericOPresult = BigInteger.ONE;
						else
							numericOPresult = BigInteger.ZERO;
						break;
					case OP_MIN:
						if (numericOPnum1.compareTo(numericOPnum2) < 0)
							numericOPresult = numericOPnum1;
						else
							numericOPresult = numericOPnum2;
						break;
					case OP_MAX:
						if (numericOPnum1.compareTo(numericOPnum2) > 0)
							numericOPresult = numericOPnum1;
						else
							numericOPresult = numericOPnum2;
						break;
					default:
						throw new RuntimeException("Opcode switched at runtime?");
					}

					stack.add(Utils.reverseBytes(Utils.encodeMPI(numericOPresult, false)));
					break;
				case OP_MUL:
				case OP_DIV:
				case OP_MOD:
				case OP_LSHIFT:
				case OP_RSHIFT:
					throw new ScriptException("Attempted to use disabled ChainScript Op.");
				case OP_NUMEQUALVERIFY:
					if (stack.size() < 2)
						throw new ScriptException("Attempted OP_NUMEQUALVERIFY on a stack with size < 2");
					BigInteger OPNUMEQUALVERIFYnum2 = castToBigInteger(stack.pollLast());
					BigInteger OPNUMEQUALVERIFYnum1 = castToBigInteger(stack.pollLast());

					if (!OPNUMEQUALVERIFYnum1.equals(OPNUMEQUALVERIFYnum2))
						throw new ScriptException("OP_NUMEQUALVERIFY failed");
					break;
				case OP_WITHIN:
					if (stack.size() < 3)
						throw new ScriptException("Attempted OP_WITHIN on a stack with size < 3");
					BigInteger OPWITHINnum3 = castToBigInteger(stack.pollLast());
					BigInteger OPWITHINnum2 = castToBigInteger(stack.pollLast());
					BigInteger OPWITHINnum1 = castToBigInteger(stack.pollLast());
					if (OPWITHINnum2.compareTo(OPWITHINnum1) <= 0 && OPWITHINnum1.compareTo(OPWITHINnum3) < 0)
						stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE, false)));
					else
						stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.ZERO, false)));
					break;
				case OP_RIPEMD160:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_RIPEMD160 on an empty stack");
					RIPEMD160Digest digest = new RIPEMD160Digest();
					byte[] dataToHash = stack.pollLast();
					digest.update(dataToHash, 0, dataToHash.length);
					byte[] ripmemdHash = new byte[20];
					digest.doFinal(ripmemdHash, 0);
					stack.add(ripmemdHash);
					break;
				case OP_SHA1:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_SHA1 on an empty stack");
					try {
						stack.add(MessageDigest.getInstance("SHA-1").digest(stack.pollLast()));
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
					break;
				case OP_SHA256:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_SHA256 on an empty stack");
					stack.add(Sha256Hash.hash(stack.pollLast()));
					break;
				case OP_HASH160:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_HASH160 on an empty stack");
					stack.add(Utils.sha256hash160(stack.pollLast()));
					break;
				case OP_HASH256:
					if (stack.size() < 1)
						throw new ScriptException("Attempted OP_SHA256 on an empty stack");
					stack.add(Sha256Hash.hashTwice(stack.pollLast()));
					break;
				case OP_CODESEPARATOR:
					lastCodeSepLocation = chunk.getStartLocationInProgram() + 1;
					break;
				case OP_CHECKSIG:
				case OP_CHECKSIGVERIFY:
					if (txContainingThis == null)
						throw new IllegalStateException("ChainScript attempted signature check but no tx was provided");
					executeCheckSig(txContainingThis, (int) index, chainScript, stack, lastCodeSepLocation, opcode);
					break;
				case OP_CHECKMULTISIG:
				case OP_CHECKMULTISIGVERIFY:
					if (txContainingThis == null)
						throw new IllegalStateException("ChainScript attempted signature check but no tx was provided");
					opCount = executeMultiSig(txContainingThis, (int) index, chainScript, stack, opCount,
							lastCodeSepLocation, opcode, enforceNullDummy);
					break;
				case OP_NOP1:
				case OP_NOP2:
				case OP_NOP3:
				case OP_NOP4:
				case OP_NOP5:
				case OP_NOP6:
				case OP_NOP7:
				case OP_NOP8:
				case OP_NOP9:
				case OP_NOP10:
					break;

				default:
					throw new ScriptException("ChainScript used a reserved opcode " + opcode);
				}
			}

			if (stack.size() + altstack.size() > 1000 || stack.size() + altstack.size() < 0)
				throw new ScriptException("Stack size exceeded range");
		}

		if (!ifStack.isEmpty())
			throw new ScriptException("OP_IF/OP_NOTIF without OP_ENDIF");
	}

	private static void executeCheckSig(Transaction txContainingThis, int index, ChainScript chainScript,
			LinkedList<byte[]> stack, int lastCodeSepLocation, int opcode) throws ScriptException {
		if (stack.size() < 2)
			throw new ScriptException("Attempted OP_CHECKSIG(VERIFY) on a stack with size < 2");
		byte[] pubKey = stack.pollLast();
		byte[] sigBytes = stack.pollLast();

		byte[] prog = chainScript.getProgram();
		byte[] connectedScript = Arrays.copyOfRange(prog, lastCodeSepLocation, prog.length);

		UnsafeOutput outStream = new UnsafeOutput(sigBytes.length + 1);
		try {
			writeBytes(outStream, sigBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		connectedScript = removeAllInstancesOf(connectedScript, outStream.toByteArray());

		boolean sigValid = false;
		try {
			Signature sig = Signature.decodeFromBitcoin(sigBytes, false);
			Sha256Hash hash = txContainingThis.hashForSignature(index, connectedScript, (byte) sig.sighashFlags);
			sigValid = ECKey.verify(hash.getBytes(), sig, pubKey);
		} catch (Exception e1) {

			if (!e1.getMessage().contains("Reached past end of ASN.1 stream"))
				log.warn("Signature checking failed! {}", e1.toString());
		}

		if (opcode == OP_CHECKSIG)
			stack.add(sigValid ? new byte[] { 1 } : new byte[] { 0 });
		else if (opcode == OP_CHECKSIGVERIFY)
			if (!sigValid)
				throw new ScriptException("ChainScript failed OP_CHECKSIGVERIFY");
	}

	private static int executeMultiSig(Transaction txContainingThis, int index, ChainScript chainScript,
			LinkedList<byte[]> stack, int opCount, int lastCodeSepLocation, int opcode, boolean enforceNullDummy)
			throws ScriptException {
		if (stack.size() < 2)
			throw new ScriptException("Attempted OP_CHECKMULTISIG(VERIFY) on a stack with size < 2");
		int pubKeyCount = castToBigInteger(stack.pollLast()).intValue();
		if (pubKeyCount < 0 || pubKeyCount > 20)
			throw new ScriptException("OP_CHECKMULTISIG(VERIFY) with pubkey count out of range");
		opCount += pubKeyCount;
		if (opCount > 201)
			throw new ScriptException("Total op count > 201 during OP_CHECKMULTISIG(VERIFY)");
		if (stack.size() < pubKeyCount + 1)
			throw new ScriptException("Attempted OP_CHECKMULTISIG(VERIFY) on a stack with size < num_of_pubkeys + 2");

		LinkedList<byte[]> pubkeys = new LinkedList<byte[]>();
		for (int i = 0; i < pubKeyCount; i++) {
			byte[] pubKey = stack.pollLast();
			pubkeys.add(pubKey);
		}

		int sigCount = castToBigInteger(stack.pollLast()).intValue();
		if (sigCount < 0 || sigCount > pubKeyCount)
			throw new ScriptException("OP_CHECKMULTISIG(VERIFY) with sig count out of range");
		if (stack.size() < sigCount + 1)
			throw new ScriptException(
					"Attempted OP_CHECKMULTISIG(VERIFY) on a stack with size < num_of_pubkeys + num_of_signatures + 3");

		LinkedList<byte[]> sigs = new LinkedList<byte[]>();
		for (int i = 0; i < sigCount; i++) {
			byte[] sig = stack.pollLast();
			sigs.add(sig);
		}

		byte[] prog = chainScript.getProgram();
		byte[] connectedScript = Arrays.copyOfRange(prog, lastCodeSepLocation, prog.length);

		for (byte[] sig : sigs) {
			UnsafeOutput outStream = new UnsafeOutput(sig.length + 1);
			try {
				writeBytes(outStream, sig);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			connectedScript = removeAllInstancesOf(connectedScript, outStream.toByteArray());
		}

		boolean valid = true;
		while (sigs.size() > 0) {
			byte[] pubKey = pubkeys.pollFirst();

			try {
				Signature sig = Signature.decodeFromBitcoin(sigs.getFirst(), false);
				Sha256Hash hash = txContainingThis.hashForSignature(index, connectedScript, (byte) sig.sighashFlags);
				if (ECKey.verify(hash.getBytes(), sig, pubKey))
					sigs.pollFirst();
			} catch (Exception e) {

			}

			if (sigs.size() > pubkeys.size()) {
				valid = false;
				break;
			}
		}

		byte[] nullDummy = stack.pollLast();
		if (enforceNullDummy && nullDummy.length > 0)
			throw new ScriptException(
					"OP_CHECKMULTISIG(VERIFY) with non-null nulldummy: " + Arrays.toString(nullDummy));

		if (opcode == OP_CHECKMULTISIG) {
			stack.add(valid ? new byte[] { 1 } : new byte[] { 0 });
		} else if (opcode == OP_CHECKMULTISIGVERIFY) {
			if (!valid)
				throw new ScriptException("ChainScript failed OP_CHECKMULTISIGVERIFY");
		}
		return opCount;
	}

	public void correctlySpends(Transaction txContainingThis, long scriptSigIndex, ChainScript scriptPubKey)
			throws ScriptException {
		correctlySpends(txContainingThis, scriptSigIndex, scriptPubKey, ALL_VERIFY_FLAGS);
	}

	public void correctlySpends(Transaction txContainingThis, long scriptSigIndex, ChainScript scriptPubKey,
			Set<VerifyFlag> verifyFlags) throws ScriptException {

		try {
			txContainingThis = new Transaction(txContainingThis.getParams(), txContainingThis.bitcoinSerialize());
		} catch (ProtocolException e) {
			throw new RuntimeException(e);
		}
		if (getProgram().length > 10000 || scriptPubKey.getProgram().length > 10000)
			throw new ScriptException("ChainScript larger than 10,000 bytes");

		LinkedList<byte[]> stack = new LinkedList<byte[]>();
		LinkedList<byte[]> p2shStack = null;

		executeScript(txContainingThis, scriptSigIndex, this, stack, verifyFlags.contains(VerifyFlag.NULLDUMMY));
		if (verifyFlags.contains(VerifyFlag.P2SH))
			p2shStack = new LinkedList<byte[]>(stack);
		executeScript(txContainingThis, scriptSigIndex, scriptPubKey, stack,
				verifyFlags.contains(VerifyFlag.NULLDUMMY));

		if (stack.size() == 0)
			throw new ScriptException("Stack empty at end of script execution.");

		if (!castToBool(stack.pollLast()))
			throw new ScriptException("ChainScript resulted in a non-true stack: " + stack);

		if (verifyFlags.contains(VerifyFlag.P2SH) && scriptPubKey.isPayToScriptHash()) {
			for (ChainScriptChunk chunk : chunks)
				if (chunk.isOpCode() && chunk.opcode > OP_16)
					throw new ScriptException(
							"Attempted to spend a P2SH scriptPubKey with a script that contained script ops");

			byte[] scriptPubKeyBytes = p2shStack.pollLast();
			ChainScript scriptPubKeyP2SH = new ChainScript(scriptPubKeyBytes);

			executeScript(txContainingThis, scriptSigIndex, scriptPubKeyP2SH, p2shStack,
					verifyFlags.contains(VerifyFlag.NULLDUMMY));

			if (p2shStack.size() == 0)
				throw new ScriptException("P2SH stack empty at end of script execution.");

			if (!castToBool(p2shStack.pollLast()))
				throw new ScriptException("P2SH script execution resulted in a non-true stack");
		}
	}

	private byte[] getQuickProgram() {
		if (program != null)
			return program;
		return getProgram();
	}

	public ScriptType getScriptType() {
		ScriptType type = ScriptType.NO_TYPE;
		if (isSentToAddress()) {
			type = ScriptType.P2PKH;
		} else if (isSentToRawPubKey()) {
			type = ScriptType.PUB_KEY;
		} else if (isPayToScriptHash()) {
			type = ScriptType.P2SH;
		}
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ChainScript other = (ChainScript) o;
		return Arrays.equals(getQuickProgram(), other.getQuickProgram());
	}

	@Override
	public int hashCode() {
		byte[] bytes = getQuickProgram();
		return Arrays.hashCode(bytes);
	}
}
