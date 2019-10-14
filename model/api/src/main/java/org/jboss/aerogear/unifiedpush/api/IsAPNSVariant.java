package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;

/**
 * There are two APNS Variants, iOSVariant and iOSTokenVariant.  However, they also have some metadata that is shared
 * as well as needing to have their updates observed by SimpleApnsClientCache.  This is a interface that defines the
 * common fields to update the cache as well as mark a variant as having APNS information.
 */
public interface IsAPNSVariant extends Serializable {
    boolean isProduction();
    void setProduction(boolean production);

    String getVariantID();
}
