package org.jboss.aerogear.unifiedpush.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@Singleton
@Startup
public class Configuration {
	
    private final AeroGearLogger logger = AeroGearLogger.getInstance(Configuration.class);

	// todo: rename
	private static final String PROPERTIES_FILE_KEY = "aerogear.config";
	
	private Properties properties;
	
	@PostConstruct
	public void loadProperties() {
		final String propertiesFilePath = System.getProperty(PROPERTIES_FILE_KEY);
		properties = new Properties();
		
		if (propertiesFilePath != null) {
			File propertiesFile = new File(propertiesFilePath);
			try (InputStream inp = new FileInputStream(propertiesFile)) {
				properties.load(inp);
			} catch (IOException e) {
			    logger.severe("Cannot load configuration", e);
			} 
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public Properties getProperties() {
		return new PropertiesView(properties);
	}
	
	@SuppressWarnings("serial")
	private class PropertiesView extends Properties {
		private final Properties properties;
		
		public PropertiesView(Properties properties) {
			this.properties = properties;
		}
		
		public String getProperty(String key) {
			return properties.getProperty(key);
		}
	}
}
