package org.jboss.aerogear.unifiedpush.message;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException;

public abstract class AbstractJMSTest {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    protected void send(Queue pushMessageQueue, Serializable msg, String variantID) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(pushMessageQueue);
            connection.start();
            ObjectMessage objectMessage = session.createObjectMessage(msg);
            objectMessage.setStringProperty("variantID", variantID);
            messageProducer.send(objectMessage);
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

    protected <T extends Serializable> T receive(Queue queue, String variantID) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer messageConsumer = session.createConsumer(queue, String.format("variantID = '%s'", variantID));
            connection.start();
            ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receiveNoWait();
            if (objectMessage != null) {
                return (T) objectMessage.getObject();
            } else {
                return null;
            }
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
