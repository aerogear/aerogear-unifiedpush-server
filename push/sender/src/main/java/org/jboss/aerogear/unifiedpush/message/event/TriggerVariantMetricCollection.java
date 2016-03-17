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
package org.jboss.aerogear.unifiedpush.message.event;

import java.io.Serializable;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.MetricsCollector;

/**
 * Event that triggers {@link MetricsCollector} processing.
 *
 * @see {@link org.jboss.aerogear.unifiedpush.message.jms.TriggerMetricCollectionConsumer}
 */
public class TriggerVariantMetricCollection implements Serializable {

    private static final long serialVersionUID = 1036025116554796512L;

    public static final long REDELIVERY_DELAY_MS = 1000L;

    private String pushMessageInformationId;
    private String variantID;

    public TriggerVariantMetricCollection(PushMessageInformation pushMessageInformation, Variant variant) {
        this.pushMessageInformationId = pushMessageInformation.getId();
        this.variantID = variant.getVariantID();
    }

    public TriggerVariantMetricCollection(String pushMessageInformationId, String variantID) {
        this.pushMessageInformationId = pushMessageInformationId;
        this.variantID = variantID;
    }

    public String getPushMessageInformationId() {
        return pushMessageInformationId;
    }

    public String getVariantID() {
        return variantID;
    }

}
