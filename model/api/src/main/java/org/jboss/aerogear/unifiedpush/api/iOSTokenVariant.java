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
import com.turo.pushy.apns.auth.ApnsSigningKey;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This class is similar to iOSVariant, but uses token signing instead of certificate identifitcation.
 */
public class iOSTokenVariant extends Variant implements IsAPNSVariant {
    private static final long serialVersionUID = -889367404039436329L;

    private boolean production;

    @NotNull
    @Size(max = 10, min = 10, message = "Team ID must be 10 characters long")
    @JsonIgnore
    private String teamId;

    @NotNull
    @Size(max = 10, min = 10, message = "Key ID must be 10 characters long")
    @JsonIgnore
    private String keyId;


    @NotNull(message = "Private key must be provided in p8 format")
    @JsonIgnore
    private String privateKey;

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
     * The APNs team ID is a value you set for developing apps. Obtain this value from your Apple developer account.
     *
     * @return the teamID
     */
    @JsonIgnore
    public String getTeamId() {
        return this.teamId;
    }

    @JsonProperty
    public void setTeamId(final String teamId) {
        this.teamId = teamId;
    }

    /**
     * The 10-character Key ID you obtained from your developer account
     *
     * @return the keyId
     */
    @JsonIgnore
    public String getKeyId() {
        return this.keyId;
    }

    @JsonProperty
    public void setKeyId(final String keyId) {
        this.keyId = keyId;
    }

    /**
     * The APNs certificate that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     *
     * @return the certificate
     */
    @JsonIgnore
    public String getPrivateKey() {
        return privateKey;
    }

    @JsonProperty
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public VariantType getType() {
        return VariantType.IOS_TOKEN;
    }

    /**
     * Validates whether the token/private key is in p8 format
     *
     * @return true if valid, otherwise false
     */
    @AssertTrue(message = "the provided private key must be in p8 format")
    @JsonIgnore
    public boolean isTokenValid() {
        try {
            InputStream targetStream = new ByteArrayInputStream(privateKey.getBytes());
            ApnsSigningKey.loadFromInputStream(targetStream, teamId, keyId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}