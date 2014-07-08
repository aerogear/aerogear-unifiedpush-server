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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPAPushApplicationDao extends JPABaseDao implements PushApplicationDao {

    @Override
    public void create(PushApplication pushApplication) {
        persist(pushApplication);
    }

    @Override
    public void update(PushApplication pushApplication) {
        merge(pushApplication);
    }

    @Override
    public void delete(PushApplication pushApplication) {
        PushApplication entity = entityManager.find(PushApplication.class, pushApplication.getId());
        remove(entity);
    }

    @Override
    public List<PushApplication> findAllForDeveloper(String loginName) {

        List<PushApplication> entities = createQuery("select pa from PushApplication pa where pa.developer = :developer")
                .setParameter("developer", loginName).getResultList();

        return entities;
    }


    @Override
    public List<String> findAllPushApplicationIDsForDeveloper (String loginName) {

        List<String> ids = createQuery("select pa.pushApplicationID from PushApplication pa where pa.developer = :developer")
                .setParameter("developer", loginName).getResultList();

        return ids;
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {

        PushApplication entity = getSingleResultForQuery(createQuery(
                "select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID and pa.developer = :developer")
                .setParameter("pushApplicationID", pushApplicationID)
                .setParameter("developer", loginName));

        return entity;
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {

        PushApplication entity = getSingleResultForQuery(createQuery("select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));

        return entity;
    }

    @Override
    public Map<String, Long> countInstallationsByType(String pushApplicationID) {
        final String jpql = "select v, count(*) from PushApplication pa join pa.variants v join v.installations i "
                + "where pushApplicationID = :pushApplicationID "
                + "group by v.variantID";

        final HashMap<String, Long> results = new HashMap<String, Long>();

        for (VariantType type : VariantType.values()) {
            results.put(type.getTypeName(), 0L);
        }
        final Query query = createQuery(jpql)
                .setParameter("pushApplicationID", pushApplicationID);
        final List<Object[]> resultList = query.getResultList();
        for (Object[] objects : resultList) {
            final Long value = (Long) objects[1];
            final Variant variant = (Variant) objects[0];
            results.put(variant.getType().getTypeName(), results.get(variant.getType().getTypeName()) + value);
            results.put(variant.getVariantID(), value);
        }

        return results;
    }

    @Override
    public long getNumberOfPushApplicationsForDeveloper(String name) {
        return (Long) createQuery("select count(pa) from PushApplication pa where pa.developer = :developer")
                .setParameter("developer", name).getSingleResult();
    }

    @Override
    public List<PushApplication> findByVariantIds(List<String> variantIDs) {
        final String jpql = "select pa from PushApplication pa left join fetch pa.variants v where v.variantID in (:variantIDs)";

        return (List<PushApplication>) createQuery(jpql).setParameter("variantIDs", variantIDs).getResultList();
    }

    @Override
    public PushApplication find(String id) {
        PushApplication entity = entityManager.find(PushApplication.class, id);
        return  entity;
    }

    private PushApplication getSingleResultForQuery(Query query) {
        List<PushApplication> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }
}
