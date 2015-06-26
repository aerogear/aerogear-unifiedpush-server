package org.jboss.aerogear.unifiedpush.message.jms.util;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException;

public class JMSOperationExecutor {

    public <T> T run(ConnectionFactory connectionFactory, JMSOperation<T> operation) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            return operation.execute(session);
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to execute", e);
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
