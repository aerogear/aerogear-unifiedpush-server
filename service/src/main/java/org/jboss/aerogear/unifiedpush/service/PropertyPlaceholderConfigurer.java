package org.jboss.aerogear.unifiedpush.service;


import java.util.Properties;

import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

public class PropertyPlaceholderConfigurer {
	private final AeroGearLogger logger = AeroGearLogger.getInstance(PropertyPlaceholderConfigurer.class);
	
	/** Never check system properties. */
	public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

	/**
	 * Check system properties if not resolvable in the specified properties.
	 * This is the default.
	 */
	public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

	/**
	 * Check system properties first, before trying the specified properties.
	 * This allows system properties to override any other property source.
	 */
	public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;

	private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;
	
	
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		this.systemPropertiesMode = systemPropertiesMode;
	}

	protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
		String propVal = null;
		if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
			propVal = resolveSystemProperty(placeholder);
		}
		if (propVal == null) {
			propVal = props.getProperty(placeholder);
		}
		if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
			propVal = resolveSystemProperty(placeholder);
		}
		return propVal;
	}

	/**
	 * Resolve the given key as JVM system property, and optionally also as
	 * system environment variable if no matching system property has been found.
	 * @param key the placeholder to resolve as system property key
	 * @return the system property value, or {@code null} if not found
	 * @see System#getProperty(String)
	 * @see System#getenv(String)
	 */
	protected String resolveSystemProperty(String key) {
		try {
			String value = System.getProperty(key);
			if (value == null) {
				value = System.getenv(key);
			}
			return value;
		}
		catch (Throwable ex) {
			logger.warning("Could not access system property '" + key + "': " + ex);
			return null;
		}
	}
	
	/**
	 * Resolve the given placeholder using the given properties, performing
	 * a system properties check according to the given mode.
	 * <p>The default implementation delegates to {@code resolvePlaceholder
	 * (placeholder, props)} before/after the system properties check.
	 * <p>Subclasses can override this for custom resolution strategies,
	 * including customized points for the system properties check.
	 * @param placeholder the placeholder to resolve
	 * @param props the merged properties of this configurer
	 * @param systemPropertiesMode the system properties mode,
	 * according to the constants in this class
	 * @return the resolved value, of null if none
	 * @see #resolvePlaceholder(String, java.util.Properties)
	 */
	public String resolvePlaceholder(String placeholder, Properties props) {
		return resolvePlaceholder(placeholder, props, systemPropertiesMode);
	}

}