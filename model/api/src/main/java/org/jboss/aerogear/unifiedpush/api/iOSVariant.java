/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Base64;

/**
 * The iOS variant class encapsulates APNs specific behavior.
 */
public class iOSVariant extends APNSVariant {
    private static final long serialVersionUID = -889367404039436329L;


    @NotNull
    @Size(max = 255, message = "Passphrase must be 1-255 characters long")
    @JsonIgnore
    private String passphrase;

    @NotNull(message = "Certificate must be provided")
    @JsonIgnore
    private String certificateData;

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
        return Base64.getDecoder().decode(certificateData);
    }

    @JsonProperty
    public void setCertificate(byte[] cert) {
        this.certificateData = Base64.getEncoder().encodeToString(cert);
    }

    @Override
    public VariantType getType() {
        return VariantType.IOS;
    }
}
