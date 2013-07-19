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
package org.jboss.aerogear.connectivity.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import org.jboss.aerogear.connectivity.api.VariantType;

/**
 * The iOS variant class encapsulates APNs specific behavior.
 */
@Entity
@DiscriminatorValue("'ios'")
public class iOSVariant extends AbstractVariant {
    private static final long serialVersionUID = -889367404039436329L;

    public iOSVariant() {
        super();
        // we are iOS:
        this.type = VariantType.IOS;
    }

    @Column
    private boolean production;

    @Column
    @NotNull
    private String passphrase;

    @Lob
    @Column(name = "CERT")
    @NotNull
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
    public String getPassphrase() {
        return this.passphrase;
    }

    public void setPassphrase(final String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * The APNs certificate that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     */
    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] cert) {
        this.certificate = cert;
    }
}
