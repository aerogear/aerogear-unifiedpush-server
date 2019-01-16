package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Properties;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jboss.aerogear.unifiedpush.service.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Sends Email over SMTP API.
 */
public class SSLEmailSender extends AbstractEmailSender implements VerificationPublisher {
	private final Logger logger = LoggerFactory.getLogger(SSLEmailSender.class);

	/**
	 * Sends off an email message to the alias address.
	 */
	@Override
	public void send(String alias, String code, MessageType type, Properties properties, MessageSource messageSource,
			String locale) {
		final String hostname = getProperty(properties, HOSTNAME_KEY);
		final String portnumb = getProperty(properties, PORTNUMB_KEY);
		final String username = getProperty(properties, USERNAME_KEY);
		final String password = getProperty(properties, PASSWORD_KEY);
		final String fromaddr = getProperty(properties, FROMADDR_KEY);

		final String subject = getMessage(messageSource, getSubjectMessageKey(type), locale);

		final Boolean port25UseTls = Boolean.valueOf(getProperty(properties, PORT25_USE_TLS));

		try {
			if (hostname == null || portnumb == null || username == null || password == null || fromaddr == null
					|| alias == null) {
				logger.warn("SSLEmail Configuraiton peoperties are missing, unable to send Email request");
				return;
			}

			try {
				Email email = new HtmlEmail();
				email.setHostName(hostname);
				email.setSmtpPort(Integer.parseInt(portnumb));
				email.setAuthenticator(new DefaultAuthenticator(username, password));

				setPropertiesForPort(email, Integer.parseInt(portnumb), port25UseTls);

				email.setFrom(fromaddr);
				email.setSubject(subject);
				email.setMsg(getMessage(messageSource, getEmailMessageKey(type), locale));
				email.addTo(alias);

				email.send();
			} catch (EmailException e) {
				VerificationPublisher.logError(logger, "Email", hostname, portnumb, username, password, fromaddr, alias,
						subject, e);
			}
		} catch (Exception e) {
			VerificationPublisher.logError(logger, "Email", hostname, portnumb, username, password, fromaddr, alias,
					subject, e);
		}
	}

	/**
	 * Sets properties to the given HtmlEmail object based on the port from which it
	 * will be sent.
	 *
	 * @param email the email object to configure
	 * @param port  the configured outgoing port
	 */
	private void setPropertiesForPort(Email email, int port, boolean port25UseTls) throws EmailException {
		switch (port) {
		case 587:
			email.setStartTLSEnabled(true);
			email.setStartTLSRequired(true);
			break;
		case 25:
			if (port25UseTls) {
				email.setStartTLSEnabled(true);
				email.setSSLCheckServerIdentity(true);
			}
			break;
		case 465:
			email.setSslSmtpPort(Integer.toString(port));
			email.setSSLOnConnect(true);
			break;
		default:
			email.setStartTLSEnabled(true);
			email.setSSLOnConnect(true);
			email.setSSLCheckServerIdentity(true);
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
