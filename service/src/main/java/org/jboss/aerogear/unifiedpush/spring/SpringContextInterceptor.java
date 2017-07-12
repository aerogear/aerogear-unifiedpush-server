package org.jboss.aerogear.unifiedpush.spring;

import javax.ejb.EJB;

import org.springframework.beans.factory.BeanFactory;

/**
 * Spring Context Interceptor which assume SpringContextBean (EJB) was
 * previously initialized with @Startup @Singleton
 * and @Interceptors(SpringBeanAutowiringInterceptor.class).
 */
public class SpringContextInterceptor extends SpringBeanAutowiringInterceptor {

	@EJB
	SpringContextBean springContextBean;

	protected BeanFactory getBeanFactory(Object target) {
		return springContextBean.getApplicationContext().getAutowireCapableBeanFactory();
	}
}
