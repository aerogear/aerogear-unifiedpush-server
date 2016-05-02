/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;

import java.util.Iterator;
import java.util.List;

public class JPACategoryDao extends JPABaseDao<Category, Integer> implements CategoryDao {

    @Override
    public List<Category> findByNames(List<String> names) {

        StringBuilder sb = new StringBuilder();
        Iterator<String> i = names.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()).append("'");

            if (i.hasNext())
            {
                sb.append(",");
            }
        }

        String sqlString = String.format("{ $query: { 'name' : { $in : [%s] } } }", sb.toString());

        return createNativeQuery(sqlString).getResultList();

            /*categoryList = entityManager.createQuery("select c from Category c where c.name in :names", Category.class)
                    .setParameter("names", names).getResultList();*/
    }



    @Override
    public Class<Category> getType() {
        return Category.class;
    }
}
