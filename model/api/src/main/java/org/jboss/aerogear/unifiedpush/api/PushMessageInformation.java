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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Metadata object that contains various informations around a submitted push message request
 */
public class PushMessageInformation extends BaseModel {

    private static final long serialVersionUID = -3855047068913784279L;

    @NotNull
    private String pushApplicationId;

    private String rawJsonMessage;
    private String ipAddress;
    private String clientIdentifier;

    private Date submitDate = new Date();
    private long totalReceivers;

    private long appOpenCounter;
    private Date firstOpenDate;
    private Date lastOpenDate;

    private int servedVariants;
    private Integer totalVariants = 0;

    private Set<VariantMetricInformation> variantInformations = new HashSet<VariantMetricInformation>();

    /**
     * The raw JSON payload of the push message request
     *
     * @return raw json
     */
    public String getRawJsonMessage() {
        return rawJsonMessage;
    }

    public void setRawJsonMessage(String rawJsonMessage) {
        this.rawJsonMessage = rawJsonMessage;
    }

    /**
     * The IP from the submitter of the push message request
     *
     * @return the ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The date when the push message request has been processed on the UPS
     *
     * @return timestamp
     */
    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    /**
     * Collection of specific infos around the related variants
     *
     * @return variant information
     */
    public Set<VariantMetricInformation> getVariantInformations() {
        return variantInformations;
    }

    public void setVariantInformations(Set<VariantMetricInformation> variantInformations) {
        this.variantInformations = variantInformations;
    }

    /**
     * The ID of the root push application, for which the push message request has been submitted
     *
     * @return containing application ID
     */
    public String getPushApplicationId() {
        return pushApplicationId;
    }

    public void setPushApplicationId(String pushApplicationId) {
        this.pushApplicationId = pushApplicationId;
    }

    /**
     * The Client Identifier showing who triggered the Push Notification
     *
     * @return string identifying the sender
     */
    public String getClientIdentifier() { return clientIdentifier; }

    public void setClientIdentifier(String clientIdentifier) { this.clientIdentifier = clientIdentifier; }

    /**
     * The number of active tokens, regardless from the variants, to which this Push Message was sent to
     *
     * @return the total of active tokens that received this Push Message
     */
    public long getTotalReceivers() {
        return totalReceivers;
    }

    public void setTotalReceivers(long totalReceivers) {
        this.totalReceivers = totalReceivers;
    }

    /**
     * The number of time this Push Application was opened after a Push Notification
     *
     * @return the number of time this Push Application was opened after a Push Notification
     */
    public long getAppOpenCounter() {
        return appOpenCounter;
    }

    public void setAppOpenCounter(long appOpenCounter) {
        this.appOpenCounter = appOpenCounter;
    }

    /**
     * Increment the AppOpenCounter
     */
    public void incrementAppOpenCounter() {
         this.setAppOpenCounter(this.getAppOpenCounter() +1);
    }

    /**
     * The date of the first time this Push Application was opened after a Push Notification
     *
     * @return the date of the first time this Push Application was opened after a Push Notification
     */
    public Date getFirstOpenDate() {
        return firstOpenDate;
    }

    public void setFirstOpenDate(Date firstOpenDate) {
        this.firstOpenDate = firstOpenDate;
    }

    /**
     * The date of the last time this Push Application was opened after a Push Notification
     *
     * @return the date of the last time this Push Application was opened after a Push Notification
     */
    public Date getLastOpenDate() {
        return lastOpenDate;
    }

    public void setLastOpenDate(Date lastOpenDate) {
        this.lastOpenDate = lastOpenDate;
    }

    /**
     * The number of variants that were fully processed (all batches were served).
     *
     * When {@link #getServedVariants()} is equal to {@link #getTotalVariants()}, the push message was fully processed.
     *
     * @return number of variants that were fully processed (all batches were served)
     */
    public int getServedVariants() {
        return servedVariants;
    }

    public void setServedVariants(int servedVariants) {
        this.servedVariants = servedVariants;
    }

    /**
     * The total number of variants to be served for the given push message.
     *
     * When {@link #getTotalVariants()} is equal to {@link #getServedVariants()}, the push message was fully processed.
     *
     * @return total number of variants to be served for the given push message.
     */
    public Integer getTotalVariants() {
        return totalVariants;
    }

    public void setTotalVariants(Integer totalVariants) {
        this.totalVariants = totalVariants;
    }

    public void addVariantInformations(VariantMetricInformation variantMetricInformation) {
        this.variantInformations.add(variantMetricInformation);
        variantMetricInformation.setPushMessageInformation(this);
    }
}
