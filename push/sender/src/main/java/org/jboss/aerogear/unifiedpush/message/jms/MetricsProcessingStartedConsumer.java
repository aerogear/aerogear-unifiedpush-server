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

import org.jboss.aerogear.unifiedpush.message.event.MetricsProcessingStartedEvent;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@MessageDriven(name = "MetricsProcessingStartedConsumer", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/MetricsProcessingStartedTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MetricsProcessingStartedConsumer extends AbstractJMSMessageListener<MetricsProcessingStartedEvent> {

    @Inject
    @Dequeue
    private Event<MetricsProcessingStartedEvent> dequeueEvent;

    /**
     * Receives MetricsProcessingStartedEvent from the JMS queue and triggers further processing via CDI event.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void onMessage(MetricsProcessingStartedEvent message) {
        dequeueEvent.fire(message);
    }
}
