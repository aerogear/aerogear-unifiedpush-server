package org.jboss.aerogear.unifiedpush.message.jms;

import org.jboss.aerogear.unifiedpush.api.IsAPNSVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;

import javax.ejb.Stateless;

@Stateless
public class APNSClientProducer extends AbstractJMSMessageProducer {

    private static final String APNS_CLIENT = "APNSClient";

    public void changeAPNClient(final IsAPNSVariant iOSVariant) {
        super.sendTransacted(APNS_CLIENT, iOSVariant, true);
    }

}
