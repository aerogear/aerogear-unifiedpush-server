package org.jboss.aerogear.unifiedpush.message;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@MessageDriven(name = "VariantTypeQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/VariantTypeQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class VariantTypeProcessorMDB implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(VariantTypeProcessorMDB.class.toString());

    public void onMessage(javax.jms.Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage) {
                MessageForVariants msg = (MessageForVariants) ((ObjectMessage) jmsMessage).getObject();
                LOGGER.info("Received message from queue: " + msg.getUnifiedPushMessage().getMessage().getAlert());
            } else {
                LOGGER.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from VariantTypeQueue", e);
        }
    }
}