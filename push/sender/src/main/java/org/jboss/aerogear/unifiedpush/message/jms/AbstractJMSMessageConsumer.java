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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;

import org.jboss.aerogear.unifiedpush.message.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.TokenLoader;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

/**
 * Abstract base for message driven beans that receives a {@link ObjectMessage} from a queue, validates its type, cast it to a generic type T and pass for processing to abstract method {@link #onMessage(T)}
 */
abstract class AbstractJMSMessageConsumer<T> implements MessageListener {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoader.class);

    public abstract void onMessage(T message);

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message jmsMessage) {
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
                logger.warning("Received message of wrong type " + jmsMessage.getClass().getName() + " to destination " + getDestinationName(jmsMessage));
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from destination " + getDestinationName(jmsMessage), e);
        }
    }

    private String getDestinationName(Message message) {
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
