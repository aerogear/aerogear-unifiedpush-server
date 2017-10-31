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
package org.jboss.aerogear.unifiedpush.message.event;

import java.io.Serializable;

/**
 * Event fired when all batches for given variant were loaded and queued.
 *
 * Note: this does not mean all batches are processed.
 *
 * @see BatchLoadedEvent
 */
public class AllBatchesLoadedEvent implements Serializable {

    private static final long serialVersionUID = 3259364604967570821L;

    private String variantID;

    public AllBatchesLoadedEvent(String variantID) {
        this.variantID = variantID;
    }

    public String getVariantID() {
        return variantID;
    }
}
