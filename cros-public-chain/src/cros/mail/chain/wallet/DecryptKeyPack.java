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

import org.spongycastle.crypto.params.KeyParameter;

import cros.mail.chain.core.ECKey;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DecryptKeyPack implements KeyPackage {
	protected final KeyPackage target;
	protected final KeyParameter aesKey;

	public DecryptKeyPack(KeyPackage target, @Nullable KeyParameter aesKey) {
		this.target = checkNotNull(target);
		this.aesKey = aesKey;
	}

	@Nullable
	private ECKey maybeDecrypt(ECKey key) {
		if (key == null)
			return null;
		else if (key.isEncrypted()) {
			if (aesKey == null)
				throw new ECKey.KeyIsEncryptedException();
			return key.decrypt(aesKey);
		} else {
			return key;
		}
	}

	private RedeemBlockData maybeDecrypt(RedeemBlockData redeemBlockData) {
		List<ECKey> decryptedKeys = new ArrayList<ECKey>();
		for (ECKey key : redeemBlockData.keys) {
			decryptedKeys.add(maybeDecrypt(key));
		}
		return RedeemBlockData.of(decryptedKeys, redeemBlockData.redeemScript);
	}

	@Nullable
	@Override
	public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
		return maybeDecrypt(target.findKeyFromPubHash(pubkeyHash));
	}

	@Nullable
	@Override
	public ECKey findKeyFromPubKey(byte[] pubkey) {
		return maybeDecrypt(target.findKeyFromPubKey(pubkey));
	}

	@Nullable
	@Override
	public RedeemBlockData findRedeemDataFromScriptHash(byte[] scriptHash) {
		return maybeDecrypt(target.findRedeemDataFromScriptHash(scriptHash));
	}
}
