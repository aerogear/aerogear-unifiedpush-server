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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBContext;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.message.MetricsCollector;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;

@MessageDriven(name = "TriggerMetricsCollectionConsumer", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/TriggerMetricCollectionQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class TriggerMetricCollectionConsumer extends AbstractJMSMessageListener<TriggerMetricCollection> {

    @Inject
    @Dequeue
    private Event<TriggerMetricCollection> dequeueEvent;

    @Resource
    private EJBContext context;

    /**
     * Fires the {@link TriggerMetricCollection} event and checks if its listeners reports that all batches were loaded by {@link MetricsCollector}.
     *
     * If all batches were loaded, the metric collection process ends.
     *
     * If not all batches were loaded, the transaction is rolled back so that this method will be re-triggered based on TriggerMetricCollectionQueue address settings.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(TriggerMetricCollection message) {
        dequeueEvent.fire(message);
        if (!message.areAllVariantsProcessed()) {
            context.setRollbackOnly();
        }
    }
}
