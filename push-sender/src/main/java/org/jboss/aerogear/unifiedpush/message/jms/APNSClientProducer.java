package org.jboss.aerogear.unifiedpush.message.jms;

import javax.ejb.Stateless;
import org.jboss.aerogear.unifiedpush.api.APNSVariant;

@Stateless
public class APNSClientProducer extends AbstractJMSMessageProducer {

    private static final String APNS_CLIENT = "APNSClient";

    public void changeAPNClient(final APNSVariant iOSVariant) {
        super.sendTransacted(APNS_CLIENT, iOSVariant, true);
    }

}
