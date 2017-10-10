package org.jboss.aerogear.unifiedpush.rest;

import javax.validation.Validator;

import org.jboss.aerogear.unifiedpush.message.SenderConfig;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.BootstrapEndpoint;
import org.jboss.aerogear.unifiedpush.spring.ServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Import({ ServiceConfig.class, SenderConfig.class })
@ComponentScan(basePackageClasses = { WebConfig.class, BootstrapEndpoint.class })
public class WebConfig {

	@Bean
	public Validator localValidatorFactoryBean() {
		return new LocalValidatorFactoryBean();
	}

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

}
