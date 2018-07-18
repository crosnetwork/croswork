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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedLongs;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;

public class Utils {

	public static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
	public static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES = BITCOIN_SIGNED_MESSAGE_HEADER
			.getBytes(Charsets.UTF_8);

	private static final Joiner SPACE_JOINER = Joiner.on(" ");

	private static BlockingQueue<Boolean> mockSleepQueue;

	public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
		if (b == null) {
			return null;
		}
		byte[] bytes = new byte[numBytes];
		byte[] biBytes = b.toByteArray();
		int start = (biBytes.length == numBytes + 1) ? 1 : 0;
		int length = Math.min(biBytes.length, numBytes);
		System.arraycopy(biBytes, start, bytes, numBytes - length, length);
		return bytes;
	}

	public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
		out[offset] = (byte) (0xFF & (val >> 24));
		out[offset + 1] = (byte) (0xFF & (val >> 16));
		out[offset + 2] = (byte) (0xFF & (val >> 8));
		out[offset + 3] = (byte) (0xFF & val);
	}

	public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
		out[offset] = (byte) (0xFF & val);
		out[offset + 1] = (byte) (0xFF & (val >> 8));
		out[offset + 2] = (byte) (0xFF & (val >> 16));
		out[offset + 3] = (byte) (0xFF & (val >> 24));
	}

	public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
		out[offset] = (byte) (0xFF & val);
		out[offset + 1] = (byte) (0xFF & (val >> 8));
		out[offset + 2] = (byte) (0xFF & (val >> 16));
		out[offset + 3] = (byte) (0xFF & (val >> 24));
		out[offset + 4] = (byte) (0xFF & (val >> 32));
		out[offset + 5] = (byte) (0xFF & (val >> 40));
		out[offset + 6] = (byte) (0xFF & (val >> 48));
		out[offset + 7] = (byte) (0xFF & (val >> 56));
	}

	public static void uint32ToByteStreamLE(long val, OutputStream stream) throws IOException {
		stream.write((int) (0xFF & val));
		stream.write((int) (0xFF & (val >> 8)));
		stream.write((int) (0xFF & (val >> 16)));
		stream.write((int) (0xFF & (val >> 24)));
	}

	public static void int64ToByteStreamLE(long val, OutputStream stream) throws IOException {
		stream.write((int) (0xFF & val));
		stream.write((int) (0xFF & (val >> 8)));
		stream.write((int) (0xFF & (val >> 16)));
		stream.write((int) (0xFF & (val >> 24)));
		stream.write((int) (0xFF & (val >> 32)));
		stream.write((int) (0xFF & (val >> 40)));
		stream.write((int) (0xFF & (val >> 48)));
		stream.write((int) (0xFF & (val >> 56)));
	}

	public static void uint64ToByteStreamLE(BigInteger val, OutputStream stream) throws IOException {
		byte[] bytes = val.toByteArray();
		if (bytes.length > 8) {
			throw new RuntimeException("Input too large to encode into a uint64");
		}
		bytes = reverseBytes(bytes);
		stream.write(bytes);
		if (bytes.length < 8) {
			for (int i = 0; i < 8 - bytes.length; i++)
				stream.write(0);
		}
	}

	public static boolean isLessThanUnsigned(long n1, long n2) {
		return UnsignedLongs.compare(n1, n2) < 0;
	}

	public static boolean isLessThanOrEqualToUnsigned(long n1, long n2) {
		return UnsignedLongs.compare(n1, n2) <= 0;
	}

	public static final BaseEncoding HEX = BaseEncoding.base16().lowerCase();

	public static byte[] reverseBytes(byte[] bytes) {

		byte[] buf = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			buf[i] = bytes[bytes.length - 1 - i];
		return buf;
	}

	public static byte[] reverseDwordBytes(byte[] bytes, int trimLength) {
		checkArgument(bytes.length % 4 == 0);
		checkArgument(trimLength < 0 || trimLength % 4 == 0);

		byte[] rev = new byte[trimLength >= 0 && bytes.length > trimLength ? trimLength : bytes.length];

		for (int i = 0; i < rev.length; i += 4) {
			System.arraycopy(bytes, i, rev, i, 4);
			for (int j = 0; j < 4; j++) {
				rev[i + j] = bytes[i + 3 - j];
			}
		}
		return rev;
	}

	public static long readUint32(byte[] bytes, int offset) {
		return (bytes[offset++] & 0xFFL) | ((bytes[offset++] & 0xFFL) << 8) | ((bytes[offset++] & 0xFFL) << 16)
				| ((bytes[offset] & 0xFFL) << 24);
	}

	public static long readInt64(byte[] bytes, int offset) {
		return (bytes[offset++] & 0xFFL) | ((bytes[offset++] & 0xFFL) << 8) | ((bytes[offset++] & 0xFFL) << 16)
				| ((bytes[offset++] & 0xFFL) << 24) | ((bytes[offset++] & 0xFFL) << 32)
				| ((bytes[offset++] & 0xFFL) << 40) | ((bytes[offset++] & 0xFFL) << 48)
				| ((bytes[offset] & 0xFFL) << 56);
	}

	public static long readUint32BE(byte[] bytes, int offset) {
		return ((bytes[offset] & 0xFFL) << 24) | ((bytes[offset + 1] & 0xFFL) << 16)
				| ((bytes[offset + 2] & 0xFFL) << 8) | (bytes[offset + 3] & 0xFFL);
	}

	public static int readUint16BE(byte[] bytes, int offset) {
		return ((bytes[offset] & 0xff) << 8) | bytes[offset + 1] & 0xff;
	}

	public static byte[] sha256hash160(byte[] input) {
		byte[] sha256 = Sha256Hash.hash(input);
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(sha256, 0, sha256.length);
		byte[] out = new byte[20];
		digest.doFinal(out, 0);
		return out;
	}

	public static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
		byte[] buf;
		if (hasLength) {
			int length = (int) readUint32BE(mpi, 0);
			buf = new byte[length];
			System.arraycopy(mpi, 4, buf, 0, length);
		} else
			buf = mpi;
		if (buf.length == 0)
			return BigInteger.ZERO;
		boolean isNegative = (buf[0] & 0x80) == 0x80;
		if (isNegative)
			buf[0] &= 0x7f;
		BigInteger result = new BigInteger(buf);
		return isNegative ? result.negate() : result;
	}

	public static byte[] encodeMPI(BigInteger value, boolean includeLength) {
		if (value.equals(BigInteger.ZERO)) {
			if (!includeLength)
				return new byte[] {};
			else
				return new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}
		boolean isNegative = value.signum() < 0;
		if (isNegative)
			value = value.negate();
		byte[] array = value.toByteArray();
		int length = array.length;
		if ((array[0] & 0x80) == 0x80)
			length++;
		if (includeLength) {
			byte[] result = new byte[length + 4];
			System.arraycopy(array, 0, result, length - array.length + 3, array.length);
			uint32ToByteArrayBE(length, result, 0);
			if (isNegative)
				result[4] |= 0x80;
			return result;
		} else {
			byte[] result;
			if (length != array.length) {
				result = new byte[length];
				System.arraycopy(array, 0, result, 1, array.length);
			} else
				result = array;
			if (isNegative)
				result[0] |= 0x80;
			return result;
		}
	}

	public static BigInteger decodeCompactBits(long compact) {
		int size = ((int) (compact >> 24)) & 0xFF;
		byte[] bytes = new byte[4 + size];
		bytes[3] = (byte) size;
		if (size >= 1)
			bytes[4] = (byte) ((compact >> 16) & 0xFF);
		if (size >= 2)
			bytes[5] = (byte) ((compact >> 8) & 0xFF);
		if (size >= 3)
			bytes[6] = (byte) (compact & 0xFF);
		return decodeMPI(bytes, true);
	}

	public static long encodeCompactBits(BigInteger value) {
		long result;
		int size = value.toByteArray().length;
		if (size <= 3)
			result = value.longValue() << 8 * (3 - size);
		else
			result = value.shiftRight(8 * (size - 3)).longValue();

		if ((result & 0x00800000L) != 0) {
			result >>= 8;
			size++;
		}
		result |= size << 24;
		result |= value.signum() == -1 ? 0x00800000 : 0;
		return result;
	}

	public static volatile Date mockTime;

	public static Date rollMockClock(int seconds) {
		return rollMockClockMillis(seconds * 1000);
	}

	public static Date rollMockClockMillis(long millis) {
		if (mockTime == null)
			throw new IllegalStateException("You need to use setMockClock() first.");
		mockTime = new Date(mockTime.getTime() + millis);
		return mockTime;
	}

	public static void setMockClock() {
		mockTime = new Date();
	}

	public static void setMockClock(long mockClockSeconds) {
		mockTime = new Date(mockClockSeconds * 1000);
	}

	public static Date now() {
		return mockTime != null ? mockTime : new Date();
	}

	public static long currentTimeMillis() {
		return mockTime != null ? mockTime.getTime() : System.currentTimeMillis();
	}

	public static long currentTimeSeconds() {
		return currentTimeMillis() / 1000;
	}

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	public static String dateTimeFormat(Date dateTime) {
		DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		iso8601.setTimeZone(UTC);
		return iso8601.format(dateTime);
	}

	public static String dateTimeFormat(long dateTime) {
		DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		iso8601.setTimeZone(UTC);
		return iso8601.format(dateTime);
	}

	public static <T> String join(Iterable<T> items) {
		return SPACE_JOINER.join(items);
	}

	public static byte[] copyOf(byte[] in, int length) {
		byte[] out = new byte[length];
		System.arraycopy(in, 0, out, 0, Math.min(length, in.length));
		return out;
	}

	public static byte[] appendByte(byte[] bytes, byte b) {
		byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
		result[result.length - 1] = b;
		return result;
	}

	public static String toString(byte[] bytes, String charsetName) {
		try {
			return new String(bytes, charsetName);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] toBytes(CharSequence str, String charsetName) {
		try {
			return str.toString().getBytes(charsetName);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] parseAsHexOrBase58(String data) {
		try {
			return HEX.decode(data);
		} catch (Exception e) {

			try {
				return Base58.decodeChecked(data);
			} catch (NoAddressException e1) {
				return null;
			}
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	public static byte[] formatMessageForSigning(String message) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
			bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
			byte[] messageBytes = message.getBytes(Charsets.UTF_8);
			VariableInt size = new VariableInt(messageBytes.length);
			bos.write(size.encode());
			bos.write(messageBytes);
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final int[] bitMask = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };

	public static boolean checkBitLE(byte[] data, int index) {
		return (data[index >>> 3] & bitMask[7 & index]) != 0;
	}

	public static void setBitLE(byte[] data, int index) {
		data[index >>> 3] |= bitMask[7 & index];
	}

	public static void sleep(long millis) {
		if (mockSleepQueue == null) {
			sleepUninterruptibly(millis, TimeUnit.MILLISECONDS);
		} else {
			try {
				boolean isMultiPass = mockSleepQueue.take();
				rollMockClockMillis(millis);
				if (isMultiPass)
					mockSleepQueue.offer(true);
			} catch (InterruptedException e) {

			}
		}
	}

	public static void setMockSleep(boolean isEnable) {
		if (isEnable) {
			mockSleepQueue = new ArrayBlockingQueue<Boolean>(1);
			mockTime = new Date(System.currentTimeMillis());
		} else {
			mockSleepQueue = null;
		}
	}

	public static void passMockSleep() {
		mockSleepQueue.offer(false);
	}

	public static void finishMockSleep() {
		if (mockSleepQueue != null) {
			mockSleepQueue.offer(true);
		}
	}

	private static int isAndroid = -1;

	public static boolean isAndroidRuntime() {
		if (isAndroid == -1) {
			final String runtime = System.getProperty("java.runtime.name");
			isAndroid = (runtime != null && runtime.equals("Android Runtime")) ? 1 : 0;
		}
		return isAndroid == 1;
	}

	private static class Pair implements Comparable<Pair> {
		int item, count;

		public Pair(int item, int count) {
			this.count = count;
			this.item = item;
		}

		@Override
		public int compareTo(Pair o) {
			return -Ints.compare(count, o.count);
		}
	}

	public static int maxOfMostFreq(int... items) {

		ArrayList<Integer> list = new ArrayList<Integer>(items.length);
		for (int item : items)
			list.add(item);
		return maxOfMostFreq(list);
	}

	public static int maxOfMostFreq(List<Integer> items) {
		if (items.isEmpty())
			return 0;

		items = Ordering.natural().reverse().sortedCopy(items);
		LinkedList<Pair> pairs = Lists.newLinkedList();
		pairs.add(new Pair(items.get(0), 0));
		for (int item : items) {
			Pair pair = pairs.getLast();
			if (pair.item != item)
				pairs.add((pair = new Pair(item, 0)));
			pair.count++;
		}

		Collections.sort(pairs);
		int maxCount = pairs.getFirst().count;
		int maxItem = pairs.getFirst().item;
		for (Pair pair : pairs) {
			if (pair.count != maxCount)
				break;
			maxItem = Math.max(maxItem, pair.item);
		}
		return maxItem;
	}

	public static String getResourceAsString(URL url) throws IOException {
		List<String> lines = Resources.readLines(url, Charsets.UTF_8);
		return Joiner.on('\n').join(lines);
	}

	public static InputStream closeUnchecked(InputStream stream) {
		try {
			stream.close();
			return stream;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static OutputStream closeUnchecked(OutputStream stream) {
		try {
			stream.close();
			return stream;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
