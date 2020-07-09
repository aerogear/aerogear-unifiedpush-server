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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dto.Count;

public class JPAPushApplicationDao extends JPABaseDao<PushApplication, String> implements PushApplicationDao {

    @Override
    public void delete(PushApplication pushApplication) {
        PushApplication entity = entityManager.find(PushApplication.class, pushApplication.getId());
        final List<Variant> variants = entity.getVariants();
        if (!variants.isEmpty()) {
            entityManager.createQuery("delete from Installation i where i.variant in :variants")
                    .setParameter("variants", variants).executeUpdate();
        }
        super.delete(entity);
    }

    @Override
    public PageResult<PushApplication, Count> findAllForDeveloper(String loginName, Integer page, Integer pageSize) {
        String select = "from PushApplication pa where pa.developer = :developer";

        Long count = entityManager.createQuery("select count(*) " + select, Long.class)
                .setParameter("developer", loginName).getSingleResult();
        List<PushApplication> entities = entityManager.createQuery("select pa " + select, PushApplication.class)
                .setFirstResult(page * pageSize).setMaxResults(pageSize)
                .setParameter("developer", loginName).getResultList();

        return new PageResult<>(entities, new Count(count));
    }


    @Override
    public List<String> findAllPushApplicationIDsForDeveloper (String loginName) {
        return createQuery("select pa.pushApplicationID from PushApplication pa where pa.developer = :developer", String.class)
                .setParameter("developer", loginName).getResultList();
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {
        return getSingleResultForQuery(createQuery(
                "select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID and pa.developer = :developer")
                .setParameter("pushApplicationID", pushApplicationID)
                .setParameter("developer", loginName));
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {
        return getSingleResultForQuery(createQuery("select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));
    }

    @Override
    public Map<String, Long> countInstallationsByType(String pushApplicationID) {
        final String jpql = "select v.type, v.variantID, count(*) from Installation i join i.variant v where i.variant.variantID in "
                + "(select v.variantID from PushApplication pa join pa.variants v where pa.pushApplicationID = :pushApplicationID) "
                + "group by v.type, v.variantID";

        final HashMap<String, Long> results = new HashMap<>();

        for (VariantType type : VariantType.values()) {
            results.put(type.getTypeName(), 0L);
        }
        final TypedQuery<Object[]> query = createQuery(jpql, Object[].class)
                .setParameter("pushApplicationID", pushApplicationID);

        query.getResultList().forEach(result -> {
            final Long value = (Long) result[2];
            final VariantType variantType = (VariantType) result[0];
            results.put(variantType.getTypeName(), results.get(variantType.getTypeName()) + value);
            results.put((String) result[1], value);
        });

        return results;
    }

    @Override
    public long getNumberOfPushApplicationsForDeveloper(String name) {
        return createQuery("select count(pa) from PushApplication pa where pa.developer = :developer", Long.class)
                .setParameter("developer", name).getSingleResult();
    }

    @Override
    public List<PushApplication> findByVariantIds(List<String> variantIDs) {
        final String jpql = "select pa from PushApplication pa left join fetch pa.variants v where v.variantID in (:variantIDs)";

        return createQuery(jpql).setParameter("variantIDs", variantIDs).getResultList();
    }

    @Override
    public Class<PushApplication> getType() {
        return PushApplication.class;
    }

    //Specific queries to the Admin
    @Override
    public PageResult<PushApplication, Count> findAll(Integer page, Integer pageSize) {

        String select = "from PushApplication pa";

        Long count = entityManager.createQuery("select count(*) " + select, Long.class).getSingleResult();

        List<PushApplication> entities = entityManager.createQuery("select pa " + select + " order by pa.name, pa.id", PushApplication.class)
                .setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();

        return new PageResult<>(entities, new Count(count));
    }

    @Override
    public PushApplication findAllByPushApplicationID(String pushApplicationID) {
        return getSingleResultForQuery(createQuery(
                "select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));
    }

    @Override
    public long getNumberOfPushApplicationsForDeveloper() {
        return createQuery("select count(pa) from PushApplication pa", Long.class)
                .getSingleResult();
    }

}
