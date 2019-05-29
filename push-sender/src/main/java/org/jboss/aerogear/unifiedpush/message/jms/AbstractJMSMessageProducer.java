/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.jms;

import org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;

/**
 * Simplifies sending of messages to a destination
 */
public abstract class AbstractJMSMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJMSMessageProducer.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/jms/remoteXA")
    private ConnectionFactory xaConnectionFactory;

     /**
     * Sends message to the destination in non-transactional manner.
     *
     * @param destination where to send
     * @param message what to send
     *
     * Since non-transacted session is used, the message is send immediately without requiring to commit enclosing transaction.
     */
    protected void sendNonTransacted(String destination, Serializable message, boolean useTopic) {
        send(destination, message, null, null, false, useTopic);
    }

    /**
     * Sends message to the destination in transactional manner.
     *
     * @param destination where to send
     * @param message what to send
     *
     * Since transacted session is used, the message won't be committed until whole enclosing transaction ends
     */
    protected void sendTransacted(String destination, Serializable message, boolean useTopic) {
        send(destination, message, null, null, true, useTopic);
    }

    /**
     * Sends message to destination with given JMS message property name and value in non-transactional manner.
     *
     * @param destination where to send
     * @param message what to send
     * @param propertyName property of obj
     * @param propertValue value of obj
     *
     * Since non-transacted session is used, the message is send immediately without requiring to commit enclosing transaction.
     */
    protected void sendNonTransacted(String destination, Serializable message, String propertyName, String propertValue, boolean useTopic) {
        send(destination, message, propertyName, propertValue, false, useTopic);
    }

    /**
     * Sends message to destination with given JMS message property name and value in transactional manner.
     *
     * @param destination where to send
     * @param message what to send
     * @param propertyName property of obj
     * @param propertValue value of obj
     *
     * Since transacted session is used, the message won't be committed until whole enclosing transaction ends.
     */
    protected void sendTransacted(String destination, Serializable message, String propertyName, String propertValue, boolean useTopic) {
        send(destination, message, propertyName, propertValue, true, useTopic);
    }

    private void send(String destination, Serializable message, String propertyName, String propertValue, boolean transacted, boolean useTopic) {
        Connection connection = null;
        try {
            if (transacted) {
                connection = xaConnectionFactory.createConnection();
            } else {
                connection = connectionFactory.createConnection();
            }
            Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            final MessageProducer messageProducer;
            if (useTopic) {
                messageProducer = session.createProducer(session.createTopic(destination));
            } else {
                messageProducer = session.createProducer(session.createQueue(destination));
            }
            logger.debug("Destination is {}", destination);
            connection.start();
            ObjectMessage objectMessage = session.createObjectMessage(message);
            if (propertyName != null) {
                objectMessage.setStringProperty(propertyName, propertValue);
            }
            messageProducer.send(objectMessage);
            logger.debug("Sending complete");
        } catch (JMSException e) {
            logger.error("Error sending", e.getMessage(), e);
            throw new MessageDeliveryException("Failed to queue push message for further processing", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    logger.error("Failed to close JMS connection: ", e);
                }
            }
        }
    }
}
