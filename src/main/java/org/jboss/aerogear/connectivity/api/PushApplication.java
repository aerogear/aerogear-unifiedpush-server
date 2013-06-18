package org.jboss.aerogear.connectivity.api;

import java.util.Set;

import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.model.iOSVariant;

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
     * The master password, used for sending message to a {@link PushApplication} or its {@link MobileVariant}s.
     */
    void setMasterSecret(String secret);
    String getMasterSecret();

    /**
     * The collection of iOS Variants. 
     */
    void setIOSApps(final Set<iOSVariant> iOSApps);
    Set<iOSVariant> getIOSApps();

    /**
     * The collection of Android Variants. 
     */
    void setAndroidApps(final Set<AndroidVariant> androidApps);
    Set<AndroidVariant> getAndroidApps();

    /**
     * The collection of SimplePush Variants. 
     */
    void setSimplePushApps(final Set<SimplePushVariant> simplePushApps);
    Set<SimplePushVariant> getSimplePushApps();

}
