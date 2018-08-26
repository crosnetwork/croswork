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

import static cros.mail.chain.core.Coin.SMALLEST_UNIT_EXPONENT;
import static com.google.common.base.Preconditions.checkArgument;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public final class CrosFixedFormat extends CrosFormat {

	public static final int[] REPEATING_PLACES = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

	public static final int[] REPEATING_DOUBLETS = { 2, 2, 2, 2, 2, 2, 2 };

	public static final int[] REPEATING_TRIPLETS = { 3, 3, 3, 3, 3 };

	private final int scale;

	protected CrosFixedFormat(Locale locale, int scale, int minDecimals, List<Integer> groups) {
		super((DecimalFormat) NumberFormat.getInstance(locale), minDecimals, groups);
		checkArgument(scale <= SMALLEST_UNIT_EXPONENT,
				"decimal cannot be shifted " + String.valueOf(scale) + " places");
		this.scale = scale;
	}

	@Override
	protected int scale(BigInteger satoshis, int fractionPlaces) {
		prefixUnitsIndicator(numberFormat, scale);
		return scale;
	}

	@Override
	public int scale() {
		return scale;
	}

	public String code() {
		return prefixCode(coinCode(), scale);
	}

	public String symbol() {
		return prefixSymbol(coinSymbol(), scale);
	}

	public int[] fractionPlaceGroups() {
		Object[] boxedArray = decimalGroups.toArray();
		int len = boxedArray.length + 1;
		int[] array = new int[len];
		array[0] = minimumFractionDigits;
		for (int i = 1; i < len; i++) {
			array[i] = (Integer) boxedArray[i - 1];
		}
		return array;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof CrosFixedFormat))
			return false;
		CrosFixedFormat other = (CrosFixedFormat) o;
		return other.scale() == scale() && other.decimalGroups.equals(decimalGroups) && super.equals(other);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + scale;
		return result;
	}

	private static String prefixLabel(int scale) {
		switch (scale) {
		case COIN_SCALE:
			return "Coin-";
		case 1:
			return "Decicoin-";
		case 2:
			return "Centicoin-";
		case MILLICOIN_SCALE:
			return "Millicoin-";
		case MICROCOIN_SCALE:
			return "Microcoin-";
		case -1:
			return "Dekacoin-";
		case -2:
			return "Hectocoin-";
		case -3:
			return "Kilocoin-";
		case -6:
			return "Megacoin-";
		default:
			return "Fixed (" + String.valueOf(scale) + ") ";
		}
	}

	@Override
	public String toString() {
		return prefixLabel(scale) + "format " + pattern();
	}

}
