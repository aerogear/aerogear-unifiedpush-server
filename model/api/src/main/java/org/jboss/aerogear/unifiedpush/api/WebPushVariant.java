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
import nl.martijndwars.webpush.Utils;
import org.jboss.aerogear.unifiedpush.utils.KeyUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * The WebPush variant uses self-generated (and optionally provided) VAPID keys for push.
 */
public class WebPushVariant extends Variant {

    private static final long serialVersionUID = -1873585264296190331L;

    @NotNull
    @Size(min = 1, max = 255, message = "Public Key must be max. 255 chars long")
    private String publicKey;

    /**
     * TODO: Find a way to store this securely
     */
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 255, message = "Private Key must be max. 255 chars long")
    private String privateKey;

    @NotNull
    @Size(max = 255, message = "Alias Must be a max of 255 Chars")
    private String alias;

    /**
     * This is a VAPID public key.  It must match the private key.
     * See https://tools.ietf.org/html/draft-ietf-webpush-vapid-01
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * This is a VAPID private key.  It must match the public key.
     * See https://tools.ietf.org/html/draft-ietf-webpush-vapid-01
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public VariantType getType() {
        return VariantType.WEB_PUSH;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Validates whether the certificate/passphrase pair
     * is valid, and does not contain any bogus content.
     *
     * @return true if valid, otherwise false
     */
    @AssertTrue(message = "the provided private key does not match with the public key")
    @JsonIgnore
    public boolean isKeypairValid() {
        try {
            PrivateKey privateKeyObject = KeyUtils.loadPrivateKey(getPrivateKey());
            PublicKey publicKeyObject = KeyUtils.loadPublicKey(getPublicKey());
            return Utils.verifyKeyPair(privateKeyObject, publicKeyObject);
        } catch (Exception e) {
            return false;
        }
    }

    @AssertTrue(message = "Alias must be a url or mailto")
    @JsonIgnore
    public boolean isAliasURLorMailto() {
        try {
            if (alias == null || alias.isEmpty()) {
                return false;
            }
            if (alias.toLowerCase().startsWith("mailto:")) {
                return alias.contains("@");
            }
            new java.net.URL(alias);
            return true;
        } catch (MalformedURLException e) {
            //Bad practice to use an exception to check if a URL is valid, but I don't want to include a library for the purpose.
            return false;
        } catch (Exception e) {//We didn't expect this exception
            return false;
        }
    }

    /**
     * Applies the non null values of this class to the webPushVariant param
     * @param webPushVariant value to have non null values of this instance applied
     */
    public void merge(WebPushVariant webPushVariant) {
        if (getPublicKey() != null && !getPublicKey().isBlank()) {
            webPushVariant.setPublicKey(getPublicKey());
        }
        if (getPrivateKey() != null && !getPrivateKey().isBlank()) {
            webPushVariant.setPrivateKey(getPrivateKey());
        }
        if (getName() != null && !getName().isBlank()) {
            webPushVariant.setName(getName());
        }
        if (getDescription() != null && !getDescription().isBlank()) {
            webPushVariant.setDescription(getDescription());
        }

        if (getAlias() != null && !getAlias().isBlank()) {
            webPushVariant.setAlias(getAlias());
        }
    }
}
