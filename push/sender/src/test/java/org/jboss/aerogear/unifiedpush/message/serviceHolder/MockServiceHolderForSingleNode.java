/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.serviceHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.serviceHolder.AbstractServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockServiceHolderForSingleNode extends AbstractServiceHolder<Integer> {

    private static final int INSTANCE_LIMIT = 5;
    private static final long INSTANTIATION_TIMEOUT = 200;
    private static final long DISPOSAL_DELAY = 1000;

    private AtomicInteger counter = new AtomicInteger(0);

    private Logger log = LoggerFactory.getLogger(MockServiceHolderForSingleNode.class);

    @Resource(mappedName = "java:/queue/FreeServiceSlotQueue")
    private Queue queue;

    public MockServiceHolderForSingleNode() {
        super(INSTANCE_LIMIT, INSTANTIATION_TIMEOUT, DISPOSAL_DELAY);
    }

    @Override
    public Queue getFreeServiceSlotQueue() {
        return queue;
    }

    @Override
    public void initialize(String pushMessageInformationId, String variantID) {
        assertEquals("Counter has to be zero before initialize", 0, counter.get());
        super.initialize(pushMessageInformationId, variantID);
        counter.set(INSTANCE_LIMIT);
        log.debug("initialized: " + counter);
    }

    @Override
    public void destroy(String pushMessageInformationId, String variantID) {
        super.destroy(pushMessageInformationId, variantID);
        log.debug("destroyed: " + counter);
        assertEquals("Counter has to be zero after destroy", 0, counter.get());
    }

    @Override
    protected Object borrowServiceSlotFromQueue(String pushMessageInformationId, String variantID) {
        Object serviceSlot = super.borrowServiceSlotFromQueue(pushMessageInformationId, variantID);
        if (serviceSlot != null) {
            counter.decrementAndGet();
            log.debug(counter.toString());
            assertTrue("Instance count can't be never lesser than zero", counter.get() >= 0);
        }
        return serviceSlot;
    }

    @Override
    protected void returnServiceSlotToQueue(String pushMessageInformationId, String variantID) {
        counter.incrementAndGet();
        log.debug(counter.toString());
        assertTrue("Instance count can't be never greater than limit", counter.get() <= INSTANCE_LIMIT);
        super.returnServiceSlotToQueue(pushMessageInformationId, variantID);
    }
}