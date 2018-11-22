package org.jboss.aerogear.unifiedpush.service.sms;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlFileSender extends AbstractSender implements VerificationPublisher {
	public static final String HTMLFILE_KEY = "aerogear.config.html.file.path";
	public static final String MESSAGE_TMPL = "aerogear.config.html.sender.template";

	private final Logger logger = LoggerFactory.getLogger(HtmlFileSender.class);

	@Override
	public void send(String alias, String code, MessageType type, Properties properties) {
		final String filepath = getProperty(properties, HTMLFILE_KEY);

		template = getProperty(properties, MESSAGE_TMPL);

		try {
			FileUtils.writeLines(new File(filepath), "UTF-8", Arrays.asList(getVerificationMessage(code, type, alias)),
					true);
		} catch (IOException e) {
			logger.error("Unable to append OTP to file: " + filepath, e);
		}
	}

	public boolean chain() {
		return false;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
