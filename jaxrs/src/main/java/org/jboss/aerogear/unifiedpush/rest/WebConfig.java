package org.jboss.aerogear.unifiedpush.rest;

import javax.validation.Validator;

import org.jboss.aerogear.unifiedpush.message.SenderConfig;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.BootstrapEndpoint;
import org.jboss.aerogear.unifiedpush.spring.ServiceConfig;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Import({ ServiceConfig.class, SenderConfig.class })
@ComponentScan(basePackageClasses = { WebConfig.class, BootstrapEndpoint.class })
public class WebConfig {

	private @Value(ConfigurationEnvironment.CONF_DIR_EL) String confDir;

	@Bean
	public Validator localValidatorFactoryBean() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();

		source.setDefaultEncoding("UTF-8");
		// External file base override class bath base
		// Limitation - When overriding default local ('basepath' | basepath_en)
		// all locals must be provided, else external 'basepath' locale
		// will override internal locales.

		source.setBasenames("WEB-INF/i18n/messages" , "classpath:i18n/messages");
		source.setAlwaysUseMessageFormat(true);

		return source;
	}
}
