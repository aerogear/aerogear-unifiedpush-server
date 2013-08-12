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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.jboss.aerogear.unifiedpush.jpa.PersistentObject;

@Entity
public class PushApplication extends PersistentObject implements org.jboss.aerogear.unifiedpush.api.PushApplication {
    private static final long serialVersionUID = 6507691362454032282L;

    public PushApplication() {
        masterSecret = UUID.randomUUID().toString();
    }

    @Column
    @NotNull
    private String name;
    @Column
    private String description;

    @Column
    private String pushApplicationID;
    @Column
    private String masterSecret;

    @Column
    private String developer;

    // TODO: let's do LAZY
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn
    private Set<iOSVariant> iOSVariants = new HashSet<iOSVariant>();

    // TODO: let's do LAZY
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn
    private Set<AndroidVariant> androidVariants = new HashSet<AndroidVariant>();

    // TODO: let's do LAZY
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn
    private Set<SimplePushVariant> simplePushVariants = new HashSet<SimplePushVariant>();

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<iOSVariant> getIOSVariants() {
        return this.iOSVariants;
    }

    public void setIOSVariants(final Set<iOSVariant> iOSVariants) {
        this.iOSVariants = iOSVariants;
    }

    public Set<AndroidVariant> getAndroidVariants() {
        return this.androidVariants;
    }

    public void setAndroidVariants(final Set<AndroidVariant> androidVariants) {
        this.androidVariants = androidVariants;
    }

    public Set<SimplePushVariant> getSimplePushVariants() {
        return simplePushVariants;
    }

    public void setSimplePushVariants(final Set<SimplePushVariant> simplePushVariants) {
        this.simplePushVariants = simplePushVariants;
    }

    public String getPushApplicationID() {
        return pushApplicationID;
    }

    public void setPushApplicationID(String pushApplicationID) {
        this.pushApplicationID = pushApplicationID;
    }

    public void setMasterSecret(String secret) {
        this.masterSecret = secret;
    }

    @Override
    public String getMasterSecret() {
        return masterSecret;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }
}
