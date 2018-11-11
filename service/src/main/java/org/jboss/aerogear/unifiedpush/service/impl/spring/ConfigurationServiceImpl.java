package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.Properties;

import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * EJB singleton wrapper bean. Wraps spring configuration environments and beans.
 */
@Service
public class ConfigurationServiceImpl implements IConfigurationService {
	private static final String VERIFICATION_IMPL_KEY = "aerogear.config.verification.impl.class";

	@Autowired
	private ConfigurationEnvironment environment;
	@Autowired
	private IOAuth2Configuration oAuth2Configuration;


	public String getVerificationClassImpl() {
		return environment.getProperty(VERIFICATION_IMPL_KEY, ClickatellSMSSender.class.getName());
	}

	public String getMasterCode() {
		return environment.getMasterCode();
	}

	/*
	 * Number of days period to query existing documents.
	 */
	public Integer getQueryDefaultPeriodInDays() {
		return environment.getQueryDefaultPeriodInDays();
	}

	public Properties getProperties() {
		return environment.getProperties();
	}

	public String getOAuth2Url() {
		return oAuth2Configuration.getOAuth2Url();
	}

	public String getUpsRealm() {
		return oAuth2Configuration.getUpsRealm();
	}
}
