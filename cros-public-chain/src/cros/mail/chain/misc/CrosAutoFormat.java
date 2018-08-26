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

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;

import static cros.mail.chain.core.Coin.SMALLEST_UNIT_EXPONENT;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.math.BigDecimal;
import static java.math.RoundingMode.HALF_UP;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import java.util.Locale;

public final class CrosAutoFormat extends CrosFormat {

	public enum Style {

		CODE {
			@Override
			void apply(DecimalFormat decimalFormat) {

				decimalFormat.applyPattern(negify(decimalFormat.toPattern()).replaceAll("¤", "¤¤")
						.replaceAll("([#0.,E-])¤¤", "$1 ¤¤").replaceAll("¤¤([0#.,E-])", "¤¤ $1"));
			}
		},

		SYMBOL {
			@Override
			void apply(DecimalFormat decimalFormat) {

				decimalFormat.applyPattern(negify(decimalFormat.toPattern()).replaceAll("¤¤", "¤"));
			}
		};

		abstract void apply(DecimalFormat decimalFormat);
	}

	protected CrosAutoFormat(Locale locale, Style style, int fractionPlaces) {
		super((DecimalFormat) NumberFormat.getCurrencyInstance(locale), fractionPlaces, ImmutableList.<Integer>of());
		style.apply(this.numberFormat);
	}

	@Override
	protected int scale(BigInteger satoshis, int fractionPlaces) {

		int places;
		int coinOffset = Math.max(SMALLEST_UNIT_EXPONENT - fractionPlaces, 0);
		BigDecimal inCoins = new BigDecimal(satoshis).movePointLeft(coinOffset);
		if (inCoins.remainder(ONE).compareTo(ZERO) == 0) {
			places = COIN_SCALE;
		} else {
			BigDecimal inMillis = inCoins.movePointRight(MILLICOIN_SCALE);
			if (inMillis.remainder(ONE).compareTo(ZERO) == 0) {
				places = MILLICOIN_SCALE;
			} else {
				BigDecimal inMicros = inCoins.movePointRight(MICROCOIN_SCALE);
				if (inMicros.remainder(ONE).compareTo(ZERO) == 0) {
					places = MICROCOIN_SCALE;
				} else {

					BigDecimal a = inCoins.subtract(inCoins.setScale(0, HALF_UP)).movePointRight(coinOffset).abs();
					BigDecimal b = inMillis.subtract(inMillis.setScale(0, HALF_UP))
							.movePointRight(coinOffset - MILLICOIN_SCALE).abs();
					BigDecimal c = inMicros.subtract(inMicros.setScale(0, HALF_UP))
							.movePointRight(coinOffset - MICROCOIN_SCALE).abs();
					if (a.compareTo(b) < 0)
						if (a.compareTo(c) < 0)
							places = COIN_SCALE;
						else
							places = MICROCOIN_SCALE;
					else if (b.compareTo(c) < 0)
						places = MILLICOIN_SCALE;
					else
						places = MICROCOIN_SCALE;
				}
			}
		}
		prefixUnitsIndicator(numberFormat, places);
		return places;
	}

	@Override
	protected int scale() {
		return COIN_SCALE;
	}

	public int fractionPlaces() {
		return minimumFractionDigits;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof CrosAutoFormat))
			return false;
		return super.equals((CrosAutoFormat) o);
	}

	@Override
	public String toString() {
		return "Auto-format " + pattern();
	}

}
