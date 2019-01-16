package org.jboss.aerogear.unifiedpush.service.sms;

import java.util.Locale;
import java.util.Properties;

import org.jboss.aerogear.unifiedpush.service.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

/**
 * Sends Email over SendGrid API.
 */
public class SendGridEmailSender extends AbstractEmailSender implements VerificationPublisher {
	private final Logger logger = LoggerFactory.getLogger(SendGridEmailSender.class);

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

		try {
			if (hostname == null || portnumb == null || username == null || password == null || fromaddr == null
					|| alias == null) {
				logger.warn("SendGrid Configuraiton peoperties are missing, unable to send Email request");
				return;
			}

			try {
				Mail mail = new Mail();

				Email fromEmail = new Email();
				fromEmail.setName(username);
				fromEmail.setEmail(fromaddr);

				mail.setFrom(fromEmail);
				mail.setSubject(subject);
				mail.setReplyTo(fromEmail);

				Content content = new Content();
				content.setType("text/html");
				content.setValue(getMessage(messageSource, getEmailMessageKey(type), locale));
				mail.addContent(content);

				Personalization personalization = new Personalization();
				Email to = new Email();
				to.setEmail(alias);
				personalization.addTo(to);
				mail.addPersonalization(personalization);

				// Send the mail
				SendGrid sg = new SendGrid(password);
				Request request = new Request();
				request.method = Method.POST;
				request.endpoint = "mail/send";

				request.body = mail.build();
				Response response = sg.api(request);

				if (response.statusCode != 200 & response.statusCode != 202) {
					VerificationPublisher.logError(logger, "Email", hostname, portnumb, username, password, fromaddr,
							alias, subject, new Exception("Unable to send email!"));
					logger.error("Response body: " + response.body + ", Response headers: " + request.headers);
				}

			} catch (Exception e) {
				VerificationPublisher.logError(logger, "Email", hostname, portnumb, username, password, fromaddr, alias,
						subject, e);
			}
		} catch (Exception e) {
			VerificationPublisher.logError(logger, "Email", hostname, portnumb, username, password, fromaddr, alias,
					subject, e);
		}
	}

	public static final void main(String[] args) {
		Properties props = new Properties();
		props.setProperty(HOSTNAME_KEY, "");
		props.setProperty(PORTNUMB_KEY, "");
		props.setProperty(USERNAME_KEY, "XXX Support");
		props.setProperty(PASSWORD_KEY, "XXX");
		props.setProperty(FROMADDR_KEY, "no-reply@example.com");
		props.setProperty(SUBJECT_KEY, "Email verification code from XXX");
		props.setProperty(MESSAGE_TMPL,
				"Your verification code for the XXX mobile application is: <b>{0}</b>. Please use this code to verify your device.</br></br>Thank you for using CB4.</br>Sincerely,</br>The XXX Team");

		SendGridEmailSender sender = new SendGridEmailSender();
		sender.send("test@example.com", "123456", MessageType.REGISTER, props, null, Locale.ENGLISH.toString());
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
