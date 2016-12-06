package org.jboss.aerogear.unifiedpush.service.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ApplicationValidator implements ConstraintValidator<ApplicationValidation, String> {
	private static final String APP_VALIDATION_KEY = "aerogear.config.application.specific.validation.ids";
	private static final String APPLICATION_SEPERATOR = ";";

	@Override
	public void initialize(ApplicationValidation constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		ConstraintValidatorContextImpl contextImpl = context.unwrap(ConstraintValidatorContextImpl.class);
		String applicationIds = contextImpl.getProperties().getProperty(APP_VALIDATION_KEY);

		if (applicationIds == null | applicationIds.length() == 0)
			return false;

		String[] ids = applicationIds.split(APPLICATION_SEPERATOR);

		for (String id : ids) {
			if (id.equals(contextImpl.getPushApplicationId())) {
				return true;
			}
		}

		return false;
	}
}
