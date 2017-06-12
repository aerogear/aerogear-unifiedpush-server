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

import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.exception.DispatchInitiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Consumes {@link VariantMetricInformation} from queue and pass them as a CDI event for further processing.
 *
 * This class serves as mediator for decoupling of JMS subsystem and services that observes these messages.
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VariantMetricInformationConsumer extends AbstractJMSMessageListener<VariantMetricInformation> {

    private final Logger logger = LoggerFactory.getLogger(VariantMetricInformationConsumer.class);

    @Inject
    @Dequeue
    private Event<VariantMetricInformation> dequeueEvent;

    @Override
    public void onMessage(VariantMetricInformation vmi) {
        try {
            logger.error("Messsage => "+ vmi.getDeliveryStatus());
            logger.error("Messsage => "+ vmi.getReason());
            dequeueEvent.fire(vmi);
        } catch (DispatchInitiationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("NotificationDispatcher or PushNotificationSender unexpectedly failed, the message won't be redelivered", e);
        }
    }
}
