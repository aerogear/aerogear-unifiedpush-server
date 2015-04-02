package org.jboss.aerogear.unifiedpush.message;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.BatchException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@MessageDriven(name = "VariantTypeQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/VariantTypeQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class TokenLoaderMDB implements MessageListener {

    private final static int BATCH_SIZE = 1000;

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoaderMDB.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/queue/TokenBatchQueue")
    private Queue batchQueue;

    @Inject
    private ClientInstallationService clientInstallationService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(javax.jms.Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage) {
                final MessageForVariants msgPayload = (MessageForVariants) ((ObjectMessage) jmsMessage).getObject();
                final UnifiedPushMessage message = msgPayload.getUnifiedPushMessage();
                final List<Variant> variants = msgPayload.getVariants();
                logger.fine("Received message from queue: " + message.getMessage().getAlert());

                Connection connection = null;
                try {
                    connection = connectionFactory.createConnection();
                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    MessageProducer messageProducer = session.createProducer(batchQueue);
                    connection.start();

                    final Criteria criteria = message.getCriteria();
                    final List<String> categories = criteria.getCategories();
                    final List<String> aliases = criteria.getAliases();
                    final List<String> deviceTypes = criteria.getDeviceTypes();

                    for (Variant variant : variants) {
                        ResultsStream<String> tokenStream = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes)
                            .fetchSize(BATCH_SIZE).executeQuery();

                        try {
                            ArrayList<String> tokens = new ArrayList<String>(BATCH_SIZE);
                            for (int i = 0; i < BATCH_SIZE && tokenStream.next(); i++) {
                                tokens.add(tokenStream.get());
                            }
                            ObjectMessage messageWithTokens = session.createObjectMessage(new MessageForTokens(message, variant, tokens));
                            messageProducer.send(messageWithTokens);
                        } catch (BatchException e) {
                            logger.severe("Failed to load batch of tokens", e);
                        }
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
            } else {
                logger.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from VariantTypeQueue", e);
        }
    }
}