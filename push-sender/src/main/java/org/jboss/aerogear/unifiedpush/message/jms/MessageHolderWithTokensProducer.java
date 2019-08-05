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

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.message.util.QueueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives CDI event with {@link MessageHolderWithTokens} payload and dispatches this payload to JMS queue selected by a type of the variant specified in payload.
 *
 * This bean serves as mediator for decoupling of JMS subsystem and services that triggers these messages.
 */
public class MessageHolderWithTokensProducer extends AbstractJMSMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(MessageHolderWithTokensProducer.class);

    @Inject
    private JmsClient jmsClient;

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue MessageHolderWithTokens msg) {
        final VariantType variantType = msg.getVariant().getType();
        logger.trace("dispatching payload for {} variant type", variantType);
        final String deduplicationId = String.format("%s-%s", msg.getPushMessageInformation().getId(), msg.getSerialId());
        jmsClient.send(msg).withDuplicateDetectionId(deduplicationId).to(QueueUtils.selectTokenQueue(variantType));
    }

}
