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

import javax.persistence.Entity;
import javax.persistence.Column;

import org.aerogear.connectivity.jpa.PersistentObject;

@Entity
public class MobileApplicationInstance extends PersistentObject {
    private static final long serialVersionUID = 7177135979544758234L;

    @Column
    private String deviceToken;
    @Column
    private String deviceType;
    @Column
    private String mobileOperatingSystem;
    @Column
    private String osVersion;
    @Column
    private String clientIdentifier;
    @Column
    private String category;

    
   public String getDeviceToken() {
      return this.deviceToken;
   }

   public void setDeviceToken(final String deviceToken) {
      this.deviceToken = deviceToken;
   }

   public String getDeviceType() {
      return this.deviceType;
   }

   public void setDeviceType(final String deviceType) {
      this.deviceType = deviceType;
   }


   public String getMobileOperatingSystem() {
      return this.mobileOperatingSystem;
   }

   public void setMobileOperatingSystem(final String mobileOperatingSystem) {
      this.mobileOperatingSystem = mobileOperatingSystem;
   }


   public String getOsVersion() {
      return this.osVersion;
   }

   public void setOsVersion(final String osVersion) {
      this.osVersion = osVersion;
   }

   public String getClientIdentifier() {
      return this.clientIdentifier;
   }

   public void setClientIdentifier(final String clientIdentifier) {
      this.clientIdentifier = clientIdentifier;
   }

   public String getCategory() {
       return category;
   }

   public void setCategory(final String category) {
       this.category = category;
   }
}
