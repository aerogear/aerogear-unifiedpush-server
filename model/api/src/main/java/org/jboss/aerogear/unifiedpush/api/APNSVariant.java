package org.jboss.aerogear.unifiedpush.api;

/**
 * There are two APNS Variants, iOSVariant and iOSTokenVariant.  However, they also have some metadata that is shared
 * as well as needing to have their updates observed by SimpleApnsClientCache.  This is a interface that defines the
 * common fields to update the cache as well as mark a variant as having APNS information.
 */
public abstract class APNSVariant extends Variant {

    private boolean production = false;

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

}
