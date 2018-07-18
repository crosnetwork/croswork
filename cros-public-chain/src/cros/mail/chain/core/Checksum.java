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

import java.io.Serializable;
import java.util.Arrays;

import com.google.common.base.Objects;
import com.google.common.primitives.UnsignedBytes;

public class Checksum implements Serializable, Cloneable, Comparable<Checksum> {
	protected final int version;
	protected byte[] bytes;

	protected Checksum(String encoded) throws NoAddressException {
		byte[] versionAndDataBytes = Base58.decodeChecked(encoded);
		byte versionByte = versionAndDataBytes[0];
		version = versionByte & 0xFF;
		bytes = new byte[versionAndDataBytes.length - 1];
		System.arraycopy(versionAndDataBytes, 1, bytes, 0, versionAndDataBytes.length - 1);
	}

	protected Checksum(int version, byte[] bytes) {
		checkArgument(version >= 0 && version < 256);
		this.version = version;
		this.bytes = bytes;
	}

	@Override
	public String toString() {

		byte[] addressBytes = new byte[1 + bytes.length + 4];
		addressBytes[0] = (byte) version;
		System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
		byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, bytes.length + 1);
		System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
		return Base58.encode(addressBytes);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(version, Arrays.hashCode(bytes));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Checksum other = (Checksum) o;
		return this.version == other.version && Arrays.equals(this.bytes, other.bytes);
	}

	@Override
	public Checksum clone() throws CloneNotSupportedException {
		return (Checksum) super.clone();
	}

	@Override
	public int compareTo(Checksum o) {
		int versionCompare = Integer.valueOf(this.version).compareTo(Integer.valueOf(o.version));
		if (versionCompare == 0) {

			return UnsignedBytes.lexicographicalComparator().compare(this.bytes, o.bytes);
		} else {
			return versionCompare;
		}
	}

	public int getVersion() {
		return version;
	}
}
