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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;

public class JPAInstallationDao extends JPABaseDao<Installation, String> implements InstallationDao {

    private static final String FIND_ALL_DEVICES_FOR_VARIANT_QUERY = "select distinct installation.deviceToken"
                    + " from Installation installation"
                    + " left join installation.categories c "
                    + " join installation.variant abstractVariant where abstractVariant.variantID = :variantID AND installation.enabled = true";

    public PageResult<Installation> findInstallationsByVariantForDeveloper(String variantID, String developer, Integer page, Integer pageSize) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Installation> query = builder.createQuery(Installation.class);
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

    public PageResult<Installation> findInstallationsByVariant(String variantID, Integer page, Integer pageSize) {
        return findInstallationsByVariantForDeveloper(variantID, null, page, pageSize);
    }

    private Predicate[] getPredicates(String variantID, String developer, CriteriaBuilder builder, Join join) {
        Predicate[] predicates;
        if(developer!=null) {
            predicates = new Predicate[]{builder.equal(join.get("variantID"), variantID),
                    builder.and(builder.equal(join.get("developer"), developer))};
        }
        else {
            predicates = new Predicate[]{builder.equal(join.get("variantID"), variantID)};
        }
        return predicates;
    }


    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {

        return getSingleResultForQuery(createQuery("select installation from Installation installation " +
                " join installation.variant abstractVariant" +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken = :deviceToken")
                .setParameter("variantID", variantID)
                .setParameter("deviceToken", deviceToken));
    }

    @Override
    public List<Installation> findInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // if there are no device-tokens, no need to bug the database
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            // be nice and return an empty list...
            return Collections.emptyList();
        }

        return createQuery("select installation from Installation installation " +
                " join installation.variant abstractVariant " +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken IN :deviceTokens")
                .setParameter("variantID", variantID)
                .setParameter("deviceTokens", deviceTokens)
                .getResultList();
    }



    @Override
    public Set<String> findAllDeviceTokenForVariantID(String variantID) {
        TypedQuery<String> query = createQuery(FIND_ALL_DEVICES_FOR_VARIANT_QUERY, String.class);
        query.setParameter("variantID", variantID);
        return new HashSet<String>(query.getResultList());
    }

    @Override
    public ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, final int maxResults, String lastTokenFromPreviousBatch) {
        // the required part: Join + all tokens for variantID;

        final StringBuilder jpqlString = new StringBuilder(FIND_ALL_DEVICES_FOR_VARIANT_QUERY);
        final Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("variantID", variantID);

        // apend query conditions based on specified message parameters
        appendDynamicQuery(jpqlString, parameters, categories, aliases, deviceTypes);

        // sort on ids so that we can handle paging properly
        if (lastTokenFromPreviousBatch != null) {
            jpqlString.append(" AND installation.deviceToken > :lastTokenFromPreviousBatch");
            parameters.put("lastTokenFromPreviousBatch", lastTokenFromPreviousBatch);
        }

        jpqlString.append(" ORDER BY installation.deviceToken ASC");

        return new ResultsStream.QueryBuilder<String>() {
            private Integer fetchSize = null;
            @Override
            public ResultsStream.QueryBuilder<String> fetchSize(int fetchSize) {
                this.fetchSize = fetchSize;
                return this;
            }
            @Override
            public ResultsStream<String> executeQuery() {
                Query hibernateQuery = JPAInstallationDao.this.createHibernateQuery(jpqlString.toString());
                hibernateQuery.setMaxResults(maxResults);
                for (Entry<String, Object> parameter : parameters.entrySet()) {
                    Object value = parameter.getValue();
                    if (value instanceof Collection<?>) {
                        hibernateQuery.setParameterList(parameter.getKey(), (Collection<?>) parameter.getValue());
                    } else {
                        hibernateQuery.setParameter(parameter.getKey(), parameter.getValue());
                    }

                }
                hibernateQuery.setReadOnly(true);
                if (fetchSize != null) {
                    hibernateQuery.setFetchSize(fetchSize);
                }
                final ScrollableResults results = hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
                return new ResultsStream<String>() {
                    @Override
                    public boolean next() throws ResultStreamException {
                        return results.next();
                    }
                    @Override
                    public String get() throws ResultStreamException {
                        return (String) results.get()[0];
                    }
                };
            }

        };
    }

    @Override
    public long getNumberOfDevicesForVariantIDs(String loginName) {
        return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t where t.developer = :developer) ", Long.class)
                .setParameter("developer", loginName).getSingleResult();
    }

    @Override
    public Class<Installation> getType() {
        return Installation.class;
    }

    //Admin query
    @Override
    public long getNumberOfDevicesForVariantIDs() {
        return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t) ", Long.class)
                .getSingleResult();
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
    private void appendDynamicQuery(final StringBuilder jpqlString, final Map<String, Object> parameters, List<String> categories, List<String> aliases, List<String> deviceTypes) {

        // OPTIONAL query arguments, as provided.....
        // are aliases present ??
        if (isListEmpty(aliases)) {
            // append the string:
            jpqlString.append(" AND installation.alias IN :aliases");
            // add the params:
            parameters.put("aliases", aliases);
        }

        // are devices present ??
        if (isListEmpty(deviceTypes)) {
            // append the string:
            jpqlString.append(" AND installation.deviceType IN :deviceTypes");
            // add the params:
            parameters.put("deviceTypes", deviceTypes);
        }

        // is a category present ?
        if (isListEmpty(categories)) {
            jpqlString.append(" AND ( c.name in (:categories))");
            parameters.put("categories", categories);
        }
    }
    /**
     * Checks if the list is empty, and not null
     */
    private boolean isListEmpty(List list) {
        return (list != null && !list.isEmpty());
    }
}
