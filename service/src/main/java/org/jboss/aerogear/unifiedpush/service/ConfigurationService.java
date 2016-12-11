package org.jboss.aerogear.unifiedpush.service;

import java.util.Properties;

public interface ConfigurationService {
	String getDocumentsRootPath();

	Boolean isVerificationEnabled();

	String getVerificationClassImpl();

	Properties getProperties();

	String getMasterCode();
}
