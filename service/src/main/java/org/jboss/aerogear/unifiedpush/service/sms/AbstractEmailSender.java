package org.jboss.aerogear.unifiedpush.service.sms;

import org.jboss.aerogear.unifiedpush.service.VerificationPublisher.MessageType;

public abstract class AbstractEmailSender extends AbstractSender {
	protected final static String HOSTNAME_KEY = "aerogear.config.email.sender.hostname";
	protected final static String PORTNUMB_KEY = "aerogear.config.email.sender.portnumber";
	protected final static String USERNAME_KEY = "aerogear.config.email.sender.username";
	protected final static String PASSWORD_KEY = "aerogear.config.email.sender.password";
	protected final static String FROMADDR_KEY = "aerogear.config.email.sender.fromaddress";
	protected final static String PORT25_USE_TLS = "aerogear.config.email.sender.tlsenabled";

	public final static String SUBJECT_KEY = "aerogear.config.email.sender.subject";
	public final static String SUBJECT_RESET_KEY = "aerogear.config.email.sender.subject.reset";
	public final static String MESSAGE_TMPL = "aerogear.config.email.sender.template";
	public final static String MESSAGE_RESET_TMPL = "aerogear.config.email.sender.template.reset";

	protected String getEmailMessageKey(MessageType type) {
		if (type == MessageType.RESET) {
			return MESSAGE_RESET_TMPL;
		}

		return MESSAGE_TMPL;
	}

	protected String getSubjectMessageKey(MessageType type) {
		if (type == MessageType.RESET) {
			return SUBJECT_RESET_KEY;
		}

		return SUBJECT_KEY;
	}
}
