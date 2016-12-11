package org.jboss.aerogear.unifiedpush.spring;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 * Singleton EJB to hold shared application context.
 */
@Startup
@Singleton
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SpringContextBean {

	@Autowired
	private ApplicationContext applicationContext;

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
