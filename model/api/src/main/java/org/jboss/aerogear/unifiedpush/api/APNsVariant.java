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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * The iOS variant class encapsulates APNs specific behavior.
 */
public class APNsVariant extends Variant {
    private static final long serialVersionUID = -889367404039436329L;

    private boolean production;

    @NotNull
    @Size(max = 255)
    @JsonIgnore
    private String passphrase;

    @NotNull
    @JsonIgnore
    private byte[] certificate;

    /**
     * If <code>true</code> a connection to Apple's Production APNs server
     * will be established for this iOS variant.
     *
     * If the method returns <code>false</code> a connection to
     * Apple's Sandbox/Development APNs server will be established
     * for this iOS variant.
     */
    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    /**
     * The APNs passphrase that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     */
    @JsonIgnore
    public String getPassphrase() {
        return this.passphrase;
    }

    @JsonProperty
    public void setPassphrase(final String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * The APNs certificate that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     */
    @JsonIgnore
    public byte[] getCertificate() {
        return certificate;
    }

    @JsonProperty
    public void setCertificate(byte[] cert) {
        this.certificate = cert;
    }

    @Override
    public VariantType getType() {
        return VariantType.APNS;
    }
}
