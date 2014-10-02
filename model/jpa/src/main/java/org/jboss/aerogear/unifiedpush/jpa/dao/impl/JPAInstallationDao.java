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

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public class JPAInstallationDao extends JPABaseDao implements InstallationDao {

    @Override
    public void create(Installation installation) {
        persist(installation);
    }

    @Override
    public void update(Installation installation) {
        merge(installation);
    }

    @Override
    public void delete(Installation installation) {
        Installation entity = entityManager.find(Installation.class, installation.getId());
        remove(entity);
    }

    public PageResult<Installation> findInstallationsByVariant(String variantID, String developer, Integer page, Integer pageSize) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Installation> query = builder.createQuery(Installation.class);
        Root<Installation> v = query.from(Installation.class);
        final Join join = v.join("variant");
        final Predicate[] predicates = getPredicates(variantID, developer, builder, join);
        query.where(predicates);

        List<Installation> result = entityManager.createQuery(query)
                .setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        final Join<Object, Object> join1 = countQuery.from(Installation.class).join("variant");
        countQuery.where(getPredicates(variantID, developer, builder, join1));
        final Long count = entityManager.createQuery(countQuery.select(builder.count(join1))).getSingleResult();

        return new PageResult<Installation>(result, count);
    }

    private Predicate[] getPredicates(String variantID, String developer, CriteriaBuilder builder, Join join) {
        return new Predicate[]{builder.equal(join.get("variantID"), variantID),
                    builder.and(builder.equal(join.get("developer"), developer))};
    }

    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {

        Installation entity = getSingleResultForQuery(createQuery("select installation from Installation installation " +
                " join installation.variant abstractVariant" +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken = :deviceToken")
                .setParameter("variantID", variantID)
                .setParameter("deviceToken", deviceToken));

        return entity;
    }

    @Override
    public List<Installation> findInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // if there are no device-tokens, no need to bug the database
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            // be nice and return an empty list...
            return Collections.EMPTY_LIST;
        }

        List<Installation> entities = createQuery("select installation from Installation installation " +
                " join installation.variant abstractVariant " +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken IN :deviceTokens")
                .setParameter("variantID", variantID)
                .setParameter("deviceTokens", deviceTokens)
                .getResultList();

        return entities;
    }

    @Override
    public List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes) {
        // the required part: Join + all tokens for variantID;

        final StringBuilder jpqlString = new StringBuilder("select installation.deviceToken from Installation installation")
                .append( " left join installation.categories c ")
                .append(" join installation.variant abstractVariant where abstractVariant.variantID = :variantID AND installation.enabled = true");

        return this.executeDynamicQuery(jpqlString, variantID, categories, aliases, deviceTypes);
    }

    @Override
    public long getNumberOfDevicesForVariantIDs(String loginName) {
        return (Long) createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t where t.developer = :developer) ")
                .setParameter("developer", loginName).getSingleResult();
    }

    @Override
    public Installation find(String id) {
        Installation entity = entityManager.find(Installation.class, id);

        return entity;
    }

    /**
     *
     * A dynamic finder for all sorts of queries around selecting Device-Token, based on different criterias.
     * The method appends different criterias to the given JPQL string, IF PRESENT.
     *
     * Done in one method, instead of having similar, but error-thrown Strings, in different methods.
     *
     * TODO: perhaps moving to Criteria API for this later
     */
    @SuppressWarnings("unchecked")
    private List<String> executeDynamicQuery(final StringBuilder jpqlBaseString, String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes) {

        // parameter names and values, stored in a map:
        final Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        // OPTIONAL query arguments, as provided.....
        // are aliases present ??
        if (isListEmpty(aliases)) {
            // append the string:
            jpqlBaseString.append(" and installation.alias IN :aliases");
            // add the params:
            parameters.put("aliases", aliases);
        }

        // are devices present ??
        if (isListEmpty(deviceTypes)) {
            // append the string:
            jpqlBaseString.append(" and installation.deviceType IN :deviceTypes");
            // add the params:
            parameters.put("deviceTypes", deviceTypes);
        }

        // is a category present ?
        if (isListEmpty(categories)) {
            jpqlBaseString.append(" and ( c.name in (:categories))");
            parameters.put("categories", categories);
        }

        // the entire JPQL string
        Query jpql = createQuery(jpqlBaseString.toString());
        // add REQUIRED param:
        jpql.setParameter("variantID", variantID);

        // add the optionals, as needed:
        Set<String> paramKeys = parameters.keySet();
        for (String parameterName : paramKeys) {
            jpql.setParameter(parameterName, parameters.get(parameterName));
        }

        return jpql.getResultList();
    }
    /**
     * Checks if the list is empty, and not null
     */
    private boolean isListEmpty(List list) {
        return (list != null && !list.isEmpty());
    }

    private Installation getSingleResultForQuery(Query query) {
        List<Installation> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }
}
