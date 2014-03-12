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
package org.jboss.aerogear.unifiedpush.model.jpa;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.jpa.PersistentObject;
import org.jboss.aerogear.unifiedpush.model.jpa.validation.DeviceTokenCheck;

import javax.persistence.*;
import java.util.Set;

@Entity
@DeviceTokenCheck
public class InstallationEntity extends PersistentObject {
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

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "category")
    private Set<String> categories;
    @Column
    private String platform;
    @Column
    private String simplePushEndpoint;

    @Column
    private VariantType variantType;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public void setDeviceToken(final String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(final String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperatingSystem() {
        return this.operatingSystem;
    }

    public void setOperatingSystem(final String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public void setOsVersion(final String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(final Set<String> categories) {
        this.categories = categories;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public void setSimplePushEndpoint(String simplePushEndpoint) {
        this.simplePushEndpoint = simplePushEndpoint;
    }

    public String getSimplePushEndpoint() {
        return simplePushEndpoint;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }
}
