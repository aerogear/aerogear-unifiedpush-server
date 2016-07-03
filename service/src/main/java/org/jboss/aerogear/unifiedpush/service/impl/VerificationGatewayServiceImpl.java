package org.jboss.aerogear.unifiedpush.service.impl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintValidator;

import org.jboss.aerogear.unifiedpush.api.validation.AlwaysTrueValidator;
import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.jboss.aerogear.unifiedpush.service.Configuration;
import org.jboss.aerogear.unifiedpush.service.VerificationGatewayService;
import org.jboss.aerogear.unifiedpush.service.sms.MockLogSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link VerificationGatewayService}. Note that this class does not implement the underlying
 * SMS sending mechanism. Rather, it uses an implementation of {@link VerificationPublisher} to do so.
 *
 * @see VerificationPublisher
 */
@Singleton
public class VerificationGatewayServiceImpl implements VerificationGatewayService {
	private final Logger logger = LoggerFactory.getLogger(VerificationGatewayServiceImpl.class);

	public final static String VERIFICATION_IMPL_KEY = "aerogear.config.verification.impl.class";
	private final static String IMPL_SPLITTER_TOKEN = ";";
	private final static String IMPL_CLASS_TOKEN = "::";

	@Inject
	private Configuration configuration;

	private Map<ConstraintValidator<? extends Annotation, ?>, VerificationPublisher> publishers;

	/**
	 * Initializes the SMS sender. We cache the sender since an implementation might set up
	 * its own (being, currently, outside of JBoss's resource management scope) resource upkeep.
	 */
	@PostConstruct
	public void initializeSender() {
		final String validationMap = configuration.getProperty(VERIFICATION_IMPL_KEY);

		if (validationMap == null || validationMap.length() == 0) {
			logger.warn("Cannot find validation implementation class, using log based validation class!");

			publishers = new HashMap<>();
			publishers.put(new AlwaysTrueValidator(), new MockLogSender());
		}

		if (publishers == null) {
			synchronized (this) {
				if (publishers == null)
					publishers = buildMap(validationMap);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<ConstraintValidator<? extends Annotation, ?>, VerificationPublisher> buildMap(String validationMap) {
		String[] impls = validationMap.split(IMPL_SPLITTER_TOKEN);
		String[] validatorToPublisher;
		boolean isValidationProvided;

		publishers = new HashMap<>();
		VerificationPublisher publisher;
		ConstraintValidator<? extends Annotation, ?> validator;

		for (int i = 0; i < impls.length; i++) {
			validatorToPublisher = impls[i].split(IMPL_CLASS_TOKEN);
			isValidationProvided = false;

			if (validatorToPublisher.length == 2) {
				// Assuming implicit alwaysTrue validator is required
				isValidationProvided = true;
			}

			try {
				publisher = (VerificationPublisher) Class.forName(validatorToPublisher[isValidationProvided ? 1 : 0])
						.newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new RuntimeException(
						"Cannot instantiate class " + validatorToPublisher[isValidationProvided ? 1 : 0]);
			}

			try {
				if (isValidationProvided)
					validator = (ConstraintValidator<? extends Annotation, ?>) Class.forName(validatorToPublisher[0])
							.newInstance();
				else
					validator = new AlwaysTrueValidator();

			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new RuntimeException("Cannot instantiate class " + validatorToPublisher[0]);
			}

			publishers.put(validator, publisher);
		}

		return publishers;
	}


	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void sendVerificationMessage(String alias, String message) {
		if (publishers == null) {
			// Retry initialization
			initializeSender();
		}

		for (@SuppressWarnings("rawtypes")
		ConstraintValidator validator : publishers.keySet()) {
			if (validator.isValid(alias, null)) {
				logger.info(String.format("Sending '%s' message using '%s' publisher", message, publishers.get(validator).getClass().getName()));
				publishers.get(validator).send(alias, message, configuration.getProperties());
			}
		}
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Map<ConstraintValidator<? extends Annotation, ?>, VerificationPublisher> getPublishers() {
		return publishers;
	}
}
