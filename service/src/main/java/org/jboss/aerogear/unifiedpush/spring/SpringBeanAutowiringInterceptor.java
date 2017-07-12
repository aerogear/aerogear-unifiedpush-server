package org.jboss.aerogear.unifiedpush.spring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.InvocationContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;

public class SpringBeanAutowiringInterceptor {

	/**
	 * Autowire the target bean after construction as well as after passivation.
	 * @param invocationContext the EJB3 invocation context
	 */
	@PostConstruct
	@PostActivate
	public void autowireBean(InvocationContext invocationContext) {
		doAutowireBean(invocationContext.getTarget());
		try {
			invocationContext.proceed();
		}
		catch (RuntimeException ex) {
			/// doReleaseBean(invocationContext.getTarget());
			throw ex;
		}
		catch (Error err) {
			//doReleaseBean(invocationContext.getTarget());
			throw err;
		}
		catch (Exception ex) {
			//doReleaseBean(invocationContext.getTarget());
			// Cannot declare a checked exception on WebSphere here - so we need to wrap.
			throw new EJBException(ex);
		}
	}

	/**
	 * Actually autowire the target bean after construction/passivation.
	 * @param target the target bean to autowire
	 */
	protected void doAutowireBean(Object target) {
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		configureBeanPostProcessor(bpp, target);
		bpp.setBeanFactory(getBeanFactory());
		bpp.processInjection(target);
	}

	/**
	 * Template method for configuring the
	 * {@link AutowiredAnnotationBeanPostProcessor} used for autowiring.
	 * @param processor the AutowiredAnnotationBeanPostProcessor to configure
	 * @param target the target bean to autowire with this processor
	 */
	protected void configureBeanPostProcessor(AutowiredAnnotationBeanPostProcessor processor, Object target) {
	}

	/**
	 * Determine the BeanFactory for autowiring the given target bean.
	 * @return the BeanFactory to use (never {@code null})
	 * @see #getBeanFactoryReference
	 */
	protected BeanFactory getBeanFactory() {
		BeanFactory factory = getBeanFactoryReference();
		if (factory instanceof ApplicationContext) {
			factory = ((ApplicationContext) factory).getAutowireCapableBeanFactory();
		}
		return factory;
	}

	/**
	 * Determine the BeanFactoryReference for the given target bean.
	 * <p>The default implementation delegates to {@link #getBeanFactoryLocatorKey(Object)}
	 * and {@link #getBeanFactoryLocatorKey}.
	 * @return the BeanFactoryReference to use (never {@code null})
	 * @see #getBeanFactoryLocatorKey(Object)
	 * @see #getBeanFactoryLocatorKey(Object)
	 */
	protected BeanFactory getBeanFactoryReference() {
		return SpringContextBootstrappingInitializer.getApplicationContext();
	}

	/**
	 * Determine the BeanFactoryLocator key to use. This typically indicates
	 * the bean name of the ApplicationContext definition in
	 * <strong>classpath*:beanRefContext.xml</strong> resource files.
	 * <p>The default is {@code null}, indicating the single
	 * ApplicationContext defined in the locator. This must be overridden
	 * if more than one shared ApplicationContext definition is available.
	 * @param target the target bean to autowire
	 * @return the BeanFactoryLocator key to use (or {@code null} for
	 * referring to the single ApplicationContext defined in the locator)
	 */
	protected String getBeanFactoryLocatorKey(Object target) {
		return null;
	}


	/**
	 * Release the factory which has been used for autowiring the target bean.
	 * @param invocationContext the EJB3 invocation context
	 */
	@PreDestroy
	@PrePassivate
	public void releaseBean(InvocationContext invocationContext) {
		try {
			invocationContext.proceed();
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			// Cannot declare a checked exception on WebSphere here - so we need to wrap.
			throw new EJBException(ex);
		}
	}
}