package org.jboss.aerogear.unifiedpush.spring;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.springframework.context.ApplicationContext;

/**
 * Singleton EJB to hold shared application context.
 */
@Startup
@Singleton
public class SpringContextBean {

	@PostConstruct
	public void intApplicationContext() {
		Properties contextProperties = new Properties();
		contextProperties.setProperty(SpringContextBootstrappingInitializer.CONTEXT_CONFIG_LOCATIONS_PARAMETER, "applicationContext.xml");
		new SpringContextBootstrappingInitializer().init(contextProperties);
	}

	public ApplicationContext getApplicationContext() {
		return SpringContextBootstrappingInitializer.getApplicationContext();
	}
}
