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
import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageConsumer;

public abstract class AbstractJMSTest {

    private static final long DEFAULT_WAIT_TIME = AbstractJMSMessageConsumer.DEFAULT_MESSAGE_WAIT;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory xaConnectionFactory;

    protected void send(final Queue pushMessageQueue, final Serializable msg) {
        connectAndRun(connectionFactory, new Runnable<Void>() {
            @Override
            public Void run(Session session) throws JMSException {
                MessageProducer messageProducer = session.createProducer(pushMessageQueue);
                ObjectMessage objectMessage = session.createObjectMessage(msg);
                messageProducer.send(objectMessage);
                return null;
            }
        });
    }

    protected void send(final Queue pushMessageQueue, final Serializable msg, final String messageProperty) {
        final String[] messageProperties = messageProperty.split("=");
        connectAndRun(connectionFactory, new Runnable<Void>() {
            @Override
            public Void run(Session session) throws JMSException {
                MessageProducer messageProducer = session.createProducer(pushMessageQueue);
                ObjectMessage objectMessage = session.createObjectMessage(msg);
                objectMessage.setStringProperty(messageProperties[0], messageProperties[1]);
                messageProducer.send(objectMessage);
                return null;
            }
        });
    }

    protected <T extends Serializable> T receiveNoWait(final Queue queue, final String messageSelector) {
        return connectAndRun(connectionFactory, new Runnable<T>() {
            @Override
            public T run(Session session) throws JMSException {
                MessageConsumer messageConsumer = session.createConsumer(queue, messageSelector);
                ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receiveNoWait();
                if (objectMessage != null) {
                    return (T) objectMessage.getObject();
                } else {
                    return null;
                }
            }
        });
    }

    protected <T extends Serializable> T receive(final Queue queue) {
        return connectAndRun(connectionFactory, new Runnable<T>() {
            @Override
            public T run(Session session) throws JMSException {
                MessageConsumer messageConsumer = session.createConsumer(queue);
                ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receive(DEFAULT_WAIT_TIME);
                if (objectMessage != null) {
                    return (T) objectMessage.getObject();
                } else {
                    return null;
                }
            }
        });
    }

    protected <T extends Serializable> T receive(final Queue queue, final String messageSelector) {
        return connectAndRun(connectionFactory, new Runnable<T>() {
            @Override
            public T run(Session session) throws JMSException {
                MessageConsumer messageConsumer = session.createConsumer(queue, messageSelector);
                ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receive(DEFAULT_WAIT_TIME);
                if (objectMessage != null) {
                    return (T) objectMessage.getObject();
                } else {
                    return null;
                }
            }
        });
    }

    private <T> T connectAndRun(ConnectionFactory connectionFactory, Runnable<T> runnable) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            return runnable.run(session);
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

    private static interface Runnable<T> {
        T run(Session session) throws JMSException;
    }

}
