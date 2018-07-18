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

import java.io.*;
import java.math.*;
import java.util.Locale;


import cros.mail.chain.script.ChainScript;

public class Unspent implements Serializable {
	private static final long serialVersionUID = -8744924157056340509L;

	private Coin value;
	private ChainScript chainScript;
	private Sha256Hash hash;
	private long index;
	private int height;
	private boolean coinbase;
	private String address;

	public Unspent(Sha256Hash hash, long index, Coin value, int height, boolean coinbase, ChainScript chainScript) {
		this.hash = hash;
		this.index = index;
		this.value = value;
		this.height = height;
		this.chainScript = chainScript;
		this.coinbase = coinbase;
		this.address = "";
	}

	public Unspent(Sha256Hash hash, long index, Coin value, int height, boolean coinbase, ChainScript chainScript,
			String address) {
		this(hash, index, value, height, coinbase, chainScript);
		this.address = address;
	}

	public Unspent(InputStream in) throws IOException {
		byte[] valueBytes = new byte[8];
		if (in.read(valueBytes, 0, 8) != 8)
			throw new EOFException();
		value = Coin.valueOf(Utils.readInt64(valueBytes, 0));

		int scriptBytesLength = ((in.read() & 0xFF)) | ((in.read() & 0xFF) << 8) | ((in.read() & 0xFF) << 16)
				| ((in.read() & 0xFF) << 24);
		byte[] scriptBytes = new byte[scriptBytesLength];
		if (in.read(scriptBytes) != scriptBytesLength)
			throw new EOFException();
		chainScript = new ChainScript(scriptBytes);

		byte[] hashBytes = new byte[32];
		if (in.read(hashBytes) != 32)
			throw new EOFException();
		hash = Sha256Hash.wrap(hashBytes);

		byte[] indexBytes = new byte[4];
		if (in.read(indexBytes) != 4)
			throw new EOFException();
		index = Utils.readUint32(indexBytes, 0);

		height = ((in.read() & 0xFF)) | ((in.read() & 0xFF) << 8) | ((in.read() & 0xFF) << 16)
				| ((in.read() & 0xFF) << 24);

		byte[] coinbaseByte = new byte[1];
		in.read(coinbaseByte);
		coinbase = coinbaseByte[0] == 1;
	}

	public Coin getValue() {
		return value;
	}

	public ChainScript getScript() {
		return chainScript;
	}

	public Sha256Hash getHash() {
		return hash;
	}

	public long getIndex() {
		return index;
	}

	public int getHeight() {
		return height;
	}

	public boolean isCoinbase() {
		return coinbase;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "Stored TxOut of %s (%s:%d)", value.toFriendlyString(), hash, index);
	}

	@Override
	public int hashCode() {
		return hash.hashCode() + (int) index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Unspent other = (Unspent) o;
		return getHash().equals(other.getHash()) && getIndex() == other.getIndex();
	}

	public void serializeToStream(OutputStream bos) throws IOException {
		Utils.uint64ToByteStreamLE(BigInteger.valueOf(value.value), bos);

		byte[] scriptBytes = chainScript.getProgram();
		bos.write(0xFF & scriptBytes.length);
		bos.write(0xFF & scriptBytes.length >> 8);
		bos.write(0xFF & (scriptBytes.length >> 16));
		bos.write(0xFF & (scriptBytes.length >> 24));
		bos.write(scriptBytes);

		bos.write(hash.getBytes());
		Utils.uint32ToByteStreamLE(index, bos);

		bos.write(0xFF & (height));
		bos.write(0xFF & (height >> 8));
		bos.write(0xFF & (height >> 16));
		bos.write(0xFF & (height >> 24));

		bos.write(new byte[] { (byte) (coinbase ? 1 : 0) });
	}
}
