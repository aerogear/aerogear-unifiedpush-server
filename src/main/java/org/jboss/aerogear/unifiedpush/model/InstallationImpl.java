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
package org.jboss.aerogear.unifiedpush.model;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.jpa.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class InstallationImpl extends PersistentObject implements Installation {
    private static final long serialVersionUID = 7177135979544758234L;

    @Column
    private boolean enabled = true;
    @Column
    private String deviceToken;
    @Column
    private String deviceType;
    @Column
    private String operatingSystem;
    @Column
    private String osVersion;
    @Column
    private String alias;
    @Column
    private List<String> categories;
    @Column
    private String platform;
    @Column
    private String simplePushEndpoint;

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getDeviceToken() {
        return this.deviceToken;
    }

    @Override
    public void setDeviceToken(final String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public String getDeviceType() {
        return this.deviceType;
    }

    @Override
    public void setDeviceType(final String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String getOperatingSystem() {
        return this.operatingSystem;
    }

    @Override
    public void setOperatingSystem(final String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    @Override
    public String getOsVersion() {
        return this.osVersion;
    }

    @Override
    public void setOsVersion(final String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    public String getAlias() {
        return this.alias;
    }

    @Override
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public void setCategories(final List<String> categories) {
        this.categories = categories;
    }

    @Override
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public void setSimplePushEndpoint(String simplePushEndpoint) {
        this.simplePushEndpoint = simplePushEndpoint;
    }

    @Override
    public String getSimplePushEndpoint() {
        return simplePushEndpoint;
    }
}
