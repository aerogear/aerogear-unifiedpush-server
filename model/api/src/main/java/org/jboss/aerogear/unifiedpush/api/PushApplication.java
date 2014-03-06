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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Logical construct of an overall mobile and push-enabled Application
 */
public class PushApplication extends BaseModel {

    private String name;
    private String description;
    private String pushApplicationID = UUID.randomUUID().toString();
    private String masterSecret = UUID.randomUUID().toString();
    private String developer;
    private Set<iOSVariant> iOSVariants = new HashSet<iOSVariant>();
    private Set<AndroidVariant> androidVariants = new HashSet<AndroidVariant>();
    private Set<SimplePushVariant> simplePushVariants = new HashSet<SimplePushVariant>();
    private Set<ChromePackagedAppVariant> chromePackagedAppVariants = new HashSet<ChromePackagedAppVariant>();

    /**
     * The name of the application. 
     */
    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * The description of the app.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Identifier used to register variants with this PushApplication
     */
    public void setPushApplicationID(String pushApplicationID) {
        this.pushApplicationID = pushApplicationID;
    }

    public String getPushApplicationID() {
        return pushApplicationID;
    }

    /**
     * The master password, used for sending message to a {@link PushApplication} or its {@link Variant}s.
     */
    public void setMasterSecret(String masterSecret) {
        this.masterSecret = masterSecret;
    }

    public String getMasterSecret() {
        return masterSecret;
    }


    /**
     * The collection of iOS Variants. 
     */
    public void setIOSVariants(final Set<iOSVariant> iOSVariants) {
        this.iOSVariants = iOSVariants;
    }

    public Set<iOSVariant> getIOSVariants() {
        return iOSVariants;
    }

    /**
     * The collection of Android Variants. 
     */
    public void setAndroidVariants(final Set<AndroidVariant> androidVariants) {
        this.androidVariants = androidVariants;
    }

    public Set<AndroidVariant> getAndroidVariants() {
        return androidVariants;
    }

    /**
     * The collection of SimplePush Variants. 
     */
    public void setSimplePushVariants(final Set<SimplePushVariant> simplePushVariants){
        this.simplePushVariants = simplePushVariants;
    }

    public Set<SimplePushVariant> getSimplePushVariants() {
        return simplePushVariants;
    }

    /**
     * The collection of 'ChromePackagedApp' Variants.
     */
    public void setChromePackagedAppVariants(final Set<ChromePackagedAppVariant> chromePackagedAppVariants) {
        this.chromePackagedAppVariants = chromePackagedAppVariants;
    }

    public Set<ChromePackagedAppVariant> getChromePackagedAppVariants() {
        return chromePackagedAppVariants;
    }

    /**
     * The developer which created the app.
     */
    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

}
