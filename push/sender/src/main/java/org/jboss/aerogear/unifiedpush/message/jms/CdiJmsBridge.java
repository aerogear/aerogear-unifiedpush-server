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

import org.jboss.aerogear.unifiedpush.message.event.AllBatchesLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;

import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Queue;

/**
 * A CDI-to-JMS bridge takes selected CDI events with {@link DispatchToQueue} stereotype and passes them to JMS queue or topic so that they can be handled asynchronously.
 */
public class CdiJmsBridge {

    @Resource(mappedName = "java:/queue/AllBatchesLoadedQueue")
    private Queue allBatchesLoadedQueue;

    @Resource(mappedName = "java:/queue/BatchLoadedQueue")
    private Queue batchLoadedQueue;

    @Inject
    private JmsClient jmsClient;

    /**
     * Listens to {@link AllBatchesLoadedEvent} event and passes it to JMS queue /queue/AllBatchesLoadedQueue with variantID as a correlation identifier.
     *
     * @param event indicates that all batches for given variant were loaded
     */
    public void queueMessage(@Observes @DispatchToQueue AllBatchesLoadedEvent event) {
        jmsClient.send(event)
            .inTransaction()
            .withProperty("variantID", event.getVariantID())
            .to(allBatchesLoadedQueue);
    }

    /**
     * Listens to {@link BatchLoadedEvent} event and passes it to JMS queue /queue/BatchLoadedQueue with variantID as a correlation identifier.
     *
     * @param event indicates that batch of tokens was loaded
     */
    public void queueMessage(@Observes @DispatchToQueue BatchLoadedEvent event) {
        jmsClient.send(event)
            .inTransaction()
            .withProperty("variantID", event.getVariantID())
            .to(batchLoadedQueue);
    }

}
