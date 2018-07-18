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
package cros.mail.chain.misc;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.math.BigDecimal;

import com.google.common.math.LongMath;

import cros.mail.chain.core.Money;

public final class FiatMoney implements Money, Comparable<FiatMoney>, Serializable {

	public static final int SMALLEST_UNIT_EXPONENT = 4;

	public final long value;
	public final String currencyCode;

	private FiatMoney(final String currencyCode, final long value) {
		this.value = value;
		this.currencyCode = currencyCode;
	}

	public static FiatMoney valueOf(final String currencyCode, final long value) {
		return new FiatMoney(currencyCode, value);
	}

	@Override
	public int smallestUnitExponent() {
		return SMALLEST_UNIT_EXPONENT;
	}

	@Override
	public long getValue() {
		return value;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public static FiatMoney parseFiat(final String currencyCode, final String str) {
		try {
			long val = new BigDecimal(str).movePointRight(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().longValue();
			return FiatMoney.valueOf(currencyCode, val);
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public FiatMoney add(final FiatMoney value) {
		checkArgument(value.currencyCode.equals(currencyCode));
		return new FiatMoney(currencyCode, LongMath.checkedAdd(this.value, value.value));
	}

	public FiatMoney subtract(final FiatMoney value) {
		checkArgument(value.currencyCode.equals(currencyCode));
		return new FiatMoney(currencyCode, LongMath.checkedSubtract(this.value, value.value));
	}

	public FiatMoney multiply(final long factor) {
		return new FiatMoney(currencyCode, LongMath.checkedMultiply(this.value, factor));
	}

	public FiatMoney divide(final long divisor) {
		return new FiatMoney(currencyCode, this.value / divisor);
	}

	public FiatMoney[] divideAndRemainder(final long divisor) {
		return new FiatMoney[] { new FiatMoney(currencyCode, this.value / divisor),
				new FiatMoney(currencyCode, this.value % divisor) };
	}

	public long divide(final FiatMoney divisor) {
		checkArgument(divisor.currencyCode.equals(currencyCode));
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

	public boolean isGreaterThan(FiatMoney other) {
		return compareTo(other) > 0;
	}

	public boolean isLessThan(FiatMoney other) {
		return compareTo(other) < 0;
	}

	@Override
	public int signum() {
		if (this.value == 0)
			return 0;
		return this.value < 0 ? -1 : 1;
	}

	public FiatMoney negate() {
		return new FiatMoney(currencyCode, -this.value);
	}

	public long longValue() {
		return this.value;
	}

	private static final MoneyFormat FRIENDLY_FORMAT = MoneyFormat.FIAT.postfixCode();

	public String toFriendlyString() {
		return FRIENDLY_FORMAT.code(0, currencyCode).format(this).toString();
	}

	private static final MoneyFormat PLAIN_FORMAT = MoneyFormat.FIAT.minDecimals(0).repeatOptionalDecimals(1, 4)
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
		final FiatMoney other = (FiatMoney) o;
		if (this.value != other.value)
			return false;
		if (!this.currencyCode.equals(other.currencyCode))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return (int) this.value + 37 * this.currencyCode.hashCode();
	}

	@Override
	public int compareTo(final FiatMoney other) {
		if (!this.currencyCode.equals(other.currencyCode))
			return this.currencyCode.compareTo(other.currencyCode);
		if (this.value != other.value)
			return this.value > other.value ? 1 : -1;
		return 0;
	}
}
