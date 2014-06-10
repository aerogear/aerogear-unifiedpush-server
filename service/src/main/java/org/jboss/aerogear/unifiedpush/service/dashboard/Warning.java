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
package org.jboss.aerogear.unifiedpush.service.dashboard;

import org.jboss.aerogear.unifiedpush.api.Variant;

public class Warning {
    private Variant variant;
    private String applicationName;
    private String applicationID;

    public Warning(String applicationID, String applicationName, Variant variant) {
        this.applicationID = applicationID;
        this.applicationName = applicationName;
        this.variant = variant;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "variant=" + variant +
                ", applicationName='" + applicationName + '\'' +
                ", applicationId='" + applicationID + '\'' +
                '}';
    }
}
