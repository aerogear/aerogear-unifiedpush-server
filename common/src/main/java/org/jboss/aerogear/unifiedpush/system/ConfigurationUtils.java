/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.system;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigurationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);

    private ConfigurationUtils() { }

    /**
     * Try to retrieve a system property and returns the defaultValue if SecurityManager blocks it.
     *
     * @param key Name of the system property to get the string for.
     * @param defaultValue Value to be returned on unsuccessful operation or if the propety is not set.
     *
     * @return the value of the System property
     */
    private static String tryGetProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException e) {
            logger.error("Could not get value of property {} due to SecurityManager. Using default value.", key);
            return null;
        }
    }

    /**
     * Try to retrieve a system property and returns the defaultValue if SecurityManager blocks it.
     *
     * @param key Name of the system property to get the integer for.
     * @param defaultValue Value to be returned on unsuccessful operation or if the propety is not set.
     *
     * @return the value of the System property
     */
    private static Integer tryGetIntegerProperty(String key, Integer defaultValue) {
        try {
            return Integer.getInteger(key, defaultValue);
        } catch (SecurityException e) {
            logger.error("Could not get value of property {} due to SecurityManager. Using default value.", key, e);
            return defaultValue;
        }
    }

    /**
     * Format a key given system property key as an environment variable, e.g.:
     * custom.aerogear.apns.push.host would become CUSTOM_AEROGEAR_APNS_PUSH_HOST
     * @param key String System Property Key
     * @return String Key as environment variable
     */
    public static String formatEnvironmentVariable(String key) {
        return key.toUpperCase().replaceAll("\\.", "_");
    }

    /**
     * Get a global string property. This method will first try to get the value from an
     * environment variable and if that does not exist it will look up a system property.
     * @param key Name of the variable
     * @param defaultValue Returned if neither env var nor system property are defined
     * @return String the value of the Environment or System Property if defined, the given
     * default value otherwise
     */
    public static String tryGetGlobalProperty(String key, String defaultValue) {
        try {
            String value = System.getenv(formatEnvironmentVariable(key));
            if (value == null) {
                value = tryGetProperty(key, defaultValue);
            }
            return value;
        } catch (SecurityException e) {
            logger.error("Could not get value of global property {} due to SecurityManager. Using default value.", key, e);
            return defaultValue;
        }
    }

    /**
     * Same as `tryGetGlobalProperty` but with null as implicit default value
     * @param key Variable name
     * @return Environment or System property value or null if not found
     */
    public static String tryGetGlobalProperty(String key) {
        return tryGetGlobalProperty(key, null);
    }

    /**
     * Get a global integer property. This method will first try to get the value from an
     * environment variable and if that does not exist it will look up a system property.
     * @param key Name of the variable
     * @param defaultValue Returned if neither env var nor system property are defined
     * @return String the value of the Environment or System Property if defined, the given
     * default value otherwise
     */
    public static Integer tryGetGlobalIntegerProperty(String key, Integer defaultValue) {
        try {
            String value = System.getenv(formatEnvironmentVariable(key));
            if (value == null) {
                return tryGetIntegerProperty(key, defaultValue);
            } else {
                return Integer.parseInt(value);
            }
        } catch (SecurityException | NumberFormatException e) {
            logger.error("Could not get value of global property {} due to SecurityManager. Using default value.", key, e);
            return defaultValue;
        }
    }

    /**
     * Same as `tryGetGlobalIntegerProperty` but with null as implicit default value
     * @param key Variable name
     * @return Environment or System property value or null if not found
     */
    public static Integer tryGetGlobalIntegerProperty(String key) {
        return tryGetGlobalIntegerProperty(key, null);
    }
}
