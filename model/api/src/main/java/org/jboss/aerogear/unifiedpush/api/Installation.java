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
package org.jboss.aerogear.unifiedpush.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jboss.aerogear.unifiedpush.api.validation.DeviceTokenCheck;

import java.util.HashSet;
import java.util.Set;

/**
 * One installation of a push-enabled client, running on a device (Android/iOS)
 */
@DeviceTokenCheck
public class Installation extends BaseModel {
    private static final long serialVersionUID = 7177135979544758234L;

    private boolean enabled = true;
    private String deviceToken;
    private String deviceType;
    private String operatingSystem;
    private String osVersion;
    private String alias;

    private Set<Category> categories = new HashSet<>();
    private String platform;
    @JsonIgnore
    private Variant variant;

    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Flag if the actual client installation is enabled (default) or not.
     * Disabled installations are not receiving push notifications.
     *
     * Used by the admin UI to disable specific clients.
     *
     * @param enabled if <code>true</code> the Installation is marked as enabled,
     * setting it to <code>false</code> disables it.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    /**
     * Identifies the device/user-agent within its PushNetwork.
     *
     * <ul>
     * <li> APNs: <code>deviceToken</code>
     * <li> GCM: <code>registrationId</code>
     * </ul>
     *
     * @param deviceToken unique string to identify an Installation with its PushNetwork
     */
    public void setDeviceToken(final String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    /**
     * The device type of the device or the user agent.
     *
     * <ul>
     * <li> For iOS that could be <code>iPod</code>, <code>iPad</code> or <code>iPhone</code>
     * <li> For Android that could be <code>Phone</code> or <code>Tablet</code>
     * </ul>
     *
     * @param deviceType the type of the registered device
     */
    public void setDeviceType(final String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperatingSystem() {
        return this.operatingSystem;
    }

    /**
     * The (optional) name of the underlying Operating System.
     *
     * @param operatingSystem the name of the Operating System.
     */
    public void setOperatingSystem(final String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    /**
     * The (optional) version of the used Operating System.
     *
     * @param osVersion the version string of the mobile OS.
     */
    public void setOsVersion(final String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAlias() {
        return this.alias;
    }

    /**
     * Application specific alias to identify users with the system.
     * E.g. email address or username
     *
     * @param alias string to map the Installation to an actual user.
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * Used to "tag" the clients. E.g. apply sport, news etc
     *
     * @param categories set of all categories the client is in
     */
    public void setCategories(final Set<Category> categories) {
        this.categories = categories;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    /**
     * A reliable way of determining the platform type
     * for an installation. FOR ADMIN UI ONLY - Helps with setting up Routes
     *
     * @param platform the name of the platform
     */
    public void setPlatform(final String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
}
