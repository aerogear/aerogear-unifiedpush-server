package org.jboss.aerogear.unifiedpush.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.aerogear.unifiedpush.spring.utils.ResourceUtils;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

	@Test
	public void testDefaultConfigurationLoader() {
		Configuration configuration = new Configuration();

		configuration.loadProperties();

		Assert.assertTrue("/tmp/documents".equals(configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY)));
	}

	@Test
	public void testClasspathConfigurationLoader() {
		Configuration configuration = new Configuration();
		System.setProperty(Configuration.PROPERTIES_FILE_KEY, "classpath:default.properties");

		configuration.loadProperties();

		Assert.assertTrue("/tmp/documents".equals(configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY)));
	}

	@Test
	public void testFileConfigurationLoader() {
		String propertiesPath = System.getProperty("java.io.tmpdir") + "/default.properties";
		Configuration configuration = new Configuration();
		System.setProperty(Configuration.PROPERTIES_FILE_KEY, "file:" + propertiesPath);
		InputStream is = null;
		try {
			is = ResourceUtils.getURL("classpath:default.properties").openStream();

			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer);
			String theString = writer.toString();

			FileUtils.write(new File(propertiesPath), theString);
		} catch (IOException e) {
			Assert.fail("Error writing file to tmp dir");
		}finally {
			IOUtils.closeQuietly(is);
		}

		configuration.loadProperties();

		Assert.assertTrue("/tmp/documents".equals(configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY)));
	}
}
