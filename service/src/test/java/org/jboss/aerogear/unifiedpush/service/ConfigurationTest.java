package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.spring.ServiceConfig;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ServiceConfig.class })
public class ConfigurationTest {

	@Autowired
	private ConfigurationEnvironment configuration;

	@Before
	public void cleanUp() {
		System.clearProperty(ConfigurationEnvironment.PROPERTIES_DOCUMENTS_KEY);
	}

	@Test
	public void testDefaultConfigurationLoader() {
		Assert.assertTrue("/tmp/documents".equals(configuration.getDocumentsRootPath()));
	}

	@Test
	public void testSystemPropertiesOverride() {
		System.setProperty(ConfigurationEnvironment.PROPERTIES_DOCUMENTS_KEY, "/tmp/xxx");

		Assert.assertTrue("/tmp/xxx".equals(configuration.getDocumentsRootPath()));
	}
}
