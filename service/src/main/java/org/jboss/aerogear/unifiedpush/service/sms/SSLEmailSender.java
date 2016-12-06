package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Properties;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends Email over SMTP API.
 */
public class SSLEmailSender extends AbstractEmailSender implements VerificationPublisher {
	private final Logger logger = LoggerFactory.getLogger(SSLEmailSender.class);

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
				logger.warn("SSLEmail Configuraiton peoperties are missing, unable to send Email request");
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
				email.setMsg(getVerificationMessage(code));
				email.addTo(alias);

				email.send();
			} catch (EmailException e) {
				VerificationPublisher.logError(logger, "Email",hostname, portnumb, username, password, fromaddr, alias, subjectt, e);
			}
		} catch (Exception e) {
			VerificationPublisher.logError(logger, "Email",hostname, portnumb, username, password, fromaddr, alias, subjectt, e);
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
