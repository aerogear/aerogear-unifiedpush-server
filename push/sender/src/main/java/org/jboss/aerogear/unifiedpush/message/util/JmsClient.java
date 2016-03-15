/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.util;

import org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for sending and receiving JMS messages
 */
@Stateless
public class JmsClient {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory xaConnectionFactory;

    /**
     * Creates {@link JmsSender} utility that allows to specify how should be message sent and into which destination
     *
     * @param message msg to be send out
     * @return the sender
     */
    public JmsSender send(Serializable message) {
        return new JmsSender(message);
    }

    /**
     * Creates {@link JmsReceiver} utility that allows to specify how should be message received and from which destination
     *
     * @return the sender
     */
    public JmsReceiver receive() {
        return new JmsReceiver();
    }

    /**
     * Utility that allows to specify how should be message sent and into which destination
     */
    public class JmsReceiver {

        private boolean transacted = false;
        private String selector = null;
        private Wait wait = new WaitIndefinitely();
        private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
        private boolean autoClose = true;

        private Connection connection;

        public JmsReceiver() {
        }

        /**
         * Receives the message in transaction (i.e. use JmsXA connection factory).
         *
         * @return the receiver
         */
        public JmsReceiver inTransaction() {
            this.transacted = true;
            return this;
        }

        /**
         * Specifies a selector used to query messages from a destination.
         * The selector can be formatted with arguments as in {@link String#format(String, Object...)}.
         *
         * @param selector specifies to query messages from a destination.
         * @param args argument for the selector
         * @return the receiver
         *
         * @see String#format(String, Object...)
         */
        public JmsReceiver withSelector(String selector, Object... args) {
            this.selector = String.format(selector, args);
            return this;
        }

        /**
         * Don't block and returns the message what is in the queue, if there is none queued, then returns null immediately.
         *
         * @return the receiver
         */
        public JmsReceiver noWait() {
            this.wait = new NoWait();
            return this;
        }

        /**
         * Waits specific number of milliseconds for a message to eventually appear in the queue, or returns null if there was no message queued in given interval.
         *
         * @param timeout wait until
         * @return the receiver
         */
        public JmsReceiver withTimeout(long timeout) {
            this.wait = new WaitSpecificTime(timeout);
            return this;
        }

        /**
         * Sets the message acknowledgement mode.
         *
         * @param acknowledgeMode
         * @return
         */
        public JmsReceiver withAcknowledgeMode(int acknowledgeMode) {
            this.acknowledgeMode = acknowledgeMode;
            return this;
        }

        /**
         * Won't close the connection automatically upon completion, allowing to reuse given connection.
         *
         * @return the receiver
         */
        public JmsReceiver noAutoClose() {
            this.autoClose = false;
            return this;
        }

        /**
         * Closes the connection.
         */
        public void close() {
            try {
                connection.close();
            } catch (JMSException e) {
                throw new java.lang.IllegalStateException(e);
            }
        }

        /**
         * Receives message from the given destination.
         *
         * @param destination where to receive from
         * @return JMS object
         */
        public ObjectMessage from(Destination destination) {
            try {
                if (transacted) {
                    connection = xaConnectionFactory.createConnection();
                } else {
                    connection = connectionFactory.createConnection();
                }
                Session session = connection.createSession(transacted, acknowledgeMode);
                MessageConsumer messageConsumer;
                if (selector != null) {
                    messageConsumer = session.createConsumer(destination, selector);
                } else {
                    messageConsumer = session.createConsumer(destination);
                }
                connection.start();
                ObjectMessage objectMessage;
                if (wait instanceof WaitIndefinitely) {
                    objectMessage = (ObjectMessage) messageConsumer.receive();
                } else if (wait instanceof NoWait) {
                    objectMessage = (ObjectMessage) messageConsumer.receiveNoWait();
                } else if (wait instanceof WaitSpecificTime) {
                    objectMessage = (ObjectMessage) messageConsumer.receive(((WaitSpecificTime) wait).getTime());
                } else {
                    throw new IllegalStateException("Unknown wait: " + wait.getClass());
                }
                return objectMessage;
            } catch (JMSException e) {
                throw new MessageDeliveryException("Failed to queue push message for further processing", e);
            } finally {
                if (connection != null && autoClose) {
                    try {
                        connection.close();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Utility that allows to specify how should be message received and from which destination
     */
    public class JmsSender {

        private Serializable message;
        private boolean transacted = false;
        private Map<String, String> properties = new LinkedHashMap<String, String>();
        private int autoAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;

        public JmsSender(Serializable message) {
            this.message = message;
        }

        /**
         * Send the message in transaction (i.e. use JmsXA connection factory).
         *
         * @return the sender
         */
        public JmsSender inTransaction() {
            this.transacted = true;
            return this;
        }

        /**
         * Sets the property that can be later used to query message by selector.
         *
         * @param name of property
         * @param value of propery
         * @return the sender
         */
        public JmsSender withProperty(String name, String value) {
            this.properties.put(name, value);
            return this;
        }

        /**
         * The message sent with given ID will be delivered exactly once.
         *
         * Any other try to sent another message with exactly same ID won't result into queing the message,
         * no matter what payload the another message has.
         *
         * @param duplicateDetectionId protection of ID for duplicate msgs
         * @return the sender
         */
        public JmsSender withDuplicateDetectionId(String duplicateDetectionId) {
            this.properties.put("_HQ_DUPL_ID", duplicateDetectionId);
            return this;
        }

        /**
         * Sends the message to the destination.
         *
         * @param destination where to send
         */
        public void to(Destination destination) {
            Connection connection = null;
            try {
                if (transacted) {
                    connection = xaConnectionFactory.createConnection();
                } else {
                    connection = connectionFactory.createConnection();
                }
                Session session = connection.createSession(transacted, autoAcknowledgeMode);
                MessageProducer messageProducer = session.createProducer(destination);
                connection.start();
                ObjectMessage objectMessage = session.createObjectMessage(message);
                for (Entry<String, String> property : properties.entrySet()) {
                    objectMessage.setStringProperty(property.getKey(), property.getValue());
                }
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
    }

    private interface Wait {
    }

    private static class WaitIndefinitely implements Wait {
    }

    private static class NoWait implements Wait {
    }

    private static class WaitSpecificTime implements Wait {
        private long time;

        public WaitSpecificTime(long time) {
            super();
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }

}
