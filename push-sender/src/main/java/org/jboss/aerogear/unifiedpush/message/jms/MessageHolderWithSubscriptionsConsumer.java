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

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.message.exception.DispatchInitiationException;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithSubscriptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumes {@link MessageHolderWithSubscriptions} from queue and pass them as a CDI event for further processing.
 *
 * This class serves as mediator for decoupling of JMS subsystem and services that observes these messages.
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MessageHolderWithSubscriptionsConsumer extends AbstractJMSMessageListener<MessageHolderWithSubscriptions> {

    private static final Logger logger = LoggerFactory.getLogger(MessageHolderWithSubscriptionsConsumer.class);

    @Inject
    @Dequeue
    private Event<MessageHolderWithSubscriptions> dequeueEvent;

    @Override
    public void onMessage(MessageHolderWithSubscriptions message) {
        try {
            logger.trace("receiving subscriptions from queue, triggering Notification Dispatcher class to pick the right sender");
            dequeueEvent.fire(message);
        } catch (DispatchInitiationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("NotificationDispatcher or PushNotificationSender unexpectedly failed, the message won't be redelivered", e);
        }
    }
}
