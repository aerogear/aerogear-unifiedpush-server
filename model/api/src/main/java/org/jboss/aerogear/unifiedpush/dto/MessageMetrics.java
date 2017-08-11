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
package org.jboss.aerogear.unifiedpush.dto;

/**
 * DTO transferring count together with number of receivers and app opened counter
 */
public class MessageMetrics {

    private final long count;
    private final long appOpenedCounter;

    public MessageMetrics(Long count, Long appOpenedCounter) {
        this.count = count;
        this.appOpenedCounter = (appOpenedCounter == null) ? 0 : appOpenedCounter;
    }

    public long getCount() {
        return count;
    }

    public long getAppOpenedCounter() {
        return appOpenedCounter;
    }

    @Override
    public String toString() {
        return "MessageMetrics [count=" + count + ", appOpenedCounter=" + appOpenedCounter + "]";
    }
}
