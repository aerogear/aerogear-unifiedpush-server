/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
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

public class FlatPushMessageInformation extends BaseModel {

    private static final long serialVersionUID = -1L;

    @NotNull
    private String pushApplicationId;

    private String rawJsonMessage;
    private String ipAddress;
    private String clientIdentifier;

    private Date submitDate = new Date();

    private Long appOpenCounter = 0L;
    private Date firstOpenDate;
    private Date lastOpenDate;

    // this is new:
    // once an error on a variant occurs, a error reason will be added to the list    
    private Set<VariantErrorStatus> errors = new HashSet();

    // end of not relevant section

    public void setRawJsonMessage(final String rawJsonMessage) {
        this.rawJsonMessage = rawJsonMessage;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPushApplicationId(final String pushApplicationId) {
        this.pushApplicationId = pushApplicationId;
    }

    public void setClientIdentifier(final String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public void setFirstOpenDate(Date firstOpenDate) {
        this.firstOpenDate = firstOpenDate;
    }

    public Date getFirstOpenDate() {
        return firstOpenDate;
    }

    public void setLastOpenDate(Date lastOpenDate) {
        this.lastOpenDate = lastOpenDate;
    }

    public Date getLastOpenDate() {
        return lastOpenDate;
    }

    public void incrementAppOpenCounter() {
        this.appOpenCounter++;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public String getRawJsonMessage() {
        return rawJsonMessage;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public String getPushApplicationId() {
        return pushApplicationId;
    }

    public Long getAppOpenCounter() {
        return appOpenCounter;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setErrors(Set<VariantErrorStatus> errors) {
        this.errors = errors;
    }

    public Set<VariantErrorStatus> getErrors() {
        return errors;
    }

    public void setAppOpenCounter(Long appOpenCounter) {
        this.appOpenCounter = appOpenCounter;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

}
