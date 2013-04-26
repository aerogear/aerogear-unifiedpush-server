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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.aerogear.connectivity.api.MobileApplication;
import org.aerogear.connectivity.jpa.PersistentObject;

@Entity
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.JOINED)
public  class AbstractMobileApplication extends PersistentObject implements MobileApplication {
    private static final long serialVersionUID = -5028062942838899201L;

    @Column
    private String name;
    
    @Column
    private String description;
    
    // TODO: let's do LAZY
    @OneToMany(fetch=FetchType.EAGER)
    private Set<MobileApplicationInstance> instances = new HashSet<MobileApplicationInstance>();

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
    public Set<MobileApplicationInstance> getInstances() {
       return this.instances;
    }

    public void setInstances(final Set<MobileApplicationInstance> instances) {
       this.instances = instances;
    }

}
