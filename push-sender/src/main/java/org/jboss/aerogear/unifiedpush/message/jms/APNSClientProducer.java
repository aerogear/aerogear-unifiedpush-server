package org.jboss.aerogear.unifiedpush.message.jms;

import org.jboss.aerogear.unifiedpush.api.iOSVariant;

import javax.ejb.Stateless;

@Stateless
public class APNSClientProducer extends AbstractJMSMessageProducer {

    private String apnsClient = "topic/APNSClient";

    public void changeAPNClient(iOSVariant iOSVariant) {
        super.sendTransacted(apnsClient, iOSVariant, true);
    }

}
