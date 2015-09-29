package org.jboss.aerogear.unifiedpush.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	private static final String PROPERTIES_FILE_KEY = "aerogear.config";
	
	private Properties properties;
	
	@PostConstruct
	public void loadProperties() {
		final String propertiesFilePath = System.getProperty(PROPERTIES_FILE_KEY);
		properties = new Properties();
		
		if (propertiesFilePath != null) {
			File propertiesFile = new File(propertiesFilePath);
			try {
				loadPropertiesFromStream(new FileInputStream(propertiesFile));
			} catch (FileNotFoundException e) {
				logger.severe("cannot open file " + propertiesFilePath);
			} 
		} else {
			loadDefaultProperties();
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}
	
	public Properties getProperties() {
		return new PropertiesView(properties);
	}
	
	private void loadDefaultProperties() {
		InputStream inp = getClass().getClassLoader().getResourceAsStream("default.properties");
		if (inp == null) {
			logger.warning("default properties not found");
		} else {
			loadPropertiesFromStream(inp);
		}
	}
	
	private void loadPropertiesFromStream(InputStream inp) {
		try (InputStream in = inp) {
			properties.load(in);
		} catch (IOException e) {
		    logger.severe("Cannot load configuration", e);
		} 
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
