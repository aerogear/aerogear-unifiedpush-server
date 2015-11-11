package org.jboss.aerogear.unifiedpush.service.sms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.aerogear.unifiedpush.api.sms.SMSSender;

/**
 * Sends SMS over Clickatell's HTTP API.
 */
public class ClickatellSMSSender implements SMSSender {
	private Logger logger = Logger.getLogger(ClickatellSMSSender.class.getName());

	private final static String DEFAULT_VERIFICATION_TEMPLATE = "{0}";
	private final static String ERROR_PREFIX = "ERR";
	
	private final static String API_ID_KEY = "aerogear.config.sms.sender.clickatell.api_id";
	private final static String USERNAME_KEY = "aerogear.config.sms.sender.clickatell.username";
	private final static String PASSWORD_KEY = "aerogear.config.sms.sender.clickatell.password";
	private final static String ENCODING_KEY = "aerogear.config.sms.sender.clickatell.encoding";
	private final static String MESSAGE_TMPL = "aerogear.config.sms.sender.clickatell.template";

	private final static String API_URL = "https://api.clickatell.com/http/sendmsg";

	private String template;
    
	@Override
	public void send(String phoneNumber, String message, Properties properties) {
		final String apiId = getProperty(properties, API_ID_KEY);
		final String username = getProperty(properties, USERNAME_KEY);
		final String password = getProperty(properties, PASSWORD_KEY);
		final String encoding = getProperty(properties, ENCODING_KEY);
		template = getProperty(properties, MESSAGE_TMPL);
		
		try {
			if (apiId==null || username==null || password==null || encoding ==null){
				logger.log(Level.WARNING, "Configuraiton peoperties are missing, unable to send SMS request");
				return;
			}
				
			StringBuilder apiCall = new StringBuilder(API_URL)
			.append("?user=").append(username)
			.append("&password=").append(password)
			.append("&api_id=").append(apiId)
			.append("&to=").append(URLEncoder.encode(phoneNumber, encoding))
			.append("&text=").append(URLEncoder.encode(getVerificationMessage(message), encoding));
			
			invokeAPI(apiCall.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("failed to encode api call", e);
		} catch (IOException e) {
			throw new RuntimeException("api call failed", e);
		}
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
					throw new RuntimeException("Received status code " + status + " from clickatell, with response " + 
							responseText);
				}
			}
		}
	}
	
	/**
	 * A failed request to Clickatell still returns HTTP 200, so we need to check
	 * the response itself to tell if something went wrong on Clickatell's end. Failed requests start with "ERR".
	 */
	private boolean isError(String response) {
		return response.startsWith(ERROR_PREFIX);
	}

	private String getProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			logger.log(Level.WARNING, "cannot find property " + key + " in configuration");
		}
		return value;
	}
    
	private String getVerificationMessage(String verificationCode) {
		return tlMessageFormat.get().format(new Object[] { verificationCode });
	}
	
    private ThreadLocal<MessageFormat> tlMessageFormat = new ThreadLocal<MessageFormat>() {
    	@Override
    	public MessageFormat initialValue() {
    		if (template == null || template.isEmpty()) {
    			template = DEFAULT_VERIFICATION_TEMPLATE;
    		}
			return new MessageFormat(template);
    	}
    };

}
