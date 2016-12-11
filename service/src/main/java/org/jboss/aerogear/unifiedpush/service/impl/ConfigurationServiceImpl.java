package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.Interceptors;

import org.jboss.aerogear.unifiedpush.service.ConfigurationService;
import org.jboss.aerogear.unifiedpush.service.OAuth2ConfigurationBuilder;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EJB singleton bean which wraps spring environment bean.
 */
@Startup
@Singleton
@Interceptors(SpringContextInterceptor.class)
public class ConfigurationServiceImpl implements ConfigurationService {
	private static final String VERIFICATION_IMPL_KEY = "aerogear.config.verification.impl.class";

	@Autowired
	private ConfigurationEnvironment environment;

	public ConfigurationServiceImpl(){

	}

	public ConfigurationServiceImpl (ConfigurationEnvironment environment){
		this.environment = environment;
	}

	@PostConstruct
	public void initialized(){
		OAuth2ConfigurationBuilder.build(environment);
	}

	public String getDocumentsRootPath() {
		return environment.getDocumentsRootPath();
	}

	public Boolean isVerificationEnabled() {
		return environment.isVerificationEnabled();
	}

	public String getVerificationClassImpl() {
		return environment.getProperty(VERIFICATION_IMPL_KEY, ClickatellSMSSender.class.getName());
	}

	public String getMasterCode() {
		return environment.getMasterCode();
	}

	public Properties getProperties() {
		return environment.getProperties();
	}
}
