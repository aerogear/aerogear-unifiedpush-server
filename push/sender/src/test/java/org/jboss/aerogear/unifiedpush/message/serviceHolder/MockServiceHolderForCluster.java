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

import javax.annotation.Resource;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.serviceHolder.AbstractServiceHolder;

public class MockServiceHolderForCluster extends AbstractServiceHolder<Integer> {

    private static final int INSTANCE_LIMIT = 5;
    private static final long INSTANTIATION_TIMEOUT = 7500;
    private static final long DISPOSAL_DELAY = 5000;

    @Resource(mappedName = "java:/queue/FreeServiceSlotQueue")
    private Queue queue;

    public MockServiceHolderForCluster() {
        super(INSTANCE_LIMIT, INSTANTIATION_TIMEOUT, DISPOSAL_DELAY);
    }

    @Override
    public Queue getFreeServiceSlotQueue() {
        return queue;
    }
}