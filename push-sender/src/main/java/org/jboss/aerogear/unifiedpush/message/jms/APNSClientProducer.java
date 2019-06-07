package org.jboss.aerogear.unifiedpush.message.jms;

import org.jboss.aerogear.unifiedpush.api.iOSVariant;

import javax.ejb.Stateless;

@Stateless
public class APNSClientProducer extends AbstractJMSMessageProducer {

    private static final String APNS_CLIENT = "APNSClient";

    public void changeAPNClient(final iOSVariant iOSVariant) {
        super.sendTransacted(APNS_CLIENT, iOSVariant, true);
    }

}
