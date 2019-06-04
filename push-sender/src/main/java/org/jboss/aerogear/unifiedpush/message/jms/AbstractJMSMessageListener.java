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

import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionAttribute;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;

import org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for message driven beans that receives a {@link javax.jms.ObjectMessage} from a queue, validates its type, cast it to a generic type T and pass for processing to abstract method {@link #onMessage(Object)}
 */
public abstract class AbstractJMSMessageListener<T> implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJMSMessageListener.class);

    public abstract void onMessage(T message);

    @SuppressWarnings("unchecked")
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message jmsMessage) {
        logger.debug("message get {}", jmsMessage.toString());
        try {
            if (jmsMessage instanceof ObjectMessage) {
                Object messageObject = ((ObjectMessage) jmsMessage).getObject();
                try {
                    T message = (T) messageObject;
                    onMessage(message);
                } catch (ClassCastException e) {
                    throw new IllegalStateException("Received message of wrong payload type " + messageObject.getClass() + " to destination " + getDestinationName(jmsMessage));
                }
            } else {
                logger.warn("Received message of wrong type {} to destination {}",
                        jmsMessage.getClass().getName(), getDestinationName(jmsMessage));
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from destination " + getDestinationName(jmsMessage), e);
        }
    }

    private static String getDestinationName(Message message) {
        try {
            Destination destination = message.getJMSDestination();
            if (destination instanceof Queue) {
                return ((Queue) destination).getQueueName();
            } else if (destination instanceof Topic) {
                return ((Topic) destination).getTopicName();
            } else {
                throw new IllegalStateException("Can't recognize destination type for " + destination.getClass());
            }
        } catch (JMSException e) {
            throw new IllegalStateException("Can't extract destination name from JMS message: " + message, e);
        }
    }
}
