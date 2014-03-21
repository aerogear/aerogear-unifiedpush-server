package org.jboss.aerogear.unifiedpush.api;
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

/**
 * The Chrome Packaged Application variant class encapsulates GCM for Chrome specific behavior.
 * see http://developer.chrome.com/apps/cloudMessaging.html for more details
 */
import javax.validation.constraints.NotNull;

public class ChromePackagedAppVariant extends Variant {
    private static final long serialVersionUID = -5473752252296190311L;

    @NotNull
    private String clientId;

    @NotNull
    private String clientSecret;

    @NotNull
    private String refreshToken;

    /**
     * This is the clientId of the created "application" in the Google API Console https://cloud.google.com/console
     */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * This is the clientSecret of the created "application" in the Google API Console https://cloud.google.com/console
     */
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * This is the refreshToken for the created "application"
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public VariantType getType() {
        return VariantType.CHROME_PACKAGED_APP;
    }
}
