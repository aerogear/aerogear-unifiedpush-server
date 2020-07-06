/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * The Android variant class encapsulates FCM specific behavior.
 */
public class AndroidVariant extends Variant {
    private static final long serialVersionUID = -4473752252296190311L;

    @NotNull
    @Size(min = 1, max = 255, message = "Server Key must be max. 255 chars long")
    private String googleKey;

    @Size(max = 255, message = "Sender ID must be max. 255 chars long")
    private String projectNumber;

    /**
     * The "Google Project Number" (or Sender ID) from the API Console is <i>not</i> needed for sending push messages, but it is a convenience to "see" it on
     * the Admin UI as well, since the Android applications require it (called Sender ID there). That way all information is stored on the
     * same object.
     *
     * @return the Sender ID string
     */
    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(final String projectNumber) {
        this.projectNumber = projectNumber;
    }

    /**
     * The Server Key from the Firebase Console of a project which has been enabled for FCM.
     *
     @return the Server key
     */
    public String getGoogleKey() {
        return this.googleKey;
    }

    public void setGoogleKey(final String googleKey) {
        this.googleKey = googleKey;
    }

    @Override
    public VariantType getType() {
        return VariantType.ANDROID;
    }

    public void merge(AndroidVariant androidVariant) {
        // apply updated data:
        if (getGoogleKey() != null && !getGoogleKey().isBlank()) {
            androidVariant.setGoogleKey(getGoogleKey());
        }

        if (getProjectNumber() != null && !getProjectNumber().isBlank()) {
            androidVariant.setProjectNumber(getProjectNumber());
        }
        if (getName() != null && !getName().isBlank()) {
            androidVariant.setName(getName());
        }
        if (getDescription() != null && !getDescription().isBlank()) {
            androidVariant.setDescription(getDescription());
        }
    }
}
