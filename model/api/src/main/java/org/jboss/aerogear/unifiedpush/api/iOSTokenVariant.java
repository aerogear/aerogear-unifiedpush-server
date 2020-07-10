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
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This class is similar to iOSVariant, but uses token signing instead of certificate identifitcation.
 */
public class iOSTokenVariant extends APNSVariant {
    private static final long serialVersionUID = -889367404039436329L;


    @NotNull
    @Size(max = 10, min = 10, message = "Team ID must be 10 characters long")
    private String teamId;

    @NotNull
    @Size(max = 10, min = 10, message = "Key ID must be 10 characters long")
    private String keyId;


    @NotNull(message = "Private key must be provided in p8 format")
    @JsonIgnore
    private String privateKey;

    @NotNull(message = "Bundle ID is required.")
    private String bundleId;


    /**
     * The APNs team ID is a value you set for developing apps. Obtain this value from your Apple developer account.
     *
     * @return the teamID
     */
    public String getTeamId() {
        return this.teamId;
    }

    public void setTeamId(final String teamId) {
        this.teamId = teamId;
    }

    /**
     * The 10-character Key ID you obtained from your developer account
     *
     * @return the keyId
     */
    public String getKeyId() {
        return this.keyId;
    }

    public void setKeyId(final String keyId) {
        this.keyId = keyId;
    }

    /**
     * The APNs privateKey that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     *
     * @return the privateKey
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

    /**
     * Bundle ID is the unique identified for your app in APNS.
     *
     * @return bundle id
     */
    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    /**
     * Applies non null values to the provided iOSTokenVariant
     * @param iOSTokenVariant a value that will be updated using non null fields of this variant instance
     */
    public void merge(iOSTokenVariant iOSTokenVariant) {
        if (this.getName() != null && !this.getName().isBlank()) {
            iOSTokenVariant.setName(this.getName());
        }

        if (this.getDescription() != null && !this.getDescription().isBlank()) {
            iOSTokenVariant.setDescription(this.getDescription());
        }
        if (this.production() != null) {
            iOSTokenVariant.setProduction(this.isProduction());
        }
        if (this.getKeyId() != null && !this.getKeyId().isBlank()) {
            iOSTokenVariant.setKeyId(this.getKeyId());
        }
        if (this.getBundleId() != null && !this.getBundleId().isBlank()) {
            iOSTokenVariant.setBundleId(this.getBundleId());
        }
        if (this.getTeamId() != null && !this.getTeamId().isBlank()) {
            iOSTokenVariant.setTeamId(this.getTeamId());
        }
        if (this.getPrivateKey() != null && !this.getPrivateKey().isBlank()) {
            iOSTokenVariant.setPrivateKey(this.getPrivateKey());
        }

    }
}
