/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.jms;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;

import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.util.QueueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives CDI event with {@link MessageHolderWithVariants} payload and dispatches this payload to JMS queue selected by a type of the variant specified in payload.
 *
 * This bean serves as mediator for decoupling of JMS subsystem and services that triggers these messages.
 */
@Stateless
public class MessageHolderWithVariantsProducer extends AbstractJMSMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(MessageHolderWithVariantsProducer.class);

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue MessageHolderWithVariants msg) {
        logger.trace("dispatching for processing variants and trigger token querying/batching");
        sendTransacted(QueueUtils.selectPushQueue(msg.getVariantType()), msg, false);
    }

}
