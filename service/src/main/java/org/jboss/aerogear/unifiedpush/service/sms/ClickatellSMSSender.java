package org.jboss.aerogear.unifiedpush.service.sms;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.aerogear.unifiedpush.service.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Sends SMS over Clickatell's HTTP API.
 */
public class ClickatellSMSSender extends AbstractSender implements VerificationPublisher {
	private final Logger logger = LoggerFactory.getLogger(ClickatellSMSSender.class);

	private static final int PHONE_NUMBER_LENGTH = 10;
	private static final String COUNTRY_CODE_KEY_PREFIX = "aerogear.config.sms.sender.clickatell.countrycode";

	private final static String ERROR_PREFIX = "ERR";

	private final static String API_ID_KEY = "aerogear.config.sms.sender.clickatell.api_id";
	private final static String USERNAME_KEY = "aerogear.config.sms.sender.clickatell.username";
	private final static String PASSWORD_KEY = "aerogear.config.sms.sender.clickatell.password";
	private final static String ENCODING_KEY = "aerogear.config.sms.sender.clickatell.encoding";

	public final static String MESSAGE_TMPL = "aerogear.config.sms.sender.clickatell.template";

	private final static String API_URL = "https://api.clickatell.com/http/sendmsg";

	/**
	 * Sends off an sms message to the number. It is assumed the message argument
	 * string is the concatenated country code and mobile number, where the mobile
	 * number is 10 digits long, with '0' left padding if required. The country code
	 * need not have extra left padding.
	 * 
	 * @param alias      the phone number to send to.
	 * @param code       text to send.
	 * @param properties list of implementation properties.
	 */
	@Override
	public void send(String alias, String code, MessageType type, Properties properties, MessageSource messageSource,
			String locale) {
		final String apiId = getProperty(properties, API_ID_KEY);
		final String username = getProperty(properties, USERNAME_KEY);
		final String password = getProperty(properties, PASSWORD_KEY);
		final String encoding = getProperty(properties, ENCODING_KEY);

		try {
			if (apiId == null || username == null || password == null || encoding == null) {
				logger.warn("Configuraiton peoperties are missing, unable to send SMS request");
				return;
			}

			PhoneNumber parsedNumber = parseNumber(alias);
			String fromNumber = getFromNumber(parsedNumber, properties);
			String formattedNumber = formatNumber(parsedNumber);

			StringBuilder apiCall = new StringBuilder(API_URL).append("?user=").append(username).append("&password=")
					.append(password).append("&api_id=").append(apiId).append("&to=")
					.append(URLEncoder.encode(formattedNumber, encoding)).append("&text=")
					.append(URLEncoder.encode(getMessage(messageSource, MESSAGE_TMPL, locale, code), encoding));

			if (fromNumber != null) {
				apiCall.append("&from=").append(fromNumber);
			}

			// TODO: take this out to the propers file as well...
			if (parsedNumber.getCountryCode().equals("1")) {
				apiCall.append("&mo=1");
			}

			invokeAPI(apiCall.toString());
		} catch (Exception e) {
			VerificationPublisher.logError(logger, "SMS", API_URL, "443", username, password, username, alias, code, e);
		}
	}

	/**
	 * Returns a phone number string formatted to clickatell's API specification: A
	 * mobile number with no leading 0's.
	 */
	private String formatNumber(PhoneNumber parsedNumber) {
		String mobileNumber = parsedNumber.getNumber();
		while (mobileNumber.startsWith("0")) {
			mobileNumber = mobileNumber.substring(1);
		}
		return parsedNumber.getCountryCode() + mobileNumber;
	}

	private String getFromNumber(PhoneNumber number, Properties properties) {
		// Look up the "from" number to use when sending the sms to the argument number.
		// Each country code can have its own from number. They are mapped in the
		// properties file
		// using the format prefix<country-code>=<from-number>
		return properties.getProperty(COUNTRY_CODE_KEY_PREFIX + number.getCountryCode());
	}

	private void invokeAPI(String apiCall) throws IOException {
		// TODO: use a connection pool.
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet get = new HttpGet(apiCall);
			try (CloseableHttpResponse response = client.execute(get)) {
				HttpEntity entity = response.getEntity();
				int status = response.getStatusLine().getStatusCode();
				String responseText = EntityUtils.toString(entity);
				if (status != org.apache.http.HttpStatus.SC_OK || isError(responseText)) {
					logger.warn("Using clickatell api format: " + apiCall);
					throw new RuntimeException(
							"Received status code " + status + " from clickatell, with response " + responseText);
				}

				logger.debug("Received status code {} from clickatell, with response: {}", status, responseText);
			}
		}
	}

	private PhoneNumber parseNumber(String normalizedNumber) {
		int numberIndex = normalizedNumber.length() - PHONE_NUMBER_LENGTH;
		// left pad the country code
		final String countryCode = normalizedNumber.substring(0, numberIndex);
		final String number = normalizedNumber.substring(numberIndex);
		return new PhoneNumber(countryCode, number);
	}

	/**
	 * A failed request to Clickatell still returns HTTP 200, so we need to check
	 * the response itself to tell if something went wrong on Clickatell's end.
	 * Failed requests start with "ERR".
	 */
	private boolean isError(String response) {
		return response.startsWith(ERROR_PREFIX);
	}

	private class PhoneNumber {
		private final String number;
		private final String countryCode;

		public PhoneNumber(String countryCode, String number) {
			this.number = number;
			this.countryCode = countryCode;
		}

		public String getNumber() {
			return number;
		}

		public String getCountryCode() {
			return countryCode;
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
