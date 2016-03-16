package org.jboss.aerogear.unifiedpush.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.aerogear.unifiedpush.spring.utils.ResourceUtils;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@Singleton
@Startup
public class Configuration {

	private final AeroGearLogger logger = AeroGearLogger.getInstance(Configuration.class);

	public static final String PROPERTIES_FILE_KEY = "aerogear.config";
	public static final String PROPERTIES_DOCUMENTS_KEY = "aerogear.config.document.path.root";
	public static final String PROP_ENABLE_VERIFICATION = "aerogear.config.verification.enable_verification";

	private Properties properties;
	private PropertyPlaceholderConfigurer configurer;

	@PostConstruct
	public void loadProperties() {
		final String propertiesFilePath = System.getProperty(PROPERTIES_FILE_KEY);
		properties = new Properties();
		configurer = new PropertyPlaceholderConfigurer();
		InputStream is = null;

		if (propertiesFilePath != null) {
			try {
				is = ResourceUtils.getURL(propertiesFilePath).openStream();

				loadPropertiesFromStream(is);
			} catch (IOException e) {
				logger.severe("cannot open file " + propertiesFilePath);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// Nothig  we can do.
					}
				}
			}
		} else {
			loadDefaultProperties();
		}

		logger.info("Using runtime properties: " + properties.toString());
	}

	private void loadDefaultProperties() {
		InputStream inp = getClass().getClassLoader().getResourceAsStream("default.properties");
		if (inp == null) {
			logger.warning("default properties not found");
		} else {
			logger.warning("loading default properties from class path resource default.properties!");
			logger.warning("use -Daerogear.config=/PATH inorder to prevent this warning!");
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

	public String getProperty(String key) {
		return configurer.resolvePlaceholder(key, properties);
	}

	public boolean getProperty(String key, boolean defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}

	public Properties getProperties() {
		return new PropertiesView(properties, configurer);
	}

	public void setSystemPropertiesMode(int systemPropertiesMode) {
		this.configurer.setSystemPropertiesMode(systemPropertiesMode);
	}

	@SuppressWarnings("serial")
	private class PropertiesView extends Properties {
		private final Properties properties;
		private final PropertyPlaceholderConfigurer configurer;

		public PropertiesView(Properties properties, PropertyPlaceholderConfigurer configurer) {
			this.properties = properties;
			this.configurer = configurer;
		}

		public String getProperty(String key) {
			return configurer.resolvePlaceholder(key, properties);
		}
	}
}
