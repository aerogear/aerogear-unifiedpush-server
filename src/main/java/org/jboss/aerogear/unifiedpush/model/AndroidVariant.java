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
package org.jboss.aerogear.unifiedpush.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jboss.aerogear.unifiedpush.api.VariantType;

/**
 * The Android variant class encapsulates GCM specific behavior.
 */
@Entity
@DiscriminatorValue("'android'")
public class AndroidVariant extends AbstractVariant {
    private static final long serialVersionUID = -4473752252296190311L;

    public AndroidVariant() {
        super();
        // we are Android:
        this.type = VariantType.ANDROID;
    }

    @Column
    @NotNull
    @Size(max=255)
    private String googleKey;

    @Column
    @Size(max=255)
    private String projectNumber;

    /**
     * The "Google Project Number" from the API Console is <i>not</i> needed for sending push messages, but it is a convenience to "see" it on
     * the Admin UI as well, since the Android applications require it (called Sender ID there). That way all informations are stored on the
     * same object.
     */
    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(final String projectNumber) {
        this.projectNumber = projectNumber;
    }

    /**
     * The Google API Key from the Google API project, which has been enabled for Android-based GCM.   
     */
    public String getGoogleKey() {
        return this.googleKey;
    }

    public void setGoogleKey(final String googleKey) {
        this.googleKey = googleKey;
    }
}