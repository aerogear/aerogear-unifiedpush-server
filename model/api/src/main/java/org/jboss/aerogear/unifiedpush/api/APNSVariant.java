package org.jboss.aerogear.unifiedpush.api;

import javax.persistence.Access;
import javax.persistence.AccessType;

/**
 * There are two APNS Variants, iOSVariant and iOSTokenVariant.  However, they also have some metadata that is shared
 * as well as needing to have their updates observed by SimpleApnsClientCache.  This is a interface that defines the
 * common fields to update the cache as well as mark a variant as having APNS information.
 */
public abstract class APNSVariant extends Variant {

    @Access(AccessType.PROPERTY)
    private Boolean production;

    /**
     * If <code>true</code> a connection to Apple's Production APNs server
     * will be established for this iOS variant.
     * <p>
     * If the method returns <code>false</code> a connection to
     * Apple's Sandbox/Development APNs server will be established
     * for this iOS variant.
     *
     * @return production state defaults to false
     */
    public boolean isProduction() {
        return production != null ? production : false;
    }

    /**
     * This returns the instance of production. This method is used to test if
     * a production value was set. You probably want to use isProduction unless
     * you have a reason to check for the existence of production instead of
     * its value.
     *
     * @return the production Boolean object, may be null.
     */
    public Boolean production() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

}
