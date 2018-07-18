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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import cros.mail.chain.core.ECKey;

import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HDUtils {
	private static final Joiner PATH_JOINER = Joiner.on("/");

	static HMac createHmacSha512Digest(byte[] key) {
		SHA512Digest digest = new SHA512Digest();
		HMac hMac = new HMac(digest);
		hMac.init(new KeyParameter(key));
		return hMac;
	}

	static byte[] hmacSha512(HMac hmacSha512, byte[] input) {
		hmacSha512.reset();
		hmacSha512.update(input, 0, input.length);
		byte[] out = new byte[64];
		hmacSha512.doFinal(out, 0);
		return out;
	}

	public static byte[] hmacSha512(byte[] key, byte[] data) {
		return hmacSha512(createHmacSha512Digest(key), data);
	}

	static byte[] toCompressed(byte[] uncompressedPoint) {
		return ECKey.CURVE.getCurve().decodePoint(uncompressedPoint).getEncoded(true);
	}

	static byte[] longTo4ByteArray(long n) {
		byte[] bytes = Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(n).array(), 4, 8);
		assert bytes.length == 4 : bytes.length;
		return bytes;
	}

	public static ImmutableList<Child> append(List<Child> path, Child child) {
		return ImmutableList.<Child>builder().addAll(path).add(child).build();
	}

	public static ImmutableList<Child> concat(List<Child> path, List<Child> path2) {
		return ImmutableList.<Child>builder().addAll(path).addAll(path2).build();
	}

	public static String formatPath(List<Child> path) {
		return PATH_JOINER.join(Iterables.concat(Collections.singleton("M"), path));
	}

	public static List<Child> parsePath(@Nonnull String path) {
		String[] parsedNodes = path.replace("M", "").split("/");
		List<Child> nodes = new ArrayList<Child>();

		for (String n : parsedNodes) {
			n = n.replaceAll(" ", "");
			if (n.length() == 0)
				continue;
			boolean isHard = n.endsWith("H");
			if (isHard)
				n = n.substring(0, n.length() - 1);
			int nodeNumber = Integer.parseInt(n);
			nodes.add(new Child(nodeNumber, isHard));
		}

		return nodes;
	}
}
