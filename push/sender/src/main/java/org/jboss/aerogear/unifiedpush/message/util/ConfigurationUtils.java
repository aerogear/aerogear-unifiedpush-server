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
package org.jboss.aerogear.unifiedpush.message.util;


import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

public final class ConfigurationUtils {

    private final static AeroGearLogger logger = AeroGearLogger.getInstance(ConfigurationUtils.class);

    private ConfigurationUtils() {
        // no-op
    }

    /**
     * Try to retrieve a system property and returns null if SecurityManager blocks it.
     *
     * @param key Name of the system property to get the string for.
     *
     * @return the value of the System property
     */
    public static String tryGetProperty(String key) {
        return tryGetProperty(key, null);
    }

    /**
     * Try to retrieve a system property and returns the defaultValue if SecurityManager blocks it.
     *
     * @param key Name of the system property to get the string for.
     * @param defaultValue Value to be returned on unsuccessful operation or if the propety is not set.
     *
     * @return the value of the System property
     */
    public static String tryGetProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException e) {
            logger.severe("Could not get value of property " + key + " due to SecurityManager. Using null value.");
            return null;
        }
    }

    /**
     * Try to retrieve a system property and returns null if SecurityManager blocks it.
     *
     * @param key Name of the system property to get the integer for.
     * @return the value of the System property
     */
    public static Integer tryGetIntegerProperty(String key) {
        return tryGetIntegerProperty(key, null);
    }

    /**
     * Try to retrieve a system property and returns the defaultValue if SecurityManager blocks it.
     *
     * @param key Name of the system property to get the integer for.
     * @param defaultValue Value to be returned on unsuccessful operation or if the propety is not set.
     *
     * @return the value of the System property
     */
    public static Integer tryGetIntegerProperty(String key, Integer defaultValue) {
        try {
            return Integer.getInteger(key, defaultValue);
        } catch (SecurityException e) {
            logger.severe("Could not get value of property " + key + " due to SecurityManager. Using null value.");
            return defaultValue;
        }
    }



}
