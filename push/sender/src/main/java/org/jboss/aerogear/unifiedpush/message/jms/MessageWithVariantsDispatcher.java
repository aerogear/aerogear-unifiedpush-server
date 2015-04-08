package org.jboss.aerogear.unifiedpush.message.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.MessageWithVariants;

@Stateless
public class MessageWithVariantsDispatcher {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/queue/AdmPushMessageQueue")
    private Queue admPushMessageQueue;

    @Resource(mappedName = "java:/queue/APNsPushMessageQueue")
    private Queue apnsPushMessageQueue;

    @Resource(mappedName = "java:/queue/GCMPushMessageQueue")
    private Queue gcmPushMessageQueue;

    @Resource(mappedName = "java:/queue/MPNSPushMessageQueue")
    private Queue mpnsPushMessageQueue;

    @Resource(mappedName = "java:/queue/SimplePushMessageQueue")
    private Queue simplePushMessageQueue;

    @Resource(mappedName = "java:/queue/WNSPushMessageQueue")
    private Queue wnsPushMessageQueue;

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue MessageWithVariants msg) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue pushMessageQueue = selectQueue(msg.getVariantType());
            MessageProducer messageProducer = session.createProducer(pushMessageQueue);
            connection.start();
            messageProducer.send(session.createObjectMessage(msg));
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to queue push message for further processing", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Queue selectQueue(VariantType variantType) {
        switch (variantType) {
            case ADM:
                return admPushMessageQueue;
            case ANDROID:
                return gcmPushMessageQueue;
            case IOS:
                return apnsPushMessageQueue;
            case SIMPLE_PUSH:
                return simplePushMessageQueue;
            case WINDOWS_MPNS:
                return mpnsPushMessageQueue;
            case WINDOWS_WNS:
                return wnsPushMessageQueue;
            default:
                throw new IllegalStateException("Unknown variant type queue");
        }
    }

}
