package org.jboss.aerogear.unifiedpush.service.sms;

import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

/**
 * Sends SMS over Clickatell's HTTP API.
 */
public class SSLEmailSender implements VerificationPublisher {
	private final static AeroGearLogger logger = AeroGearLogger.getInstance(SSLEmailSender.class);

	private final static String DEFAULT_VERIFICATION_TEMPLATE = "{0}";

	private final static String HOSTNAME_KEY = "aerogear.config.email.sender.hostname";
	private final static String PORTNUMB_KEY = "aerogear.config.email.sender.portnumber";
	private final static String USERNAME_KEY = "aerogear.config.email.sender.username";
	private final static String PASSWORD_KEY = "aerogear.config.email.sender.password";
	private final static String FROMADDR_KEY = "aerogear.config.email.sender.fromaddress";
	private final static String SUBJECTT_KEY = "aerogear.config.email.sender.subject";

	private final static String MESSAGE_TMPL = "aerogear.config.email.sender.template";

	private String template;

	/**
	 * Sends off an email message to the alias address.
	 */
	@Override
	public void send(String alias, String code, Properties properties) {
		final String hostname = getProperty(properties, HOSTNAME_KEY);
		final String portnumb = getProperty(properties, PORTNUMB_KEY);
		final String username = getProperty(properties, USERNAME_KEY);
		final String password = getProperty(properties, PASSWORD_KEY);
		final String fromaddr = getProperty(properties, FROMADDR_KEY);
		final String subjectt = getProperty(properties, SUBJECTT_KEY);

		template = getProperty(properties, MESSAGE_TMPL);

		try {
			if (hostname == null || portnumb == null || username == null || password == null || fromaddr == null
					|| alias == null) {
				logger.warning("Configuraiton peoperties are missing, unable to send Email request");
				return;
			}

			try {
				Email email = new HtmlEmail();
				email.setHostName(hostname);
				email.setSmtpPort(Integer.parseInt(portnumb));
				email.setAuthenticator(new DefaultAuthenticator(username, password));
				email.setStartTLSEnabled(true);
				email.setFrom(fromaddr);
				email.setSubject(subjectt);
				email.setMsg(getVerificationTemplate(code));
				email.addTo(alias);

				email.send();
			} catch (EmailException e) {
				logError(hostname, portnumb, username, password, fromaddr, alias, subjectt, e);
			}
		} catch (Exception e) {
			logError(hostname, portnumb, username, password, fromaddr, alias, subjectt, e);
		}
	}

	private void logError(String hostname, String portnumb, String username, String password, String fromaddr,
			String toaddres, String subjectt, Exception e) {

		StringBuilder builder = new StringBuilder();
		builder.append("Cannot send email message using");
		builder.append(": hostname: ").append(hostname);
		builder.append(", portnumb: ").append(portnumb);
		builder.append(", username: ").append(username);
		builder.append(", password: ").append(password);
		builder.append(", fromaddr: ").append(fromaddr);
		builder.append(", toaddres: ").append(toaddres);

		logger.severe(builder.toString() , e);
	}

	private String getProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			logger.warning("cannot find property " + key + " in configuration");
		}
		return value;
	}

	private String getVerificationTemplate(String verificationCode) {
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
