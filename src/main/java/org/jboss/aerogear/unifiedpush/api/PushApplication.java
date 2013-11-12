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

import java.util.Set;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;

/**
 * Logical construct of an overall mobile and push-enabled Application
 */
public interface PushApplication {

    /**
     * The name of the application. 
     */
    void setName(final String name);

    String getName();

    /**
     * The description of the app.
     */
    void setDescription(final String description);

    String getDescription();

    /**
     * Identifier used to register variants with this PushApplication
     */
    void setPushApplicationID(String pushApplicationID);

    String getPushApplicationID();

    /**
     * The master password, used for sending message to a {@link PushApplication} or its {@link Variant}s.
     */
    void setMasterSecret(String secret);

    String getMasterSecret();

    /**
     * The collection of iOS Variants. 
     */
    void setIOSVariants(final Set<iOSVariant> iOSApps);

    Set<iOSVariant> getIOSVariants();

    /**
     * The collection of Android Variants. 
     */
    void setAndroidVariants(final Set<AndroidVariant> androidApps);

    Set<AndroidVariant> getAndroidVariants();

    /**
     * The collection of SimplePush Variants. 
     */
    void setSimplePushVariants(final Set<SimplePushVariant> simplePushApps);

    Set<SimplePushVariant> getSimplePushVariants();

}
