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
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.math.LongMath.checkedMultiply;
import static com.google.common.math.LongMath.checkedPow;
import static com.google.common.math.LongMath.divide;

import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cros.mail.chain.core.Coin;
import cros.mail.chain.core.Money;


public final class MoneyFormat {

	public static final MoneyFormat BTC = new MoneyFormat().shift(0).minDecimals(2).repeatOptionalDecimals(2, 3);

	public static final MoneyFormat MBTC = new MoneyFormat().shift(3).minDecimals(2).optionalDecimals(2);

	public static final MoneyFormat UBTC = new MoneyFormat().shift(6).minDecimals(0).optionalDecimals(2);

	public static final MoneyFormat FIAT = new MoneyFormat().shift(0).minDecimals(2).repeatOptionalDecimals(2, 1);

	public static final String CODE_BTC = "BTC";

	public static final String CODE_MBTC = "mBTC";

	public static final String CODE_UBTC = "µBTC";

	public static final int MAX_DECIMALS = 8;

	private final char negativeSign;
	private final char positiveSign;
	private final char zeroDigit;
	private final char decimalMark;
	private final int minDecimals;
	private final List<Integer> decimalGroups;
	private final int shift;
	private final RoundingMode roundingMode;
	private final String[] codes;
	private final char codeSeparator;
	private final boolean codePrefixed;

	private static final String DECIMALS_PADDING = "0000000000000000";

	public MoneyFormat negativeSign(char negativeSign) {
		checkArgument(!Character.isDigit(negativeSign));
		checkArgument(negativeSign > 0);
		if (negativeSign == this.negativeSign)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat positiveSign(char positiveSign) {
		checkArgument(!Character.isDigit(positiveSign));
		if (positiveSign == this.positiveSign)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat digits(char zeroDigit) {
		if (zeroDigit == this.zeroDigit)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat decimalMark(char decimalMark) {
		checkArgument(!Character.isDigit(decimalMark));
		checkArgument(decimalMark > 0);
		if (decimalMark == this.decimalMark)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat minDecimals(int minDecimals) {
		if (minDecimals == this.minDecimals)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat optionalDecimals(int... groups) {
		List<Integer> decimalGroups = new ArrayList<Integer>(groups.length);
		for (int group : groups)
			decimalGroups.add(group);
		return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups, shift,
				roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat repeatOptionalDecimals(int decimals, int repetitions) {
		checkArgument(repetitions >= 0);
		List<Integer> decimalGroups = new ArrayList<Integer>(repetitions);
		for (int i = 0; i < repetitions; i++)
			decimalGroups.add(decimals);
		return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups, shift,
				roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat shift(int shift) {
		if (shift == this.shift)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat roundingMode(RoundingMode roundingMode) {
		if (roundingMode == this.roundingMode)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat noCode() {
		if (codes == null)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, null, codeSeparator, codePrefixed);
	}

	public MoneyFormat code(int codeShift, String code) {
		checkArgument(codeShift >= 0);
		final String[] codes = null == this.codes ? new String[MAX_DECIMALS]
				: Arrays.copyOf(this.codes, this.codes.length);

		codes[codeShift] = code;
		return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups, shift,
				roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat codeSeparator(char codeSeparator) {
		checkArgument(!Character.isDigit(codeSeparator));
		checkArgument(codeSeparator > 0);
		if (codeSeparator == this.codeSeparator)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat prefixCode() {
		if (codePrefixed)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, true);
	}

	public MoneyFormat postfixCode() {
		if (!codePrefixed)
			return this;
		else
			return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups,
					shift, roundingMode, codes, codeSeparator, false);
	}

	public MoneyFormat withLocale(Locale locale) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
		char negativeSign = dfs.getMinusSign();
		char zeroDigit = dfs.getZeroDigit();
		char decimalMark = dfs.getMonetaryDecimalSeparator();
		return new MoneyFormat(negativeSign, positiveSign, zeroDigit, decimalMark, minDecimals, decimalGroups, shift,
				roundingMode, codes, codeSeparator, codePrefixed);
	}

	public MoneyFormat() {

		this.negativeSign = '-';
		this.positiveSign = 0;
		this.zeroDigit = '0';
		this.decimalMark = '.';
		this.minDecimals = 2;
		this.decimalGroups = null;
		this.shift = 0;
		this.roundingMode = RoundingMode.HALF_UP;
		this.codes = new String[MAX_DECIMALS];
		this.codes[0] = CODE_BTC;
		this.codes[3] = CODE_MBTC;
		this.codes[6] = CODE_UBTC;
		this.codeSeparator = ' ';
		this.codePrefixed = true;
	}

	private MoneyFormat(char negativeSign, char positiveSign, char zeroDigit, char decimalMark, int minDecimals,
			List<Integer> decimalGroups, int shift, RoundingMode roundingMode, String[] codes, char codeSeparator,
			boolean codePrefixed) {
		this.negativeSign = negativeSign;
		this.positiveSign = positiveSign;
		this.zeroDigit = zeroDigit;
		this.decimalMark = decimalMark;
		this.minDecimals = minDecimals;
		this.decimalGroups = decimalGroups;
		this.shift = shift;
		this.roundingMode = roundingMode;
		this.codes = codes;
		this.codeSeparator = codeSeparator;
		this.codePrefixed = codePrefixed;
	}

	public CharSequence format(Money money) {

		int maxDecimals = minDecimals;
		if (decimalGroups != null)
			for (int group : decimalGroups)
				maxDecimals += group;
		int smallestUnitExponent = money.smallestUnitExponent();
		checkState(maxDecimals <= smallestUnitExponent,
				"The maximum possible number of decimals (%s) cannot exceed %s.", maxDecimals, smallestUnitExponent);

		long satoshis = Math.abs(money.getValue());
		long precisionDivisor = checkedPow(10, smallestUnitExponent - shift - maxDecimals);
		satoshis = checkedMultiply(divide(satoshis, precisionDivisor, roundingMode), precisionDivisor);

		long shiftDivisor = checkedPow(10, smallestUnitExponent - shift);
		long numbers = satoshis / shiftDivisor;
		long decimals = satoshis % shiftDivisor;

		String decimalsStr = String.format(Locale.US, "%0" + (smallestUnitExponent - shift) + "d", decimals);
		StringBuilder str = new StringBuilder(decimalsStr);
		while (str.length() > minDecimals && str.charAt(str.length() - 1) == '0')
			str.setLength(str.length() - 1);
		int i = minDecimals;
		if (decimalGroups != null) {
			for (int group : decimalGroups) {
				if (str.length() > i && str.length() < i + group) {
					while (str.length() < i + group)
						str.append('0');
					break;
				}
				i += group;
			}
		}
		if (str.length() > 0)
			str.insert(0, decimalMark);
		str.insert(0, numbers);
		if (money.getValue() < 0)
			str.insert(0, negativeSign);
		else if (positiveSign != 0)
			str.insert(0, positiveSign);
		if (codes != null) {
			if (codePrefixed) {
				str.insert(0, codeSeparator);
				str.insert(0, code());
			} else {
				str.append(codeSeparator);
				str.append(code());
			}
		}

		if (zeroDigit != '0') {
			int offset = zeroDigit - '0';
			for (int d = 0; d < str.length(); d++) {
				char c = str.charAt(d);
				if (Character.isDigit(c))
					str.setCharAt(d, (char) (c + offset));
			}
		}
		return str;
	}

	public Coin parse(String str) throws NumberFormatException {
		return Coin.valueOf(parseValue(str, Coin.SMALLEST_UNIT_EXPONENT));
	}

	public FiatMoney parseFiat(String currencyCode, String str) throws NumberFormatException {
		return FiatMoney.valueOf(currencyCode, parseValue(str, FiatMoney.SMALLEST_UNIT_EXPONENT));
	}

	private long parseValue(String str, int smallestUnitExponent) {
		checkState(DECIMALS_PADDING.length() >= smallestUnitExponent);
		if (str.isEmpty())
			throw new NumberFormatException("empty string");
		char first = str.charAt(0);
		if (first == negativeSign || first == positiveSign)
			str = str.substring(1);
		String numbers;
		String decimals;
		int decimalMarkIndex = str.indexOf(decimalMark);
		if (decimalMarkIndex != -1) {
			numbers = str.substring(0, decimalMarkIndex);
			decimals = (str + DECIMALS_PADDING).substring(decimalMarkIndex + 1);
			if (decimals.indexOf(decimalMark) != -1)
				throw new NumberFormatException("more than one decimal mark");
		} else {
			numbers = str;
			decimals = DECIMALS_PADDING;
		}
		String satoshis = numbers + decimals.substring(0, smallestUnitExponent - shift);
		for (char c : satoshis.toCharArray())
			if (!Character.isDigit(c))
				throw new NumberFormatException("illegal character: " + c);
		long value = Long.parseLong(satoshis);
		if (first == negativeSign)
			value = -value;
		return value;
	}

	public String code() {
		if (codes == null)
			return null;
		if (codes[shift] == null)
			throw new NumberFormatException("missing code for shift: " + shift);
		return codes[shift];
	}
}
