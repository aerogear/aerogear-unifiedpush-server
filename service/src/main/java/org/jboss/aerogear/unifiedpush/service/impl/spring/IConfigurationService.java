package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.Properties;

public interface IConfigurationService {
	Properties getProperties();

	Boolean isVerificationEnabled();

	String getVerificationClassImpl();

	String getMasterCode();

	Integer getQueryDefaultPeriodInDays();

	String getOAuth2Url();

	String getUpsRealm();
}
