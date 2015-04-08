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

import org.jboss.aerogear.unifiedpush.message.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.MessageWithTokens;

@Stateless
public class MessageWithTokensDispatcher {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/queue/TokenBatchQueue")
    private Queue tokenBatchQueue;

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue MessageWithTokens msg) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
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

}
