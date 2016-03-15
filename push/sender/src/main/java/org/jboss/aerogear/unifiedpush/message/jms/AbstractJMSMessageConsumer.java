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

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import java.io.Serializable;

/**
 * Allows its implementations to simply receive messages from JMS queues in non-blocking way
 */
public abstract class AbstractJMSMessageConsumer {

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory xaConnectionFactory;

    /**
     * Allows to receive message from queue in non-blocking way
     * @param queue the queue to read from
     * @param <T> given type
     *
     * @return message from given queue or null if there is no message in the given queue
     */
    protected <T extends Serializable> T receiveInTransactionNoWait(Queue queue) {
        return receiveInTransactionNoWait(queue, null, null);
    }

    /**
     * Allows to receive selected message from queue in non-blocking way. Message is selected by given JMS message property name and value.
     *
     * @param queue the queue to read from
     * @param propertyName field we are interested in
     * @param propertyValue value of interest
     * @param <T> given type
     *
     * @return message from given queue or null if there is no message in the given queue for given property name and value
     */
    protected <T extends Serializable> T receiveInTransactionNoWait(Queue queue, String propertyName, String propertyValue) {
        Connection connection = null;
        try {
            connection = xaConnectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer messageConsumer;
            if (propertyName != null) {
                messageConsumer = session.createConsumer(queue, String.format("%s = '%s'", propertyName, propertyValue));
            } else {
                messageConsumer = session.createConsumer(queue);
            }
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
