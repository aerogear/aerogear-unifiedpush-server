package org.jboss.aerogear.unifiedpush.message.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.TokenLoader;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@MessageDriven(name = "VariantMetricQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/VariantMetricQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class VariantMetricInformationMDB implements MessageListener {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoader.class);

    @Inject
    @Dequeue
    private Event<VariantMetricInformation> message;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage && ((ObjectMessage) jmsMessage).getObject() instanceof VariantMetricInformation) {
                message.fire((VariantMetricInformation) ((ObjectMessage) jmsMessage).getObject());
            } else {
                logger.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from VariantTypeQueue", e);
        }
    }



}
