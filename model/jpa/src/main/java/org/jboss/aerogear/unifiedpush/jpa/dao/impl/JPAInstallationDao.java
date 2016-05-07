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

import com.mongodb.*;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.dto.Count;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.*;

public class JPAInstallationDao extends JPABaseDao<Installation, String> implements InstallationDao {

    @Inject
    JPACategoryDao categoryDao;

    public PageResult<Installation, Count> findInstallationsByVariantForDeveloper(String variantID, String developer, Integer page, Integer pageSize, String search) {


        StringBuilder qBase = new StringBuilder("{ $query : { 'variant_id' : '%s' ");

        ArrayList<String> parameters = new ArrayList<String>();
        parameters.add(variantID);

        if (developer != null) {
            // fetch developer from variant
            // compare if it equals to variantID
            String qString = String.format("db.variant.count( {'_id': '%s', 'developer' : '%s'})",variantID,developer);
            long devs = (Long) entityManager.createNativeQuery(qString).getSingleResult();

            if (devs == 0)
            {
                return new PageResult<Installation, Count>(new ArrayList<Installation>(), new Count(0L));
            }

        }
        if (search != null) {
            qBase.append(", $or: [ " +
                    "{ 'device_token' : {'$regex': '%s'} }," +
                    " { 'device_type' : {'$regex': '%s'} }," +
                    " { 'platform' : {'$regex': '%s'} }," +
                    " { 'operating_system' : {'$regex': '%s'} }," +
                    " { 'os_version' : {'$regex': '%s'} }," +
                    " { 'alias' : {'$regex': '%s'} }" +
                    " ]");
            parameters.add(search);
            parameters.add(search);
            parameters.add(search);
            parameters.add(search);
            parameters.add(search);
            parameters.add(search);

            //parameters.put("search", "%" + search + "%");
        }
        qBase.append("} , $orderby: { _id : 1 } }");
        String sqlString = String.format(qBase.toString(), parameters.toArray());


        List<Installation> resultList = createNativeQuery(sqlString)
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        Long count = Long.valueOf(createNativeQuery(sqlString).getResultList().size());

        return new PageResult<Installation, Count>(resultList, new Count(count));

        /*
        final StringBuilder jpqlBase = new StringBuilder(FIND_INSTALLATIONS);
        final Map<String, Object> parameters = new LinkedHashMap<String, Object>();
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

        return new PageResult<Installation, Count>(resultList, new Count(count));
        */
    }

    public PageResult<Installation, Count> findInstallationsByVariant(String variantID, Integer page, Integer pageSize, String search) {
        return findInstallationsByVariantForDeveloper(variantID, null, page, pageSize, search);
    }


    @Override
    public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {

        String sqlString = String.format("db.installation.find({ 'variant_id' : '%s' , 'device_token' : '%s'})", variantID, deviceToken);

        Installation i = getSingleResultForQuery(createNativeQuery(sqlString));
        return i;

        /*return getSingleResultForQuery(createQuery("select installation from Installation installation " +
                " join installation.variant abstractVariant" +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken = :deviceToken")
                .setParameter("variantID", variantID)
                .setParameter("deviceToken", deviceToken));*/
    }

    @Override
    public List<Installation> findInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
        // if there are no device-tokens, no need to bug the database
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            // be nice and return an empty list...
            return Collections.emptyList();
        }

        StringBuilder dT = new StringBuilder();
        Iterator<String> iter = deviceTokens.iterator();

        while (iter.hasNext()) {
            dT.append("'")
            .append(iter.next())
            .append("'");

            if (iter.hasNext())
                dT.append(",");
        }


        String dTs = dT.toString();

        String sqlString =  String.format("{ $query : { 'variant_id' : '%s' , 'device_token' : { $in : [%s] } } }", variantID, dTs);
        return createNativeQuery(sqlString).getResultList();

        /*return createQuery("select installation from Installation installation " +
                " join installation.variant abstractVariant " +
                " where abstractVariant.variantID = :variantID" +
                " and installation.deviceToken IN :deviceTokens")
                .setParameter("variantID", variantID)
                .setParameter("deviceTokens", deviceTokens)
                .getResultList();*/
    }



    @Override
    public Set<String> findAllDeviceTokenForVariantID(String variantID) {

        String sqlString = String.format("{ $query :  { 'variant_id' : '%s', 'enabled' : true} }, {deviceToken: 1}", variantID);

        //TypedQuery<String> query = createQuery(FIND_ALL_DEVICES_FOR_VARIANT_QUERY, String.class);
        //query.setParameter("variantID", variantID);

        // possible bottleneck, ogm doesnt allow to fetch just part of document
        List<Installation> installationList = createNativeQuery(sqlString).getResultList();

        HashSet<String> result = new HashSet<String>();

        for(Installation i : installationList)
        {
            result.add(i.getDeviceToken());
        }

        return result;
    }

    @Override
    public ResultsStream.QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes, final int maxResults, String lastTokenFromPreviousBatch) {
        // the required part: Join + all tokens for variantID;

        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient( "localhost" , 27017 );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DB db = mongoClient.getDB( "unifiedpush" );

        DBCollection installation = db.getCollection( "installation" );

        ArrayList andList = new ArrayList();

        BasicDBObject query1 = new BasicDBObject("enabled", true);
        andList.add(query1);

        BasicDBObject query2 = new BasicDBObject("variant_id", variantID);
        andList.add(query2);

        final DBCursor cursor = installation.find(new BasicDBObject("$and", andList));


        if (isListNotEmpty(aliases)) {
            BasicDBObject query3 = new BasicDBObject("alias", new BasicDBObject("$in", aliases));
            andList.add(query3);
        }

        // are devices present ??
        if (isListNotEmpty(deviceTypes)) {
            BasicDBObject query4 = new BasicDBObject("device_type", new BasicDBObject("$in", deviceTypes));
            andList.add(query4);
        }

        // is a category present ?
        if (isListNotEmpty(categories)) {
            List<Category> cat = categoryDao.findByNames(categories);

            ArrayList orList = new ArrayList();
            BasicDBObject query5 = new BasicDBObject("$or", orList);
            for (Category c : cat)
            {
                orList.add(new BasicDBObject("categories", c.getId()  ) );

            }

            if (isListNotEmpty(cat) )
            {
                andList.add(query5);
            }
            else
            {
                // query failed so return nothing
                BasicDBObject query7 = new BasicDBObject("enabled", false);
                andList.add(query7);

            }
        }

        // sort on ids so that we can handle paging properly
        if (lastTokenFromPreviousBatch != null) {
            BasicDBObject query6 = new BasicDBObject("device_token", new BasicDBObject("$gt",lastTokenFromPreviousBatch));
            andList.add(query6);
        }


        cursor.sort(new BasicDBObject("device_token", 1));



        return new ResultsStream.QueryBuilder<String>() {
            private Integer fetchSize = null;
            @Override
            public ResultsStream.QueryBuilder<String> fetchSize(int fetchSize) {
                this.fetchSize = fetchSize;
                return this;
            }
            @Override
            public ResultsStream<String> executeQuery() {

                cursor.limit(maxResults);

                // just read
                //q.setReadOnly(true);
                if (fetchSize != null) {
                    cursor.batchSize(fetchSize);
                }
               // final ScrollableResults results = q.scroll(ScrollMode.FORWARD_ONLY);
                return new ResultsStream<String>() {
                    @Override
                    public boolean next() throws ResultStreamException {
                        try {
                            return cursor.hasNext();
                        }
                        catch (Exception e)
                        {
                            throw new ResultStreamException(e);
                        }

                    }
                    @Override
                    public String get() throws ResultStreamException {
                        try {
                            Object dT = cursor.next().get("device_token");
                            if (dT != null)
                            {
                                return String.valueOf(dT);
                            }
                            else
                            {
                                return null;
                            }

                        }
                        catch (Exception e)
                        {
                            throw new ResultStreamException(e);
                        }
                    }
                };
            }

        };

        /*
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
        */
    }

    @Override
    public long getNumberOfDevicesForLoginName(String loginName) {

        String qS = String.format("db.variant.find( { 'developer' : '%s'}, {'_id' : 1} )",loginName);
        List<String> variantsWithIds =  entityManager.createNativeQuery(qS).getResultList();

        StringBuilder sb = new StringBuilder();
        Iterator<String> i = variantsWithIds.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()).append("'");
            if (i.hasNext())
            {
                sb.append(",");
            }
        }

        String qS2 = String.format("db.installation.count( { 'variant_id' : { '$in' : [%s] } })",sb.toString());
        Long numberOfInstallations = (Long)entityManager.createNativeQuery(qS2).getSingleResult();
        return numberOfInstallations;
        /*return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t where t.developer = :developer) ", Long.class)
                .setParameter("developer", loginName).getSingleResult();*/
    }

    @Override
    public Class<Installation> getType() {
        return Installation.class;
    }

    //Admin query
    @Override
    public long getTotalNumberOfDevices() {
        String sqlString = "db.installation.count( {})";
        Long numberOfInstallations = (Long)entityManager.createNativeQuery(sqlString).getSingleResult();
        return numberOfInstallations;
        /*return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID IN (select t.variantID from Variant t) ", Long.class)
                .getSingleResult();*/
    }

    @Override
    public long getNumberOfDevicesForVariantID(String variantId) {

        String qS = String.format("db.installation.count( { 'variant_id' : '%s'})",variantId);
        Long numberOfInstallations = (Long)entityManager.createNativeQuery(qS).getSingleResult();
        return numberOfInstallations;
        /*return createQuery("select count(installation) from Installation installation join installation.variant abstractVariant where abstractVariant.variantID = :variantId ", Long.class)
                .setParameter("variantId", variantId)
                .getSingleResult();*/
    }


    /**
     * Checks if the list is empty, and not null
     */
    private boolean isListNotEmpty(List list) {
        return (list != null && !list.isEmpty());
    }
}
