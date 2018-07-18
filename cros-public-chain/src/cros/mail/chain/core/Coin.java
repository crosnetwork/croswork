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

import com.google.common.math.LongMath;

import cros.mail.chain.misc.MoneyFormat;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

public final class Coin implements Money, Comparable<Coin>, Serializable {

	public static final int SMALLEST_UNIT_EXPONENT = 8;

	private static final long COIN_VALUE = LongMath.pow(10, SMALLEST_UNIT_EXPONENT);

	public static final Coin ZERO = Coin.valueOf(0);

	public static final Coin COIN = Coin.valueOf(COIN_VALUE);

	public static final Coin CENT = COIN.divide(100);

	public static final Coin MILLICOIN = COIN.divide(1000);

	public static final Coin MICROCOIN = MILLICOIN.divide(1000);

	public static final Coin SATOSHI = Coin.valueOf(1);

	public static final Coin FIFTY_COINS = COIN.multiply(50);

	public static final Coin NEGATIVE_SATOSHI = Coin.valueOf(-1);

	public final long value;

	private Coin(final long satoshis) {
		this.value = satoshis;
	}

	public static Coin valueOf(final long satoshis) {
		return new Coin(satoshis);
	}

	@Override
	public int smallestUnitExponent() {
		return SMALLEST_UNIT_EXPONENT;
	}

	@Override
	public long getValue() {
		return value;
	}

	public static Coin valueOf(final int coins, final int cents) {
		checkArgument(cents < 100);
		checkArgument(cents >= 0);
		checkArgument(coins >= 0);
		final Coin coin = COIN.multiply(coins).add(CENT.multiply(cents));
		return coin;
	}

	public static Coin parseCoin(final String str) {
		try {
			long satoshis = new BigDecimal(str).movePointRight(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().longValue();
			return Coin.valueOf(satoshis);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Coin add(final Coin value) {
		return new Coin(LongMath.checkedAdd(this.value, value.value));
	}

	public Coin subtract(final Coin value) {
		return new Coin(LongMath.checkedSubtract(this.value, value.value));
	}

	public Coin multiply(final long factor) {
		return new Coin(LongMath.checkedMultiply(this.value, factor));
	}

	public Coin divide(final long divisor) {
		return new Coin(this.value / divisor);
	}

	public Coin[] divideAndRemainder(final long divisor) {
		return new Coin[] { new Coin(this.value / divisor), new Coin(this.value % divisor) };
	}

	public long divide(final Coin divisor) {
		return this.value / divisor.value;
	}

	public boolean isPositive() {
		return signum() == 1;
	}

	public boolean isNegative() {
		return signum() == -1;
	}

	public boolean isZero() {
		return signum() == 0;
	}

	public boolean isGreaterThan(Coin other) {
		return compareTo(other) > 0;
	}

	public boolean isLessThan(Coin other) {
		return compareTo(other) < 0;
	}

	public Coin shiftLeft(final int n) {
		return new Coin(this.value << n);
	}

	public Coin shiftRight(final int n) {
		return new Coin(this.value >> n);
	}

	@Override
	public int signum() {
		if (this.value == 0)
			return 0;
		return this.value < 0 ? -1 : 1;
	}

	public Coin negate() {
		return new Coin(-this.value);
	}

	public long longValue() {
		return this.value;
	}

	private static final MoneyFormat FRIENDLY_FORMAT = MoneyFormat.BTC.minDecimals(2).repeatOptionalDecimals(1, 6)
			.postfixCode();

	public String toFriendlyString() {
		return FRIENDLY_FORMAT.format(this).toString();
	}

	private static final MoneyFormat PLAIN_FORMAT = MoneyFormat.BTC.minDecimals(0).repeatOptionalDecimals(1, 8)
			.noCode();

	public String toPlainString() {
		return PLAIN_FORMAT.format(this).toString();
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (o == null || o.getClass() != getClass())
			return false;
		final Coin other = (Coin) o;
		if (this.value != other.value)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return (int) this.value;
	}

	@Override
	public int compareTo(final Coin other) {
		if (this.value == other.value)
			return 0;
		return this.value > other.value ? 1 : -1;
	}
}
