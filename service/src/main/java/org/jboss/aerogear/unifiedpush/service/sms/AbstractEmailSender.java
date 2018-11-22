package org.jboss.aerogear.unifiedpush.service.sms;

public abstract class AbstractEmailSender extends AbstractSender {
	protected final static String HOSTNAME_KEY = "aerogear.config.email.sender.hostname";
	protected final static String PORTNUMB_KEY = "aerogear.config.email.sender.portnumber";
	protected final static String USERNAME_KEY = "aerogear.config.email.sender.username";
	protected final static String PASSWORD_KEY = "aerogear.config.email.sender.password";
	protected final static String FROMADDR_KEY = "aerogear.config.email.sender.fromaddress";
	protected final static String SUBJECT_KEY = "aerogear.config.email.sender.subject";
	protected final static String SUBJECT_RESET_KEY = "aerogear.config.email.sender.subject.reset";
	protected final static String PORT25_USE_TLS = "aerogear.config.email.sender.tlsenabled";

	protected final static String MESSAGE_TMPL = "aerogear.config.email.sender.template";
	protected final static String MESSAGE_RESET_TMPL = "aerogear.config.email.sender.template.reset";

}
