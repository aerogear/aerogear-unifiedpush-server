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
package org.jboss.aerogear.unifiedpush.model.jpa;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@DiscriminatorValue("ios")
public class iOSVariantEntity extends AbstractVariantEntity {
    private static final long serialVersionUID = -889367404039436329L;

    public iOSVariantEntity() {
        super();
    }
    
    public VariantType getType() {
    	return VariantType.IOS;
    }

    @Column
    private boolean production;

    @Column
    @NotNull
    @Size(max = 255)
    private String passphrase;

    @Lob
    @Column(name = "CERT")
    @NotNull
    private byte[] certificate;

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public void setPassphrase(final String passphrase) {
        this.passphrase = passphrase;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] cert) {
        this.certificate = cert;
    }
}
