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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.dao.helper.InstallationAlias;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.springframework.stereotype.Repository;

@Repository
public class JPAInstallationDao extends JPABaseDao<Installation, String> implements InstallationDao {

    private static final String FIND_ALL_DEVICES_FOR_VARIANT_QUERY = "select distinct installation.deviceToken"
                    + " from Installation installation"
                    + " left join installation.categories c "
                    + " join installation.variant abstractVariant where abstractVariant.variantID = :variantID AND installation.enabled = true";

    private static final String FIND_ALL_DEVICES_FOR_VARIANT_QUERY_LEGACY = "select distinct installation.deviceToken"
                    + " from Installation installation"
                    + " left join installation.categories c "
                    + " join installation.variant abstractVariant where abstractVariant.variantID = :variantID AND installation.enabled = true AND locate(':', installation.deviceToken) = 0";

    private static final String FIND_INSTALLATIONS = "FROM Installation installation"
                    + " JOIN installation.variant v"
                    + " WHERE v.variantID = :variantID";

    @Override
    public PageResult<Installation, Count> findInstallationsByVariantForDeveloper(
            String variantID, String developer, Integer page, Integer pageSize, String search) {


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

        String jpqlStr = jpqlBase.toString();
        TypedQuery<Long> countQuery = createQuery("SELECT COUNT(installation) " + jpqlStr, Long.class);
        TypedQuery<Installation> query = createQuery("SELECT installation " + jpqlStr + " ORDER BY installation.id")
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize);

        List<Installation> resultList = setParameters(query, parameters).getResultList();
        Long count = setParameters(countQuery, parameters).getSingleResult();

        return new PageResult<>(resultList, new Count(count));
    }

    private <X> TypedQuery<X> setParameters(TypedQuery<X> query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
        return query;
    }

    @Override
    public PageResult<Installation, Count> findInstallationsByVariant(String variantID, Integer page, Integer pageSize, String search) {
        return findInstallationsByVariantForDeveloper(variantID, null, page, pageSize, search);
    }


    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {

        return getSingleResultForQuery(createQuery(getFindVariantByDeviceTokenQuery().toString())
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
    public ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, final int maxResults, String lastTokenFromPreviousBatch, boolean oldGCM) {
        // the required part: Join + all tokens for variantID;

        final StringBuilder jpqlString = oldGCM ? new StringBuilder(FIND_ALL_DEVICES_FOR_VARIANT_QUERY_LEGACY) : new StringBuilder(FIND_ALL_DEVICES_FOR_VARIANT_QUERY);
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

        return new ResultsStream.QueryBuilder<String>() {
            private Integer fetchSize;
            @Override
            public ResultsStream.QueryBuilder<String> fetchSize(int fetchSize) {
                this.fetchSize = fetchSize;
                return this;
            }
            @Override
            public ResultsStream<String> executeQuery() {
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
    public long getNumberOfDevicesForLoginName(String loginName) {
        return createQuery("select count(installation) from Installation installation, Variant t where installation.variant = t.variantID and t.developer = :developer ", Long.class)
                .setParameter("developer", loginName).getSingleResult();
    }

    @Override
    public Class<Installation> getType() {
        return Installation.class;
    }

    //Admin query
    @Override
    public long getTotalNumberOfDevices() {
        return createQuery("select count(installation) from Installation installation", Long.class)
                .getSingleResult();
    }

    @Override
    public long getNumberOfDevicesForVariantID(String variantId) {
        return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID = :variantId ", Long.class)
                .setParameter("variantId", variantId)
                .getSingleResult();
    }

    /**
     * Return list of installations by aliases list.
     * Match is case insensitive and expect aliases list to be lowercase letters.
     */
    @SuppressWarnings("unchecked")
	@Override
	public List<InstallationAlias> findByVariantIDsInAliasList(List<String> variantIDs, List<String> aliases) {
    	javax.persistence.Query typedQuery = entityManager.createQuery("select i.id, i.alias from Installation i" +
    			" where i.variant.variantID IN :variantIDs " + //
    			" and LOWER(i.alias) IN :aliases") //
    			.setParameter("variantIDs", variantIDs) //
    			.setParameter("aliases", getAliases(aliases));//

    	return toInstallationAlias(typedQuery.getResultList());
    }

    private List<InstallationAlias> toInstallationAlias(List<Object[]> results){
    	List<InstallationAlias> installations = new ArrayList<>();
    	if (results != null && results.size() > 0){
    		for (Object[] instlation: results){
    			installations.add(new InstallationAlias((String)instlation[0], (String)instlation[1]));
    		}
    	}

    	return installations;
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
        if (isListNotEmpty(aliases)) {
            // append the string:
            jpqlString.append(" AND LOWER(installation.alias) IN :aliases");
            // add the params:
            parameters.put("aliases", getAliases(aliases));
        }

        // are devices present ??
        if (isListNotEmpty(deviceTypes)) {
            // append the string:
            jpqlString.append(" AND installation.deviceType IN :deviceTypes");
            // add the params:
            parameters.put("deviceTypes", deviceTypes);
        }

        // is a category present ?
        if (isListNotEmpty(categories)) {
            jpqlString.append(" AND ( c.name in (:categories))");
            parameters.put("categories", categories);
        }
    }
    /**
     * Checks if the list is empty, and not null
     */
    private static boolean isListNotEmpty(List<?> list) {
        return (list != null && !list.isEmpty());
    }

    private StringBuilder getFindVariantByDeviceTokenQuery() {
    	return new StringBuilder("select installation from Installation installation " +
                " join installation.variant abstractVariant" +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken = :deviceToken");
    }

    /**
     * Transform alias list to lowercase aliases
     */
    private static List<String> getAliases(List<String> aliases){
    	if (!isListNotEmpty(aliases)){
    		return aliases;
    	}

    	List<String> lowerAliases = new ArrayList<>();

    	for (String alias: aliases){
    		lowerAliases.add(alias.toLowerCase());
    	}

    	return lowerAliases;
    }

	@Override
	public List<Installation> findInstallationsByAlias(String alias) {
		StringBuilder queryString = new StringBuilder("select installation from Installation installation " +
                " where installation.alias = :alias");

		return createQuery(queryString.toString()).setParameter("alias", alias).getResultList();
	}

}
