/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.aerogear.connectivity.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Column;

@Entity
@DiscriminatorValue("'android'")
public class AndroidApplication extends AbstractMobileApplication {
    private static final long serialVersionUID = -4473752252296190311L;

    @Column
    private String googleKey;

    public String getGoogleKey() {
        return this.googleKey;
    }

    public void setGoogleKey(final String googleKey) {
        this.googleKey = googleKey;
    }
}