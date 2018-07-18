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

import java.util.Locale;

import com.google.common.primitives.Ints;

public class Child implements Comparable<Child> {

	public static final int HARDENED_BIT = 0x80000000;

	public static final Child ZERO = new Child(0);
	public static final Child ONE = new Child(1);
	public static final Child ZERO_HARDENED = new Child(0, true);

	private final int i;

	public Child(int childNumber, boolean isHardened) {
		if (hasHardenedBit(childNumber))
			throw new IllegalArgumentException("Most significant bit is reserved and shouldn't be set: " + childNumber);
		i = isHardened ? (childNumber | HARDENED_BIT) : childNumber;
	}

	public Child(int i) {
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public int i() {
		return i;
	}

	public boolean isHardened() {
		return hasHardenedBit(i);
	}

	private static boolean hasHardenedBit(int a) {
		return (a & HARDENED_BIT) != 0;
	}

	public int num() {
		return i & (~HARDENED_BIT);
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "%d%s", num(), isHardened() ? "H" : "");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Child other = (Child) o;
		return i == other.i;
	}

	@Override
	public int hashCode() {
		return i;
	}

	@Override
	public int compareTo(Child other) {
		return Ints.compare(this.num(), other.num());
	}
}
