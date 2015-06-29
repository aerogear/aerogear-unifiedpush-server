package org.jboss.aerogear.unifiedpush.message.jms.util;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageConsumer;

/**
 * Exposes basic JMS operations for simple consumption from the services.
 */
public class JMSExecutor {

    private static final long DEFAULT_WAIT_TIME = AbstractJMSMessageConsumer.DEFAULT_MESSAGE_WAIT;

    @Inject
    private JMSOperationExecutor jmsOperationExecutor;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    /**
     * Sends a message to the queue
     */
    public void send(final Queue pushMessageQueue, final Serializable msg) {
        jmsOperationExecutor.run(connectionFactory, new JMSOperation<Void>() {
            @Override
            public Void execute(Session session) throws JMSException {
                MessageProducer messageProducer = session.createProducer(pushMessageQueue);
                ObjectMessage objectMessage = session.createObjectMessage(msg);
                messageProducer.send(objectMessage);
                return null;
            }
        });
    }

    /**
     * Sends a message to the queue by providing a property (such as "property=value") that receiver can filter messages on using message selector (such as "property='value'").
     *
     * @see #receiveNoWaitWithSelector(Queue, String)
     * @see #receiveOrWaitWithSelector(Queue, String)
     * @see #receiveWithSelectorAndWait(Queue, String, long)
     */
    public void sendWithProperty(final Queue pushMessageQueue, final Serializable msg, final String messageProperty) {
        final String[] messageProperties = messageProperty.split("=");
        jmsOperationExecutor.run(connectionFactory, new JMSOperation<Void>() {
            @Override
            public Void execute(Session session) throws JMSException {
                MessageProducer messageProducer = session.createProducer(pushMessageQueue);
                ObjectMessage objectMessage = session.createObjectMessage(msg);
                objectMessage.setStringProperty(messageProperties[0], messageProperties[1]);
                messageProducer.send(objectMessage);
                return null;
            }
        });
    }

    /**
     * Sends a message to the queue by providing a property (such as "property=value") that receiver can filter messages on using message selector (such as "property='value'")
     */
    public <T extends Serializable> T receiveNoWaitWithSelector(final Queue queue, final String messageSelector) {
        return jmsOperationExecutor.run(connectionFactory, new JMSOperation<T>() {
            @Override
            public T execute(Session session) throws JMSException {
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

    public <T extends Serializable> T receive(final Queue queue) {
        return jmsOperationExecutor.run(connectionFactory, new JMSOperation<T>() {
            @Override
            public T execute(Session session) throws JMSException {
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

    public <T extends Serializable> T receiveWithWait(final Queue queue, final long waitTime) {
        return jmsOperationExecutor.run(connectionFactory, new JMSOperation<T>() {
            @Override
            public T execute(Session session) throws JMSException {
                MessageConsumer messageConsumer = session.createConsumer(queue);
                ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receive(waitTime);
                if (objectMessage != null) {
                    return (T) objectMessage.getObject();
                } else {
                    return null;
                }
            }
        });
    }

    public <T extends Serializable> T receiveWithSelector(final Queue queue, final String messageSelector) {
        return jmsOperationExecutor.run(connectionFactory, new JMSOperation<T>() {
            @Override
            public T execute(Session session) throws JMSException {
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

    public <T extends Serializable> T receiveWithSelectorAndWait(final Queue queue, final String messageSelector, final long waitTime) {
        return jmsOperationExecutor.run(connectionFactory, new JMSOperation<T>() {
            @Override
            public T execute(Session session) throws JMSException {
                MessageConsumer messageConsumer = session.createConsumer(queue, messageSelector);
                ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receive(waitTime);
                if (objectMessage != null) {
                    return (T) objectMessage.getObject();
                } else {
                    return null;
                }
            }
        });
    }

}
