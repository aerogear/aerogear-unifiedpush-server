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
package org.jboss.aerogear.connectivity.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import org.jboss.aerogear.connectivity.api.VariantType;

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
    private String googleKey;

    public String getGoogleKey() {
        return this.googleKey;
    }

    public void setGoogleKey(final String googleKey) {
        this.googleKey = googleKey;
    }
}