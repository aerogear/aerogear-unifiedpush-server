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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import net.iharder.Base64;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;

/**
 * The iOS variant class encapsulates APNs specific behavior.
 */
public class iOSVariant extends Variant {
    private static final long serialVersionUID = -889367404039436329L;

    private boolean production = false;

    @NotNull
    @Size(max = 255, message = "Passphrase must be 1-255 characters long")
    @JsonIgnore
    private String passphrase;

    @NotNull(message = "Certificate must be provided")
    @JsonIgnore
    private String certificateData;

    /**
     * If <code>true</code> a connection to Apple's Production APNs server
     * will be established for this iOS variant.
     *
     * If the method returns <code>false</code> a connection to
     * Apple's Sandbox/Development APNs server will be established
     * for this iOS variant.
     *
     * @return production state
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
     *
     * @return the passphrase
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
     *
     * @return the certificate
     */
    @JsonIgnore
    public byte[] getCertificate() {
        try {
            return Base64.decode(certificateData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonProperty
    public void setCertificate(byte[] cert) {
        this.certificateData = Base64.encodeBytes(cert);
    }

    @Override
    public VariantType getType() {
        return VariantType.IOS;
    }
}
