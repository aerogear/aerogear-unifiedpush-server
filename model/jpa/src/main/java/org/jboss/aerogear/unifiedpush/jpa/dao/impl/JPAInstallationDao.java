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

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.jboss.aerogear.unifiedpush.dto.Token;
import org.jboss.aerogear.unifiedpush.dto.WebPushToken;

import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JPAInstallationDao extends JPABaseDao<Installation, String> implements InstallationDao {


    private static final String FROM_INSTALLATION_FOR_VARIANT_QUERY
            = " from Installation installation"
            + " left join installation.categories c "
            + " join installation.variant abstractVariant where abstractVariant.variantID = :variantID AND installation.enabled = true";

    private static final String FIND_ALL_DEVICES_FOR_VARIANT_QUERY = "select distinct installation.deviceToken, installation.publicKey, installation.authSecret"
                    + FROM_INSTALLATION_FOR_VARIANT_QUERY;

    private static final String FIND_ALL_DEVICES_FOR_VARIANT_QUERY_LEGACY = "select distinct installation.deviceToken, installation.publicKey, installation.authSecret"
                    + FROM_INSTALLATION_FOR_VARIANT_QUERY
                    + " AND locate(':', installation.deviceToken) = 0";

    private static final String FIND_ALL_DEVICE_TOKENS_FOR_VARIANT_QUERY = "select distinct installation.deviceToken"
            + FROM_INSTALLATION_FOR_VARIANT_QUERY;


    private static final String FIND_INSTALLATIONS = "FROM Installation installation"
                    + " JOIN installation.variant v"
                    + " WHERE v.variantID = :variantID";

    public PageResult<Installation, Count> findInstallationsByVariantForDeveloper(String variantID, String developer, Integer page, Integer pageSize, String search) {


        final StringBuilder jpqlBase = new StringBuilder(FIND_INSTALLATIONS);
        final Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("variantID", variantID);
        if (developer != null) {
            jpqlBase.append(" AND v.developer = :developer");
            parameters.put("developer", developer);
        }
        if (search != null) {
            jpqlBase.append(" AND ( installation.deviceToken LIKE :search"
                    + " OR installation.deviceType LIKE :search"
                    + " OR installation.platform LIKE :search"
                    + " OR installation.operatingSystem LIKE :search"
                    + " OR installation.osVersion LIKE :search"
                    + " OR installation.alias LIKE :search )");
            parameters.put("search", "%" + search + "%");
        }


        TypedQuery<Long> countQuery = createQuery("SELECT COUNT(installation) " + jpqlBase.toString(), Long.class);
        TypedQuery<Installation> query = createQuery("SELECT installation " + jpqlBase.toString() + " ORDER BY installation.id").setFirstResult(page * pageSize).setMaxResults(pageSize);

        List<Installation> resultList = setParameters(query, parameters).getResultList();
        Long count = setParameters(countQuery, parameters).getSingleResult();

        return new PageResult<>(resultList, new Count(count));
    }

    private <X> TypedQuery<X> setParameters(TypedQuery<X> query, Map<String, Object> parameters) {
        parameters.forEach((k,v) -> query.setParameter(k, v));
        return query;
    }

    public PageResult<Installation, Count> findInstallationsByVariant(String variantID, Integer page, Integer pageSize, String search) {
        return findInstallationsByVariantForDeveloper(variantID, null, page, pageSize, search);
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
        TypedQuery<String> query = createQuery(FIND_ALL_DEVICE_TOKENS_FOR_VARIANT_QUERY, String.class);
        query.setParameter("variantID", variantID);
        return new HashSet<>(query.getResultList());
    }

    @Override
    public ResultsStream.QueryBuilder<Token> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, final int maxResults, String lastTokenFromPreviousBatch, boolean oldGCM) {
        // the required part: Join + all tokens for variantID;

        final StringBuilder jpqlString = new StringBuilder(
                oldGCM ? FIND_ALL_DEVICES_FOR_VARIANT_QUERY_LEGACY : FIND_ALL_DEVICES_FOR_VARIANT_QUERY);
        final Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("variantID", variantID);

        // apend query conditions based on specified message parameters
        appendDynamicQuery(jpqlString, parameters, categories, aliases, deviceTypes);

        // sort on ids so that we can handle paging properly
        if (lastTokenFromPreviousBatch != null) {
            jpqlString.append(" AND installation.deviceToken > :lastTokenFromPreviousBatch");
            parameters.put("lastTokenFromPreviousBatch", lastTokenFromPreviousBatch);
        }

        jpqlString.append(" ORDER BY installation.deviceToken ASC");

        return new ResultsStream.QueryBuilder<Token>() {
            private Integer fetchSize = null;
            @Override
            public ResultsStream.QueryBuilder<Token> fetchSize(int fetchSize) {
                this.fetchSize = fetchSize;
                return this;
            }
            @Override
            public ResultsStream<Token> executeQuery() {
                Query hibernateQuery = JPAInstallationDao.this.createHibernateQuery(jpqlString.toString());
                hibernateQuery.setMaxResults(maxResults);

                parameters.forEach((k,v) -> {
					if (v  instanceof Collection<?>) {
                        hibernateQuery.setParameterList(k, (Collection<?>) v);
                    } else {
                        hibernateQuery.setParameter(k, v);
                    }
                 });

                hibernateQuery.setReadOnly(true);
                if (fetchSize != null) {
                    hibernateQuery.setFetchSize(fetchSize);
                }
                final ScrollableResults results = hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
                return new ResultsStream<Token>() {
                    @Override
                    public boolean next() throws ResultStreamException {
                        return results.next();
                    }
                    @Override
                    public Token get() throws ResultStreamException {
                        Object[] row = results.get();
                        String endpoint = (String) row[0];
                        String publicKey = (String) row[1];
                        String authSercret = (String) row[2];

                        if (publicKey != null && authSercret != null) {
                            return new WebPushToken(endpoint, publicKey, authSercret);
                        } else {
                            return new Token(endpoint);
                        }
                    }
                };
            }

        };
    }

    @Override
    public long getNumberOfDevicesForLoginName(String loginName) {
        return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t where t.developer = :developer) ", Long.class)
                .setParameter("developer", loginName).getSingleResult();
    }

    @Override
    public Class<Installation> getType() {
        return Installation.class;
    }

    //Admin query
    @Override
    public long getTotalNumberOfDevices() {
        return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t) ", Long.class)
                .getSingleResult();
    }

    @Override
    public long getNumberOfDevicesForVariantID(String variantId) {
        return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID = :variantId ", Long.class)
                .setParameter("variantId", variantId)
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
