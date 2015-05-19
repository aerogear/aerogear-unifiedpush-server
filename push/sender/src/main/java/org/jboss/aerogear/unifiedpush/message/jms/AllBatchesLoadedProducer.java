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
import javax.enterprise.event.Observes;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.TokenLoader;
import org.jboss.aerogear.unifiedpush.message.holder.AllBatchesLoaded;

/**
 * Receives message from {@link TokenLoader} and queues it under the variantID as a JMS message property in order to allow
 * consumers simply receiving the message for given variant
 */
public class AllBatchesLoadedProducer extends AbstractJMSMessageProducer {

    @Resource(mappedName = "java:/queue/AllBatchesLoadedQueue")
    private Queue allBatchesLoaded;

    public void queueMessage(@Observes @DispatchToQueue AllBatchesLoaded msg) {
        sendNonTransacted(allBatchesLoaded, msg, "variantID", msg.getVariantID());
    }
}
