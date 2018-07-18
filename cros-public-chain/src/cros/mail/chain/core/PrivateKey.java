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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.Arrays;

public class PrivateKey extends Checksum {
	private boolean compressed;

	PrivateKey(NetworkParams params, byte[] keyBytes, boolean compressed) {
		super(params.getDumpedPrivateKeyHeader(), encode(keyBytes, compressed));
		this.compressed = compressed;
	}

	private static byte[] encode(byte[] keyBytes, boolean compressed) {
		Preconditions.checkArgument(keyBytes.length == 32, "Private keys must be 32 bytes");
		if (!compressed) {
			return keyBytes;
		} else {

			byte[] bytes = new byte[33];
			System.arraycopy(keyBytes, 0, bytes, 0, 32);
			bytes[32] = 1;
			return bytes;
		}
	}

	public PrivateKey(NetworkParams params, String encoded) throws NoAddressException {
		super(encoded);
		if (params != null && version != params.getDumpedPrivateKeyHeader())
			throw new NoAddressException("Mismatched version number, trying to cross networks? " + version + " vs "
					+ params.getDumpedPrivateKeyHeader());
		if (bytes.length == 33 && bytes[32] == 1) {
			compressed = true;
			bytes = Arrays.copyOf(bytes, 32);
		} else if (bytes.length == 32) {
			compressed = false;
		} else {
			throw new NoAddressException("Wrong number of bytes for a private key, not 32 or 33");
		}
	}

	public ECKey getKey() {
		final ECKey key = ECKey.fromPrivate(bytes);
		return compressed ? key : key.decompress();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PrivateKey other = (PrivateKey) o;
		return Arrays.equals(bytes, other.bytes) && version == other.version && compressed == other.compressed;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(bytes, version, compressed);
	}
}
