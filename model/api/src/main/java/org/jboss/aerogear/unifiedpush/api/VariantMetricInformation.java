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
package org.jboss.aerogear.unifiedpush.api;

import javax.validation.constraints.NotNull;

/**
 * Contains metadata about a variant, of the push message request, such as successful delivery to the push network
 * or involved client devices.
 */
public class VariantMetricInformation extends BaseModel {

    @NotNull
    private String variantID;
    private long receivers;
    private Boolean deliveryStatus = Boolean.FALSE;
    private String reason;

    private PushMessageInformation pushMessageInformation;

    public VariantMetricInformation() {
    }

    /**
     * The ID of the involved variant
     */
    public String getVariantID() {
        return variantID;
    }

    public void setVariantID(String variantID) {
        this.variantID = variantID;
    }

    /**
     * Number of receives for this variant that were found for the submitted push request
     */
    public long getReceivers() {
        return receivers;
    }

    public void setReceivers(long receivers) {
        this.receivers = receivers;
    }

    /**
     * Indicator if the request to the actual push network, for the related variant, was successful or not.
     */
    public Boolean getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(Boolean deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * In case of an error (deliveryStatus:false), there is most likely a reason which may give some more insights.
     */
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public PushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public void setPushMessageInformation(PushMessageInformation pushMessageInformation) {
        this.pushMessageInformation = pushMessageInformation;
    }
}
