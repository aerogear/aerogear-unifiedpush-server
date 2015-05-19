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

import org.jboss.aerogear.unifiedpush.message.event.AllBatchesLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;

/**
 * A CDI-to-JMS bridge takes some selected CDI events and passes them to JMS messaging system so that they can be handled asynchronously.
 */
public class CdiJmsBridge extends AbstractJMSMessageProducer {

    @Resource(mappedName = "java:/queue/AllBatchesLoadedQueue")
    private Queue allBatchesLoaded;

    @Resource(mappedName = "java:/queue/BatchLoadedQueue")
    private Queue batchLoadedQueue;

    public void queueMessage(@Observes @DispatchToQueue AllBatchesLoadedEvent msg) {
        sendTransacted(allBatchesLoaded, msg, "variantID", msg.getVariantID());
    }

    public void queueMessage(@Observes @DispatchToQueue BatchLoadedEvent msg) {
        sendTransacted(batchLoadedQueue, msg, "variantID", msg.getVariantID());
    }
}
