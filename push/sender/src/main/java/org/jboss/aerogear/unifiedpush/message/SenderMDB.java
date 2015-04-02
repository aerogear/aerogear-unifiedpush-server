package org.jboss.aerogear.unifiedpush.message;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@MessageDriven(name = "TokenBatchQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/TokenBatchQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class SenderMDB implements MessageListener {

    private final static AeroGearLogger LOGGER = AeroGearLogger.getInstance(SenderMDB.class);

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(javax.jms.Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage) {
                MessageForTokens msg = (MessageForTokens) ((ObjectMessage) jmsMessage).getObject();
                LOGGER.fine("Received message from queue: " + msg.getUnifiedPushMessage().getMessage().getAlert());
            } else {
                LOGGER.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from TokenBatchQueue", e);
        }
    }
}