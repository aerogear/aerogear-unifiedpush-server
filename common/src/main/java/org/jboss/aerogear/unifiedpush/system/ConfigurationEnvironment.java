package org.jboss.aerogear.unifiedpush.system;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(name = "environment", value = { "classpath:default.properties",
		"file://${aerogear.config.dir}/environment.properties" }, ignoreResourceNotFound = true)
public class ConfigurationEnvironment {
	private final Logger logger = LoggerFactory.getLogger(ConfigurationEnvironment.class);

	public static final String PROPERTIES_DOCUMENTS_QUERY_DAYS = "aerogear.config.documents.query.period.days";
	public static final String PROP_ENABLE_VERIFICATION = "aerogear.config.verification.enable_verification";
	public static final String PROP_MASTER_VERIFICATION = "aerogear.config.verification.master_code";

	@Autowired
	private Environment env;

	private Properties properties;

	public Boolean isVerificationEnabled() {
		return env.getProperty(PROP_ENABLE_VERIFICATION, Boolean.class, Boolean.FALSE);
	}

	public String getMasterCode() {
		return env.getProperty(PROP_MASTER_VERIFICATION, String.class, null);
	}

	/*
	 * Number of days period to query existing documents.
	 */
	public Integer getQueryDefaultPeriodInDays() {
		return env.getProperty(PROPERTIES_DOCUMENTS_QUERY_DAYS, Integer.class, 31);
	}

	public String getProperty(String key, String defaultValue) {
		return env.getProperty(key, defaultValue);
	}

	public Boolean getProperty(String key, Boolean defaultValue) {
		return env.getProperty(key, Boolean.class, defaultValue);
	}

	public Integer getProperty(String key, Integer defaultValue) {
		return Integer.valueOf(env.getProperty(key, defaultValue.toString()));
	}

	/**
	 * Property placeholder configurer needed to process @Value annotations
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Cache and return all Environment properties
	 */
	public Properties getProperties() {
		if (properties == null) {
			synchronized (this) {
				if (properties == null)
					properties = new Properties();
			}
		}

		for (Iterator<org.springframework.core.env.PropertySource<?>> it = ((AbstractEnvironment) env)
				.getPropertySources().iterator(); it.hasNext();) {
			org.springframework.core.env.PropertySource<?> propertySource = (org.springframework.core.env.PropertySource<?>) it
					.next();
			if (propertySource instanceof EnumerablePropertySource) {
				Arrays.stream(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
						.forEach(prop -> properties.put(prop, propertySource.getProperty(prop)));
			} else {
				logger.warn("propertySource: " + propertySource.getName()
						+ " can't be processed (NOT derived from EnumerablePropertySource)!");
			}
		}

		return properties;
	}

}
