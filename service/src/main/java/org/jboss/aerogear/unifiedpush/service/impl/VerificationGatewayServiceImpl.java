package org.jboss.aerogear.unifiedpush.service.impl;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintValidator;

import org.jboss.aerogear.unifiedpush.api.validation.AlwaysTrueValidator;
import org.jboss.aerogear.unifiedpush.api.verification.VerificationPublisher;
import org.jboss.aerogear.unifiedpush.service.ConfigurationService;
import org.jboss.aerogear.unifiedpush.service.VerificationGatewayService;
import org.jboss.aerogear.unifiedpush.service.sms.MockLogSender;
import org.jboss.aerogear.unifiedpush.service.validation.ConstraintValidatorContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link VerificationGatewayService}. Note that this
 * class does not implement the underlying SMS sending mechanism. Rather, it
 * uses an implementation of {@link VerificationPublisher} to do so.
 *
 * @see VerificationPublisher
 */
@Singleton
@DependsOn(value = { "ConfigurationServiceImpl" })
public class VerificationGatewayServiceImpl implements VerificationGatewayService {
	private final Logger logger = LoggerFactory.getLogger(VerificationGatewayServiceImpl.class);

	public final static String VERIFICATION_IMPL_KEY = "aerogear.config.verification.impl.class";
	private final static String IMPL_SPLITTER_TOKEN = ";";
	private final static String IMPL_CLASS_TOKEN = "::";

	@Inject
	private ConfigurationService configuration;

	private List<VerificationPart> chain;

	public VerificationGatewayServiceImpl() {
	}

	/**
	 * Used for testing and mock services.
	 *
	 * @param configuration
	 *            contractor method for unittests only.
	 */
	public VerificationGatewayServiceImpl(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	/**
	 * Initializes the SMS sender. We cache the sender since an implementation
	 * might set up its own (being, currently, outside of JBoss's resource
	 * management scope) resource upkeep.
	 */
	@PostConstruct
	public void initializeSender() {
		final String validationMap = configuration.getVerificationClassImpl();

		if (validationMap == null || validationMap.length() == 0) {
			logger.warn("Cannot find validation implementation class, using log based validation class!");

			chain = new LinkedList<>();
			chain.add(new VerificationPart(new AlwaysTrueValidator(), new MockLogSender()));
		}

		if (chain == null) {
			synchronized (this) {
				if (chain == null)
					chain = buildMap(validationMap);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<VerificationPart> buildMap(String validationMap) {
		String[] impls = validationMap.split(IMPL_SPLITTER_TOKEN);
		String[] validatorToPublisher;
		boolean isValidationProvided;

		chain = new LinkedList<>();
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
				throw new RuntimeException("Cannot instantiate class " + validatorToPublisher[0], e);
			}

			chain.add(new VerificationPart(validator, publisher));
		}

		return chain;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void sendVerificationMessage(String pushApplicationId, String alias, String code) {
		VerificationPublisher publisher;

		if (chain == null) {
			// Retry initialization
			initializeSender();
		}

		for (VerificationPart part : chain) {
			@SuppressWarnings("rawtypes")
			ConstraintValidator validator = part.getValidator();
			publisher = part.getPublisher();

			if (validator.isValid(alias,
					new ConstraintValidatorContextImpl(pushApplicationId, configuration.getProperties()))) {
				logger.info(String.format("Sending '%s' message to alias '%s' using '%s' publisher", code, alias,
						publisher.getClass().getName()));
				publisher.send(alias, code, configuration.getProperties());

				if (!publisher.chain()) {
					break;
				}
			}
		}
	}

	public List<VerificationPart> getChain() {
		return chain;
	}

	public class VerificationPart {
		private final ConstraintValidator<? extends Annotation, ?> validator;
		private final VerificationPublisher publisher;

		public VerificationPart(ConstraintValidator<? extends Annotation, ?> validator,
				VerificationPublisher publisher) {
			super();
			this.validator = validator;
			this.publisher = publisher;
		}

		public ConstraintValidator<? extends Annotation, ?> getValidator() {
			return validator;
		}

		public VerificationPublisher getPublisher() {
			return publisher;
		}

	}
}
