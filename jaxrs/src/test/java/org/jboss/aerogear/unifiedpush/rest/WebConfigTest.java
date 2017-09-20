package org.jboss.aerogear.unifiedpush.rest;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ListenerBootstrap;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.RequestContextListener;

import io.undertow.UndertowOptions;

@Configuration
@Import({ WebConfig.class })
public class WebConfigTest {
	@Bean
	public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
		UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();

		factory.setAccessLogDirectory(new File(System.getProperty("user.dir")));
		factory.setAccessLogEnabled(true);
		factory.setAccessLogPattern(
				"%I %q %m %h %a %l %u %t \"%r\" %s %b (%D ms) %U \"%{i,Referer}\" \"%{i,Host}\" \"%{i,User-Agent}\" \"%{o,Content-Type}\" \"%{o,Content-Length}\"");
		factory.setBufferSize(16000);
		factory.setDirectBuffers(true);
		factory.setIoThreads(10);
		factory.setWorkerThreads(100);

		factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true),
				builder -> builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, true),
				builder -> builder.setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true));

		return factory;
	}

	@Bean
	public ServletContextInitializer initializer() {
		return new ServletContextInitializer() {

			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {

				servletContext.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
			}
		};
	}

    @Bean(destroyMethod = "cleanup")
    public static RestEasySpringInitializer restEasySpringInitializer() {
        return new RestEasySpringInitializer();
    }

	@Bean
	public ServletListenerRegistrationBean<RequestContextListener> requestContextListener() {
		return new ServletListenerRegistrationBean<RequestContextListener>(new RequestContextListener());
	}

	@Bean
	public ServletRegistrationBean httpServletDispatcher() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new HttpServletDispatcher(), "/rest/*");
		bean.setName("Resteasy");
		bean.setLoadOnStartup(1);
		return bean;
	}

	/*
	 * Alternative method to @bean ServletListenerRegistrationBean<ResteasyBootstrap>
	 * This autoconfiguration will integrate spring and resteasy properly.
	 */
	public static class RestEasySpringInitializer
			implements ServletContextInitializer, ApplicationContextAware, BeanFactoryPostProcessor {

		private ResteasyDeployment deployment;

		private ConfigurableApplicationContext applicationContext;

		private ConfigurableListableBeanFactory beanFactory;

		public void cleanup() {
			deployment.stop();
		}

		@Override
		public void onStartup(ServletContext servletContext) throws ServletException {
			ListenerBootstrap config = new ListenerBootstrap(servletContext);
			deployment = config.createDeployment();
			deployment.start();

			servletContext.setAttribute(ResteasyProviderFactory.class.getName(), deployment.getProviderFactory());
			servletContext.setAttribute(Dispatcher.class.getName(), deployment.getDispatcher());
			servletContext.setAttribute(Registry.class.getName(), deployment.getRegistry());

			SpringBeanProcessor processor = new SpringBeanProcessor(deployment.getDispatcher(),
					deployment.getRegistry(), deployment.getProviderFactory());
			processor.postProcessBeanFactory(beanFactory);
			applicationContext.addApplicationListener(processor);
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = (ConfigurableApplicationContext) applicationContext;
		}
	}

}
