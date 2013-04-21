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

package org.aerogear.connectivity.rest.util;

import javax.ws.rs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
 
public class iOSApplicationUploadForm {
 
    public iOSApplicationUploadForm() {
    }
 
    private String passphrase;
    private byte[] certificate;
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    @FormParam("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @FormParam("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassphrase() {
        return passphrase;
    }
    
    @FormParam("passphrase")
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
    
    public byte[] getCertificate() {
        return certificate;
    }
    @FormParam("certificate")
    @PartType("application/octet-stream")
    public void setCertificate(byte[] data) {
        this.certificate = data;
    }
 
}