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
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Simple dao that checks if the database is available by running select 1 query.
 */
public class JPAHealthDao {
    @Inject
    private EntityManager entityManager;

    public boolean dbCheck() {
        return entityManager.createNativeQuery("select 1 from PushApplication").getFirstResult() == 1;
    }
}
