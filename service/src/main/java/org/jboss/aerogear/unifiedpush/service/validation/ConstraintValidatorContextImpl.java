package org.jboss.aerogear.unifiedpush.service.validation;

import java.util.Properties;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

import org.hibernate.validator.internal.engine.DefaultClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstraintValidatorContextImpl implements ConstraintValidatorContext {
	private final Logger logger = LoggerFactory.getLogger(ConstraintValidatorContextImpl.class);
	private final String pushApplicationId;
	private final Properties properties;
    
	public ConstraintValidatorContextImpl(String pushApplicationId, Properties properties) {
		this.pushApplicationId = pushApplicationId;
		this.properties = properties;
	}

	@Override
	public void disableDefaultConstraintViolation() {

	}

	@Override
	public String getDefaultConstraintMessageTemplate() {
		return null;
	}

	@Override
	public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		// allow unwrapping into public super types
		if (type.isAssignableFrom(ConstraintValidatorContextImpl.class)) {
			return type.cast(this);
		}
		logger.warn("Unable to cast " + type + ", to ConstraintValidatorContextImpl");
		throw new ValidationException("Unable to cast " + type + ", to ConstraintValidatorContextImpl");
	}

	public String getPushApplicationId() {
		return pushApplicationId;
	}

	public Properties getProperties() {
		return properties;
	}

	@Override
	public ClockProvider getClockProvider() {
		return DefaultClockProvider.INSTANCE;
	}
}
