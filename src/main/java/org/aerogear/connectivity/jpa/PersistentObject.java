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

package org.aerogear.connectivity.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public abstract class PersistentObject implements Serializable {
    private static final long serialVersionUID = -2604260447891156143L;

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false)
    private String id = null;

//    @Version
//    @Column(name = "version")
//    private int version = 0;

    public String getId()
    {
       return this.id;
    }

    public void setId(final String id)
    {
       this.id = id;
    }

//    public int getVersion()
//    {
//       return this.version;
//    }
//
//    public void setVersion(final int version)
//    {
//       this.version = version;
//    }

}
