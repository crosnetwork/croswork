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

import java.util.Arrays;

public final class EncryptData {
	public final byte[] initialisationVector;
	public final byte[] encryptedBytes;

	public EncryptData(byte[] initialisationVector, byte[] encryptedBytes) {
		this.initialisationVector = Arrays.copyOf(initialisationVector, initialisationVector.length);
		this.encryptedBytes = Arrays.copyOf(encryptedBytes, encryptedBytes.length);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		EncryptData that = (EncryptData) o;
		return Arrays.equals(encryptedBytes, that.encryptedBytes)
				&& Arrays.equals(initialisationVector, that.initialisationVector);

	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(initialisationVector);
		result = 31 * result + Arrays.hashCode(encryptedBytes);
		return result;
	}

	@Override
	public String toString() {
		return "EncryptData [initialisationVector=" + Arrays.toString(initialisationVector) + ", encryptedPrivateKey="
				+ Arrays.toString(encryptedBytes) + "]";
	}
}
