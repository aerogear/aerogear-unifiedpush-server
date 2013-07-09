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
import javax.persistence.Lob;

@Entity
@DiscriminatorValue("'ios'")
public class iOSVariant extends AbstractMobileVariant {
    private static final long serialVersionUID = -889367404039436329L;

    public iOSVariant() {
        super();
    }

    @Column
    private String developmentPassphrase;

    @Column
    private String productionPassphrase;

    @Lob
    @Column
    private byte[] developmentCertificate;

    @Lob
    @Column
    private byte[] productionCertificate;

    public String getDevelopmentPassphrase() {
        return this.developmentPassphrase;
    }

    public void setDevelopmentPassphrase(final String passphrase) {
        this.developmentPassphrase = passphrase;
    }

    public String getProductionPassphrase() {
        return productionPassphrase;
    }

    public void setProductionPassphrase(String productionPassphrase) {
        this.productionPassphrase = productionPassphrase;
    }

    public byte[] getDevelopmentCertificate() {
        return developmentCertificate;
    }

    public void setDevelopmentCertificate(byte[] cert) {
        this.developmentCertificate = cert;
    }

    public byte[] getProductionCertificate() {
        return productionCertificate;
    }

    public void setProductionCertificate(byte[] productionCertificate) {
        this.productionCertificate = productionCertificate;
    }
}
