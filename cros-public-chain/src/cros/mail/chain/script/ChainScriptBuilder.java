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
import cros.mail.chain.core.Utils;
import cros.mail.chain.encrypt.Signature;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static cros.mail.chain.script.ChainScriptCodes.*;

public class ChainScriptBuilder {
	private List<ChainScriptChunk> chunks;

	public ChainScriptBuilder() {
		chunks = Lists.newLinkedList();
	}

	public ChainScriptBuilder(ChainScript template) {
		chunks = new ArrayList<ChainScriptChunk>(template.getChunks());
	}

	public ChainScriptBuilder addChunk(ChainScriptChunk chunk) {
		return addChunk(chunks.size(), chunk);
	}

	public ChainScriptBuilder addChunk(int index, ChainScriptChunk chunk) {
		chunks.add(index, chunk);
		return this;
	}

	public ChainScriptBuilder op(int opcode) {
		return op(chunks.size(), opcode);
	}

	public ChainScriptBuilder op(int index, int opcode) {
		checkArgument(opcode > OP_PUSHDATA4);
		return addChunk(index, new ChainScriptChunk(opcode, null));
	}

	public ChainScriptBuilder data(byte[] data) {
		if (data.length == 0)
			return smallNum(0);
		else
			return data(chunks.size(), data);
	}

	public ChainScriptBuilder data(int index, byte[] data) {

		byte[] copy = Arrays.copyOf(data, data.length);
		int opcode;
		if (data.length == 0) {
			opcode = OP_0;
		} else if (data.length == 1) {
			byte b = data[0];
			if (b >= 1 && b <= 16)
				opcode = ChainScript.encodeToOpN(b);
			else
				opcode = 1;
		} else if (data.length < OP_PUSHDATA1) {
			opcode = data.length;
		} else if (data.length < 256) {
			opcode = OP_PUSHDATA1;
		} else if (data.length < 65536) {
			opcode = OP_PUSHDATA2;
		} else {
			throw new RuntimeException("Unimplemented");
		}
		return addChunk(index, new ChainScriptChunk(opcode, copy));
	}

	public ChainScriptBuilder smallNum(int num) {
		return smallNum(chunks.size(), num);
	}

	public ChainScriptBuilder smallNum(int index, int num) {
		checkArgument(num >= 0, "Cannot encode negative numbers with smallNum");
		checkArgument(num <= 16, "Cannot encode numbers larger than 16 with smallNum");
		return addChunk(index, new ChainScriptChunk(ChainScript.encodeToOpN(num), null));
	}

	public ChainScript build() {
		return new ChainScript(chunks);
	}

	public static ChainScript createOutputScript(Address to) {
		if (to.isP2SHAddress()) {

			return new ChainScriptBuilder().op(OP_HASH160).data(to.getHash160()).op(OP_EQUAL).build();
		} else {

			return new ChainScriptBuilder().op(OP_DUP).op(OP_HASH160).data(to.getHash160()).op(OP_EQUALVERIFY)
					.op(OP_CHECKSIG).build();
		}
	}

	public static ChainScript createOutputScript(ECKey key) {
		return new ChainScriptBuilder().data(key.getPubKey()).op(OP_CHECKSIG).build();
	}

	public static ChainScript createInputScript(@Nullable Signature signature, ECKey pubKey) {
		byte[] pubkeyBytes = pubKey.getPubKey();
		byte[] sigBytes = signature != null ? signature.encodeToBitcoin() : new byte[] {};
		return new ChainScriptBuilder().data(sigBytes).data(pubkeyBytes).build();
	}

	public static ChainScript createInputScript(@Nullable Signature signature) {
		byte[] sigBytes = signature != null ? signature.encodeToBitcoin() : new byte[] {};
		return new ChainScriptBuilder().data(sigBytes).build();
	}

	public static ChainScript createMultiSigOutputScript(int threshold, List<ECKey> pubkeys) {
		checkArgument(threshold > 0);
		checkArgument(threshold <= pubkeys.size());
		checkArgument(pubkeys.size() <= 16);
		ChainScriptBuilder builder = new ChainScriptBuilder();
		builder.smallNum(threshold);
		for (ECKey key : pubkeys) {
			builder.data(key.getPubKey());
		}
		builder.smallNum(pubkeys.size());
		builder.op(OP_CHECKMULTISIG);
		return builder.build();
	}

	public static ChainScript createMultiSigInputScript(List<Signature> signatures) {
		List<byte[]> sigs = new ArrayList<byte[]>(signatures.size());
		for (Signature signature : signatures) {
			sigs.add(signature.encodeToBitcoin());
		}

		return createMultiSigInputScriptBytes(sigs, null);
	}

	public static ChainScript createMultiSigInputScript(Signature... signatures) {
		return createMultiSigInputScript(Arrays.asList(signatures));
	}

	public static ChainScript createMultiSigInputScriptBytes(List<byte[]> signatures) {
		return createMultiSigInputScriptBytes(signatures, null);
	}

	public static ChainScript createP2SHMultiSigInputScript(@Nullable List<Signature> signatures,
			ChainScript multisigProgram) {
		List<byte[]> sigs = new ArrayList<byte[]>();
		if (signatures == null) {

			int numSigs = multisigProgram.getNumberOfSignaturesRequiredToSpend();
			for (int i = 0; i < numSigs; i++)
				sigs.add(new byte[] {});
		} else {
			for (Signature signature : signatures) {
				sigs.add(signature.encodeToBitcoin());
			}
		}
		return createMultiSigInputScriptBytes(sigs, multisigProgram.getProgram());
	}

	public static ChainScript createMultiSigInputScriptBytes(List<byte[]> signatures,
			@Nullable byte[] multisigProgramBytes) {
		checkArgument(signatures.size() <= 16);
		ChainScriptBuilder builder = new ChainScriptBuilder();
		builder.smallNum(0);
		for (byte[] signature : signatures)
			builder.data(signature);
		if (multisigProgramBytes != null)
			builder.data(multisigProgramBytes);
		return builder.build();
	}

	public static ChainScript updateScriptWithSignature(ChainScript scriptSig, byte[] signature, int targetIndex,
			int sigsPrefixCount, int sigsSuffixCount) {
		ChainScriptBuilder builder = new ChainScriptBuilder();
		List<ChainScriptChunk> inputChunks = scriptSig.getChunks();
		int totalChunks = inputChunks.size();

		boolean hasMissingSigs = inputChunks.get(totalChunks - sigsSuffixCount - 1).equalsOpCode(OP_0);
		checkArgument(hasMissingSigs, "ScriptSig is already filled with signatures");

		for (ChainScriptChunk chunk : inputChunks.subList(0, sigsPrefixCount))
			builder.addChunk(chunk);

		int pos = 0;
		boolean inserted = false;
		for (ChainScriptChunk chunk : inputChunks.subList(sigsPrefixCount, totalChunks - sigsSuffixCount)) {
			if (pos == targetIndex) {
				inserted = true;
				builder.data(signature);
				pos++;
			}
			if (!chunk.equalsOpCode(OP_0)) {
				builder.addChunk(chunk);
				pos++;
			}
		}

		while (pos < totalChunks - sigsPrefixCount - sigsSuffixCount) {
			if (pos == targetIndex) {
				inserted = true;
				builder.data(signature);
			} else {
				builder.addChunk(new ChainScriptChunk(OP_0, null));
			}
			pos++;
		}

		for (ChainScriptChunk chunk : inputChunks.subList(totalChunks - sigsSuffixCount, totalChunks))
			builder.addChunk(chunk);

		checkState(inserted);
		return builder.build();
	}

	public static ChainScript createP2SHOutputScript(byte[] hash) {
		checkArgument(hash.length == 20);
		return new ChainScriptBuilder().op(OP_HASH160).data(hash).op(OP_EQUAL).build();
	}

	public static ChainScript createP2SHOutputScript(ChainScript redeemScript) {
		byte[] hash = Utils.sha256hash160(redeemScript.getProgram());
		return ChainScriptBuilder.createP2SHOutputScript(hash);
	}

	public static ChainScript createP2SHOutputScript(int threshold, List<ECKey> pubkeys) {
		ChainScript redeemScript = createRedeemScript(threshold, pubkeys);
		return createP2SHOutputScript(redeemScript);
	}

	public static ChainScript createRedeemScript(int threshold, List<ECKey> pubkeys) {
		pubkeys = new ArrayList<ECKey>(pubkeys);
		Collections.sort(pubkeys, ECKey.PUBKEY_COMPARATOR);
		return ChainScriptBuilder.createMultiSigOutputScript(threshold, pubkeys);
	}

	public static ChainScript createOpReturnScript(byte[] data) {
		checkArgument(data.length <= 40);
		return new ChainScriptBuilder().op(OP_RETURN).data(data).build();
	}
}
