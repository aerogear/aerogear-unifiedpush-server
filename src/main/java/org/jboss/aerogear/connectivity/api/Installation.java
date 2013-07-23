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
package org.jboss.aerogear.connectivity.api;

/**
 * One installation of a push-enabled client, running on a device (Android/iOS)
 * or user-agent (SimplePush)
 */
public interface Installation {

    /**
     * Flag if the actual client installation is enabled (default) or not.
     * Disabled installations are not receiving push notifications.
     * 
     * Used by the admin UI to disable specific clients.
     * 
     * @param enabled if <code>true</code> the Installation is marked as enabled,
     * setting it to <code>false</code> disables it.
     */
    void setEnabled(boolean enabled);

    boolean isEnabled();

    /**
     * Identifies the device/user-agent within its PushNetwork.
     * 
     * <ul>
     * <li> APNs: <code>deviceToken</code>
     * <li> GCM: <code>registrationId</code>
     * <li> SimplePush: <code>channelId</code>
     * </ul>
     * 
     * @param deviceToken unique string to identify an Installation with its PushNetwork
     */
    void setDeviceToken(final String deviceToken);

    String getDeviceToken();

    /**
     * The device type of the device or the user agent.
     * 
     * <li> For SimplePush this will be <code>Web</code>
     * <li> For iOS that could be <code>iPod</code>, <code>iPad</code> or <code>iPhone</code>
     * <li> For Android that could be <code>Phone</code> or <code>Tablet</code>
     * 
     * @param deviceType the type of the registered device
     */
    void setDeviceType(final String deviceType);

    String getDeviceType();

    /**
     * The (optional) name of the underlying Operating System.
     * 
     * @param mobileOperatingSystem the name of the Operating System.
     */
    void setMobileOperatingSystem(final String mobileOperatingSystem);

    String getMobileOperatingSystem();

    /**
     * The (optional) version of the used Operating System.
     * 
     * @param osVersion the version string of the mobile OS.
     */
    void setOsVersion(final String osVersion);

    String getOsVersion();

    /**
     * Application specific alias to identify users with the system. 
     * E.g. email address or username
     * 
     * @param clientIdentifier string to map the Installation to an actual user. 
     */
    void setAlias(final String clientIdentifier);

    String getAlias();

    /**
     * Used for SimplePush, to "tag" the channel
     * 
     * @param category the name of the 'tagged' category
     */
    void setCategory(final String category);

    String getCategory();
}
