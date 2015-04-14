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
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;

@Stateless
public class MessageHolderWithTokensProducer {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/queue/AdmTokenBatchQueue")
    private Queue admTokenBatchQueue;

    @Resource(mappedName = "java:/queue/APNsTokenBatchQueue")
    private Queue apnsTokenBatchQueue;

    @Resource(mappedName = "java:/queue/GCMTokenBatchQueue")
    private Queue gcmTokenBatchQueue;

    @Resource(mappedName = "java:/queue/MPNSTokenBatchQueue")
    private Queue mpnsTokenBatchQueue;

    @Resource(mappedName = "java:/queue/SimplePushTokenBatchQueue")
    private Queue simplePushTokenBatchQueue;

    @Resource(mappedName = "java:/queue/WNSTokenBatchQueue")
    private Queue wnsTokenBatchQueue;

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue MessageHolderWithTokens msg) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue tokenBatchQueue = selectQueue(msg.getVariant().getType());
            MessageProducer messageProducer = session.createProducer(tokenBatchQueue);
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
                return admTokenBatchQueue;
            case ANDROID:
                return gcmTokenBatchQueue;
            case IOS:
                return apnsTokenBatchQueue;
            case SIMPLE_PUSH:
                return simplePushTokenBatchQueue;
            case WINDOWS_MPNS:
                return mpnsTokenBatchQueue;
            case WINDOWS_WNS:
                return wnsTokenBatchQueue;
            default:
                throw new IllegalStateException("Unknown variant type queue");
        }
    }

}
