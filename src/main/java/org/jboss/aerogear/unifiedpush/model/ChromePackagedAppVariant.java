package org.jboss.aerogear.unifiedpush.model;
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

import org.jboss.aerogear.unifiedpush.api.VariantType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * The Chrome Packaged Application variant class encapsulates GCM for Chrome specific behavior.
 * see http://developer.chrome.com/apps/cloudMessaging.html for more details
 */
@Entity
@DiscriminatorValue("'chromePackagedApp'")
public class ChromePackagedAppVariant extends AbstractVariant {
    private static final long serialVersionUID = -5473752252296190311L;

    public ChromePackagedAppVariant() {
        super();

        this.setType(VariantType.CHROME_PACKAGED_APP);
    }

    /**
     * This is the clientId of the created "application" in the Google API Console https://cloud.google.com/console
     */
    @Column
    @NotNull
    private String clientId;

    /**
     * This is the clientSecret of the created "application" in the Google API Console https://cloud.google.com/console
     */
    @Column
    @NotNull
    private String clientSecret;

    /**
     * This is the refreshToken for the created "application"
     */
    @Column
    @NotNull
    private String refreshToken;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
