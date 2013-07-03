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

package org.jboss.aerogear.connectivity.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.jboss.aerogear.connectivity.api.MobileVariant;
import org.jboss.aerogear.connectivity.jpa.PersistentObject;

@Entity
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractMobileVariant extends PersistentObject implements MobileVariant {
    private static final long serialVersionUID = -5028062942838899201L;

    public AbstractMobileVariant() {
        secret = UUID.randomUUID().toString();
    }

    @Column
    private String name;
    @Column
    private String description;
    @Column
    private String variantID;
    @Column
    private String secret;
    @Column
    private String developer;

    // TODO: let's do LAZY
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="variantID", referencedColumnName="variantID")
    private Set<MobileVariantInstanceImpl> instances = new HashSet<MobileVariantInstanceImpl>();

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public Set<MobileVariantInstanceImpl> getInstances() {
        return this.instances;
    }

    public void setInstances(final Set<MobileVariantInstanceImpl> instances) {
        this.instances = instances;
    }

    public String getVariantID() {
        return variantID;
    }

    public void setVariantID(String variantID) {
        this.variantID = variantID;
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }
}
