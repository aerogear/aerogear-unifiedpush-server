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

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.martijndwars.webpush.Utils;
import org.jboss.aerogear.unifiedpush.utils.KeyUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @Size(max = 255, message = "Private Key must be max. 255 chars long")
    private String privateKey;

    /**
     * This is a VAPID public key.  It must match the private key.
     * See https://tools.ietf.org/html/draft-ietf-webpush-vapid-01
     * @return
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
     * @return
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

}
