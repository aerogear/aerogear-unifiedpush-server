package org.jboss.aerogear.unifiedpush.message.jms;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.aerogear.unifiedpush.message.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.TokenLoader;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

public class MessageHolderWithVariantsConsumer implements MessageListener {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoader.class);

    @Inject
    @Dequeue
    private Event<MessageHolderWithVariants> message;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage && ((ObjectMessage) jmsMessage).getObject() instanceof MessageHolderWithVariants) {
                message.fire((MessageHolderWithVariants) ((ObjectMessage) jmsMessage).getObject());
            } else {
                logger.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from VariantTypeQueue", e);
        }
    }
}
