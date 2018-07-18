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
import java.math.BigInteger;

import cros.mail.chain.core.Coin;


public class InterchangeRate implements Serializable {

	public final Coin coin;
	public final FiatMoney fiatMoney;

	public InterchangeRate(Coin coin, FiatMoney fiatMoney) {
		checkArgument(coin.isPositive());
		checkArgument(fiatMoney.isPositive());
		checkArgument(fiatMoney.currencyCode != null, "currency code required");
		this.coin = coin;
		this.fiatMoney = fiatMoney;
	}

	public InterchangeRate(FiatMoney fiatMoney) {
		this(Coin.COIN, fiatMoney);
	}

	public FiatMoney coinToFiat(Coin convertCoin) {

		final BigInteger converted = BigInteger.valueOf(convertCoin.value).multiply(BigInteger.valueOf(fiatMoney.value))
				.divide(BigInteger.valueOf(coin.value));
		if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
				|| converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
			throw new ArithmeticException("Overflow");
		return FiatMoney.valueOf(fiatMoney.currencyCode, converted.longValue());
	}

	public Coin fiatToCoin(FiatMoney convertFiat) {
		checkArgument(convertFiat.currencyCode.equals(fiatMoney.currencyCode), "Currency mismatch: %s vs %s",
				convertFiat.currencyCode, fiatMoney.currencyCode);

		final BigInteger converted = BigInteger.valueOf(convertFiat.value).multiply(BigInteger.valueOf(coin.value))
				.divide(BigInteger.valueOf(fiatMoney.value));
		if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
				|| converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
			throw new ArithmeticException("Overflow");
		try {
			return Coin.valueOf(converted.longValue());
		} catch (IllegalArgumentException x) {
			throw new ArithmeticException("Overflow: " + x.getMessage());
		}
	}
}
