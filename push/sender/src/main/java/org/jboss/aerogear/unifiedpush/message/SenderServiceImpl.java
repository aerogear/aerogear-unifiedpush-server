/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

@Stateless
@Asynchronous
public class SenderServiceImpl implements SenderService {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(SenderServiceImpl.class);

    @Inject
    private GenericVariantService genericVariantService;
    @Inject
    private PushMessageMetricsService metricsService;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/queue/VariantTypeQueue")
    private Queue variantTypeQueue;

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void send(PushApplication pushApplication, UnifiedPushMessage message) {
        logger.info("Processing send request with '" + message.toString() + "' payload");

        final PushMessageInformation pushMessageInformation =
                metricsService.storeNewRequestFrom(
                        pushApplication.getPushApplicationID(),
                        message.toStrippedJsonString(),
                        message.getIpAddress(),
                        message.getClientIdentifier()
                        );

        // collections for all the different variants:
        final VariantMap variants = new VariantMap();

        final List<String> variantIDs = message.getCriteria().getVariants();

        // if the criteria payload did specify the "variants" field,
        // we look up each of those mentioned variants, by their "variantID":
        if (variantIDs != null) {

            for (String variantID : variantIDs) {
                Variant variant = genericVariantService.findByVariantID(variantID);

                // does the variant exist ?
                if (variant != null) {
                    variants.add(variant);
                }
            }
        } else {
            // No specific variants have been requested,
            // we get all the variants, from the given PushApplicationEntity:
            variants.addAll(pushApplication.getVariants());
        }

        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(variantTypeQueue);
            connection.start();

            // we split the variants per type since each type may have its own configuration (e.g. batch size)
            for (final Entry<VariantType, List<Variant>> variant : variants.entrySet()) {
                ObjectMessage messageWithVariants = session.createObjectMessage(new MessageForVariants(message, variant.getValue()));
                messageProducer.send(messageWithVariants);
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
    }

    /**
     * Map for storing variants split by the variant type
     */
    private static class VariantMap extends EnumMap<VariantType, List<Variant>> {
        private static final long serialVersionUID = -1942168038908630961L;
        VariantMap() {
            super(VariantType.class);
        }
        void add(Variant variant) {
            List<Variant> list = this.get(variant.getType());
            if (list == null) {
                list = new ArrayList<Variant>();
                this.put(variant.getType(), list);
            }
            list.add(variant);
        }
        void addAll(Collection<Variant> variants) {
            for (Variant variant : variants) {
                this.add(variant);
            }
        }
    }
}
