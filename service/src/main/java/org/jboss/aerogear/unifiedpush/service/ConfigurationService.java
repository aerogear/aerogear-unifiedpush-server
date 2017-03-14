package org.jboss.aerogear.unifiedpush.service;

import java.util.Properties;

public interface ConfigurationService {
	Properties getProperties();

	Boolean isVerificationEnabled();

	String getVerificationClassImpl();

	String getMasterCode();

	Integer getQueryDefaultPeriodInDays();
}
