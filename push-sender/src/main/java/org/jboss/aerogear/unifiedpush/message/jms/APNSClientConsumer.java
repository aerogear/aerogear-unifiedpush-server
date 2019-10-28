package org.jboss.aerogear.unifiedpush.message.jms;

import javax.inject.Inject;
import org.jboss.aerogear.unifiedpush.api.APNSVariant;
import org.jboss.aerogear.unifiedpush.message.cache.SimpleApnsClientCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class consumes events relating to changes to APNS Variants.
 */
public class APNSClientConsumer extends AbstractJMSMessageListener<APNSVariant> {

    @Inject
    SimpleApnsClientCache apnsClientCache;

    private static final Logger logger = LoggerFactory.getLogger(APNSClientConsumer.class);

    @Override
    public void onMessage(final APNSVariant iOSVariant) {
        logger.info("Resetting iOS with new cert");
        apnsClientCache.disconnectOnChange(iOSVariant);
    }

}
