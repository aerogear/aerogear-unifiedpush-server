package org.jboss.aerogear.unifiedpush.service.wrap;

import java.util.Properties;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.jboss.aerogear.unifiedpush.service.ConfigurationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EJB wrapper bean. Wraps spring configuration environments and
 * beans.
 */
@Stateless
@Wrapper
@Interceptors(SpringContextInterceptor.class)
public class ConfigurationServiceWrapper implements ConfigurationService {

	@Autowired
	private IConfigurationService configurationService;

	public Boolean isVerificationEnabled() {
		return configurationService.isVerificationEnabled();
	}

	public String getVerificationClassImpl() {
		return configurationService.getVerificationClassImpl();
	}

	public String getMasterCode() {
		return configurationService.getMasterCode();
	}

	/*
	 * Number of days period to query existing documents.
	 */
	public Integer getQueryDefaultPeriodInDays() {
		return configurationService.getQueryDefaultPeriodInDays();
	}

	public Properties getProperties() {
		return configurationService.getProperties();
	}

	public String getOAuth2Url() {
		return configurationService.getOAuth2Url();
	}

	public String getUpsRealm() {
		return configurationService.getUpsRealm();
	}
}
