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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UnsafeOutput extends ByteArrayOutputStream {

	public UnsafeOutput() {
		super(32);
	}

	public UnsafeOutput(int size) {
		super(size);
	}

	@Override
	public void write(int b) {
		int newcount = count + 1;
		if (newcount > buf.length) {
			buf = Utils.copyOf(buf, Math.max(buf.length << 1, newcount));
		}
		buf[count] = (byte) b;
		count = newcount;
	}

	@Override
	public void write(byte[] b, int off, int len) {
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		int newcount = count + len;
		if (newcount > buf.length) {
			buf = Utils.copyOf(buf, Math.max(buf.length << 1, newcount));
		}
		System.arraycopy(b, off, buf, count, len);
		count = newcount;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(buf, 0, count);
	}

	@Override
	public void reset() {
		count = 0;
	}

	@Override
	public byte toByteArray()[] {
		return count == buf.length ? buf : Utils.copyOf(buf, count);
	}

	@Override
	public int size() {
		return count;
	}

}
