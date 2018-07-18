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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import javax.annotation.Nullable;

import cros.mail.chain.param.CrosNetworks;
import cros.mail.chain.script.ChainScript;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Address extends Checksum {

	public static final int LENGTH = 20;

	private transient NetworkParams params;

	public Address(NetworkParams params, int version, byte[] hash160) throws InvalidNetworkException {
		super(version, hash160);
		checkNotNull(params);
		checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
		if (!isAcceptableVersion(params, version))
			throw new InvalidNetworkException(version, params.getAcceptableAddressCodes());
		this.params = params;
	}

	public static Address fromP2SHHash(NetworkParams params, byte[] hash160) {
		try {
			return new Address(params, params.getP2SHHeader(), hash160);
		} catch (InvalidNetworkException e) {
			throw new RuntimeException(e);
		}
	}

	public static Address fromP2SHScript(NetworkParams params, ChainScript scriptPubKey) {
		checkArgument(scriptPubKey.isPayToScriptHash(), "Not a P2SH script");
		return fromP2SHHash(params, scriptPubKey.getPubKeyHash());
	}

	public Address(NetworkParams params, byte[] hash160) {
		super(params.getAddressHeader(), hash160);
		checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
		this.params = params;
	}

	public Address(@Nullable NetworkParams params, String address) throws NoAddressException {
		super(address);
		if (params != null) {
			if (!isAcceptableVersion(params, version)) {
				throw new InvalidNetworkException(version, params.getAcceptableAddressCodes());
			}
			this.params = params;
		} else {
			NetworkParams paramsFound = null;
			for (NetworkParams p : CrosNetworks.get()) {
				if (isAcceptableVersion(p, version)) {
					paramsFound = p;
					break;
				}
			}
			if (paramsFound == null)
				throw new NoAddressException("No network found for " + address);

			this.params = paramsFound;
		}
	}

	public byte[] getHash160() {
		return bytes;
	}

	public boolean isP2SHAddress() {
		final NetworkParams parameters = getParameters();
		return parameters != null && this.version == parameters.p2shHeader;
	}

	public NetworkParams getParameters() {
		return params;
	}

	public static NetworkParams getParametersFromAddress(String address) throws NoAddressException {
		try {
			return new Address(null, address).getParameters();
		} catch (InvalidNetworkException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isAcceptableVersion(NetworkParams params, int version) {
		for (int v : params.getAcceptableAddressCodes()) {
			if (version == v) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Address clone() throws CloneNotSupportedException {
		return (Address) super.clone();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(params.id);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		params = NetworkParams.fromID(in.readUTF());
	}
}
