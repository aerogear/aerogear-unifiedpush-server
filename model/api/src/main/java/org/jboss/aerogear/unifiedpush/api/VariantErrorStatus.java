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

public class VariantErrorStatus {

    //@Id
    private String pushMessageVariantId; // = "push-job-id" + ":" + "variant-id";
    private String errorReason; // the text we receive for the error

    private String pushJobId;
    private String variantID;

    private FlatPushMessageInformation pushMessageInformation;

    public VariantErrorStatus () {
        // bogus ctor
    }

    public VariantErrorStatus(final String pushJobId, final String variantId, final String errorReason) {
        this.pushMessageVariantId = new StringBuilder(pushJobId).append(':').append(variantId).toString();
        this.variantID = variantId;
        this.pushJobId = pushJobId;
        this.errorReason = errorReason;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getPushMessageVariantId() {
        return pushMessageVariantId;
    }

    public void setPushMessageVariantId(String pushMessageVariantId) {
        this.pushMessageVariantId = pushMessageVariantId;
    }

    public String getPushJobId() {
        return pushJobId;
    }

    public void setPushJobId(String pushJobId) {
        this.pushJobId = pushJobId;
    }

    public String getVariantID() {
        return variantID;
    }

    public void setVariantID(String variantId) {
        this.variantID = variantId;
    }

    public void setPushMessageInformation(FlatPushMessageInformation pushMessageInformation) {
        this.pushMessageInformation = pushMessageInformation;
    }

    public FlatPushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VariantErrorStatus that = (VariantErrorStatus) o;

        return pushMessageVariantId != null ? pushMessageVariantId.equals(that.pushMessageVariantId) : that.pushMessageVariantId == null;
    }

      @Override
      public int hashCode() {
          return pushMessageVariantId != null ? pushMessageVariantId.hashCode() : 0;
      }

}