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
package cros.mail.chain.uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cros.mail.chain.core.Address;
import cros.mail.chain.core.NoAddressException;
import cros.mail.chain.core.Coin;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.param.AbstractCrosNetParam;

import javax.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class CrosURI {

	private static final Logger log = LoggerFactory.getLogger(CrosURI.class);

	public static final String FIELD_MESSAGE = "message";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_AMOUNT = "amount";
	public static final String FIELD_ADDRESS = "address";
	public static final String FIELD_PAYMENT_REQUEST_URL = "r";

	@Deprecated
	public static final String BITCOIN_SCHEME = "bitcoin";
	private static final String ENCODED_SPACE_CHARACTER = "%20";
	private static final String AMPERSAND_SEPARATOR = "&";
	private static final String QUESTION_MARK_SEPARATOR = "?";

	private final Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();

	public CrosURI(String uri) throws CrosURIParseException {
		this(null, uri);
	}

	public CrosURI(@Nullable NetworkParams params, String input) throws CrosURIParseException {
		checkNotNull(input);
		log.debug("Attempting to parse '{}' for {}", input, params == null ? "any" : params.getId());

		String scheme = null == params ? AbstractCrosNetParam.BITCOIN_SCHEME : params.getUriScheme();

		URI uri;
		try {
			uri = new URI(input);
		} catch (URISyntaxException e) {
			throw new CrosURIParseException("Bad URI syntax", e);
		}

		String blockchainInfoScheme = scheme + "://";
		String correctScheme = scheme + ":";
		String schemeSpecificPart;
		if (input.startsWith(blockchainInfoScheme)) {
			schemeSpecificPart = input.substring(blockchainInfoScheme.length());
		} else if (input.startsWith(correctScheme)) {
			schemeSpecificPart = input.substring(correctScheme.length());
		} else {
			throw new CrosURIParseException("Unsupported URI scheme: " + uri.getScheme());
		}

		String[] addressSplitTokens = schemeSpecificPart.split("\\?", 2);
		if (addressSplitTokens.length == 0)
			throw new CrosURIParseException("No data found after the bitcoin: prefix");
		String addressToken = addressSplitTokens[0];

		String[] nameValuePairTokens;
		if (addressSplitTokens.length == 1) {

			nameValuePairTokens = new String[] {};
		} else {

			nameValuePairTokens = addressSplitTokens[1].split("&");
		}

		parseParameters(params, addressToken, nameValuePairTokens);

		if (!addressToken.isEmpty()) {

			try {
				Address address = new Address(params, addressToken);
				putWithValidation(FIELD_ADDRESS, address);
			} catch (final NoAddressException e) {
				throw new CrosURIParseException("Bad address", e);
			}
		}

		if (addressToken.isEmpty() && getPaymentRequestUrl() == null) {
			throw new CrosURIParseException("No address and no r= parameter found");
		}
	}

	private void parseParameters(@Nullable NetworkParams params, String addressToken, String[] nameValuePairTokens)
			throws CrosURIParseException {

		for (String nameValuePairToken : nameValuePairTokens) {
			final int sepIndex = nameValuePairToken.indexOf('=');
			if (sepIndex == -1)
				throw new CrosURIParseException("Malformed Bitcoin URI - no separator in '" + nameValuePairToken + "'");
			if (sepIndex == 0)
				throw new CrosURIParseException("Malformed Bitcoin URI - empty name '" + nameValuePairToken + "'");
			final String nameToken = nameValuePairToken.substring(0, sepIndex).toLowerCase(Locale.ENGLISH);
			final String valueToken = nameValuePairToken.substring(sepIndex + 1);

			if (FIELD_AMOUNT.equals(nameToken)) {

				try {
					Coin amount = Coin.parseCoin(valueToken);
					if (params != null && amount.isGreaterThan(params.getMaxMoney()))
						throw new CrosURIParseException("Max number of coins exceeded");
					if (amount.signum() < 0)
						throw new ArithmeticException("Negative coins specified");
					putWithValidation(FIELD_AMOUNT, amount);
				} catch (IllegalArgumentException e) {
					throw new OptionalInputValidationException(
							String.format(Locale.US, "'%s' is not a valid amount", valueToken), e);
				} catch (ArithmeticException e) {
					throw new OptionalInputValidationException(
							String.format(Locale.US, "'%s' has too many decimal places", valueToken), e);
				}
			} else {
				if (nameToken.startsWith("req-")) {

					throw new RequiredInputValidationException(
							"'" + nameToken + "' is required but not known, this URI is not valid");
				} else {

					try {
						if (valueToken.length() > 0)
							putWithValidation(nameToken, URLDecoder.decode(valueToken, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

	}

	private void putWithValidation(String key, Object value) throws CrosURIParseException {
		if (parameterMap.containsKey(key)) {
			throw new CrosURIParseException(String.format(Locale.US, "'%s' is duplicated, URI is invalid", key));
		} else {
			parameterMap.put(key, value);
		}
	}

	@Nullable
	public Address getAddress() {
		return (Address) parameterMap.get(FIELD_ADDRESS);
	}

	public Coin getAmount() {
		return (Coin) parameterMap.get(FIELD_AMOUNT);
	}

	public String getLabel() {
		return (String) parameterMap.get(FIELD_LABEL);
	}

	public String getMessage() {
		return (String) parameterMap.get(FIELD_MESSAGE);
	}

	public String getPaymentRequestUrl() {
		return (String) parameterMap.get(FIELD_PAYMENT_REQUEST_URL);
	}

	public List<String> getPaymentRequestUrls() {
		ArrayList<String> urls = new ArrayList<String>();
		while (true) {
			int i = urls.size();
			String paramName = FIELD_PAYMENT_REQUEST_URL + (i > 0 ? Integer.toString(i) : "");
			String url = (String) parameterMap.get(paramName);
			if (url == null)
				break;
			urls.add(url);
		}
		Collections.reverse(urls);
		return urls;
	}

	public Object getParameterByName(String name) {
		return parameterMap.get(name);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("CrosURI[");
		boolean first = true;
		for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
			if (first) {
				first = false;
			} else {
				builder.append(",");
			}
			builder.append("'").append(entry.getKey()).append("'=").append("'").append(entry.getValue()).append("'");
		}
		builder.append("]");
		return builder.toString();
	}

	public static String convertToBitcoinURI(Address address, Coin amount, String label, String message) {
		return convertToBitcoinURI(address.toString(), amount, label, message);
	}

	public static String convertToBitcoinURI(String address, @Nullable Coin amount, @Nullable String label,
			@Nullable String message) {
		checkNotNull(address);
		if (amount != null && amount.signum() < 0) {
			throw new IllegalArgumentException("Coin must be positive");
		}

		StringBuilder builder = new StringBuilder();
		builder.append(BITCOIN_SCHEME).append(":").append(address);

		boolean questionMarkHasBeenOutput = false;

		if (amount != null) {
			builder.append(QUESTION_MARK_SEPARATOR).append(FIELD_AMOUNT).append("=");
			builder.append(amount.toPlainString());
			questionMarkHasBeenOutput = true;
		}

		if (label != null && !"".equals(label)) {
			if (questionMarkHasBeenOutput) {
				builder.append(AMPERSAND_SEPARATOR);
			} else {
				builder.append(QUESTION_MARK_SEPARATOR);
				questionMarkHasBeenOutput = true;
			}
			builder.append(FIELD_LABEL).append("=").append(encodeURLString(label));
		}

		if (message != null && !"".equals(message)) {
			if (questionMarkHasBeenOutput) {
				builder.append(AMPERSAND_SEPARATOR);
			} else {
				builder.append(QUESTION_MARK_SEPARATOR);
			}
			builder.append(FIELD_MESSAGE).append("=").append(encodeURLString(message));
		}

		return builder.toString();
	}

	static String encodeURLString(String stringToEncode) {
		try {
			return java.net.URLEncoder.encode(stringToEncode, "UTF-8").replace("+", ENCODED_SPACE_CHARACTER);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
