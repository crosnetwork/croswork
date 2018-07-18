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

public class VariableInt {
	public final long value;
	private final int originallyEncodedSize;

	public VariableInt(long value) {
		this.value = value;
		originallyEncodedSize = getSizeInBytes();
	}

	public VariableInt(byte[] buf, int offset) {
		int first = 0xFF & buf[offset];
		if (first < 253) {
			value = first;
			originallyEncodedSize = 1;
		} else if (first == 253) {
			value = (0xFF & buf[offset + 1]) | ((0xFF & buf[offset + 2]) << 8);
			originallyEncodedSize = 3;
		} else if (first == 254) {
			value = Utils.readUint32(buf, offset + 1);
			originallyEncodedSize = 5;
		} else {
			value = Utils.readInt64(buf, offset + 1);
			originallyEncodedSize = 9;
		}
	}

	public int getOriginalSizeInBytes() {
		return originallyEncodedSize;
	}

	public int getSizeInBytes() {
		return sizeOf(value);
	}

	public static int sizeOf(long value) {

		if (value < 0)
			return 9;
		if (value < 253)
			return 1;
		if (value <= 0xFFFFL)
			return 3;
		if (value <= 0xFFFFFFFFL)
			return 5;
		return 9;
	}

	public byte[] encode() {
		byte[] bytes;
		switch (sizeOf(value)) {
		case 1:
			return new byte[] { (byte) value };
		case 3:
			return new byte[] { (byte) 253, (byte) (value), (byte) (value >> 8) };
		case 5:
			bytes = new byte[5];
			bytes[0] = (byte) 254;
			Utils.uint32ToByteArrayLE(value, bytes, 1);
			return bytes;
		default:
			bytes = new byte[9];
			bytes[0] = (byte) 255;
			Utils.uint64ToByteArrayLE(value, bytes, 1);
			return bytes;
		}
	}
}
