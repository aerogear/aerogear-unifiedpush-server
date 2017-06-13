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

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;

import java.util.Collections;
import java.util.List;

public class JPAVariantDao extends JPABaseDao<Variant, String> implements VariantDao {


    @Override
    public void delete(Variant variant) {
        entityManager.createQuery("delete from Installation i where i.variant in :variant")
            .setParameter("variant", variant).executeUpdate();
        entityManager.createQuery("delete from VariantErrorStatus ves where ves.variant in :variant")
            .setParameter("variant", variant).executeUpdate();
        Variant entity = find(variant.getId());
        super.delete(entity);
    }


    @Override
    public Variant findByVariantID(String variantID) {
        return getSingleResultForQuery(createQuery("select t from Variant t where t.variantID = :variantID")
                .setParameter("variantID", variantID));
    }

    @Override
    public boolean existsVariantIDForDeveloper(String variantID, String loginName) {

        Long numberOfVariants = createQuery("select count(t) from Variant t where t.variantID = :variantID and t.developer = :developer", Long.class)
                .setParameter("variantID", variantID)
                .setParameter("developer", loginName).getSingleResult();


        return numberOfVariants == 1L;
    }


    @Override
    public List<String> findVariantIDsForDeveloper(String loginName) {
        return createQuery("select t.variantID from Variant t where t.developer = :developer", String.class)
                .setParameter("developer", loginName).getResultList();
    }

    @Override
    public List<Variant> findAllVariantsByIDs(List<String> variantIDs) {
        if (variantIDs.isEmpty()) {
            return Collections.emptyList();
        }

        return createQuery("select t from Variant t where t.variantID IN :variantIDs")
                .setParameter("variantIDs", variantIDs).getResultList();
    }

    //Admin queries
    @Override
    public boolean existsVariantIDForAdmin(String variantID) {

        Long numberOfVariants = createQuery("select count(t) from Variant t where t.variantID = :variantID", Long.class)
                .setParameter("variantID", variantID).getSingleResult();

        return numberOfVariants == 1L;
    }

    @Override
    public Class<Variant> getType() {
        return Variant.class;
    }
}
