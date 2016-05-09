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
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantMetricInformationDao;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.persistence.Query;
import java.net.UnknownHostException;
import java.util.*;


public class JPAPushMessageInformationDao extends JPABaseDao<PushMessageInformation, String> implements PushMessageInformationDao {

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private final AeroGearLogger logger = AeroGearLogger.getInstance(JPAPushMessageInformationDao.class);


    @Override
    public List<PushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending) {
        String sQ;
        if (ascending)
        {
            sQ = String.format("db.push_message_info.find( { '$query' : { 'push_application_id' : '%s'}, '$orderby': { 'submit_date' : 1 } }  )", pushApplicationId);
        }
        else
        {
            sQ = String.format("db.push_message_info.find( { '$query' : { 'push_application_id' : '%s'}, '$orderby': { 'submit_date' : -1 } } )", pushApplicationId);
        }
        return entityManager.createNativeQuery(sQ).getResultList();

        /*return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId ORDER BY pmi.submitDate " + ascendingOrDescending(ascending))
                .setParameter("pushApplicationId", pushApplicationId).getResultList();*/
    }

    @Override
    public long getNumberOfPushMessagesForPushApplication(String pushApplicationId) {
        String qS = String.format("db.push_message_info.count( {'push_application_id' : '%s' })",pushApplicationId);
        return  (Long) entityManager.createNativeQuery(String.format(qS,pushApplicationId)).getSingleResult();
        /*return createQuery("select count(*) from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId", Long.class)
                .setParameter("pushApplicationId", pushApplicationId).getSingleResult();*/
    }

    @Override
    public long getNumberOfPushMessagesForVariant(String variantID) {
        String qS = String.format("db.variant_metric_info.count( {'variant_id' : '%s' })",variantID);
        return  (Long) entityManager.createNativeQuery(String.format(qS,variantID)).getSingleResult();
        /*return createQuery("select count(*) from VariantMetricInformation vmi where vmi.variantID = :variantID", Long.class)
                .setParameter("variantID", variantID).getSingleResult();*/
    }

    @Override
    public PageResult<PushMessageInformation, MessageMetrics> findAllForPushApplication(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize) {
        String sQ;
        String order;
        String search_query = "";
        if (ascending)
        {
            order = "1";
        }
        else
        {
            order = "-1";
        }

        if (search != null) {
            search_query = ", 'raw_json_message': '/" + search + "/'";
        }

        sQ = String.format("db.push_message_info.find( { '$query' : { 'push_application_id' : '%s' %s}, '$orderby': { 'submit_date' : %s } } )"
                , pushApplicationId, search_query , order);


        Query q = entityManager.createNativeQuery(sQ, PushMessageInformation.class);
        q.setFirstResult(page * pageSize).setMaxResults(pageSize);
        List<PushMessageInformation> pushMessageInformationList = q.getResultList();

        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient( "localhost" , 27017 );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        DB db = mongoClient.getDB( "unifiedpush" );
        DBCollection installation = db.getCollection( "push_message_info" );


        DBObject group = BasicDBObjectBuilder.start().push("$group")
                .push("_id").pop()
                .push("count").add("$sum", 1).pop()
                .push("total_receivers").add("$sum", "$total_receivers").pop()
                .push("app_open_counter").add("$sum", "$app_open_counter").get();
        AggregationOutput aggr = installation.aggregate(Arrays.asList(group));
        Iterator<DBObject> i = aggr.results().iterator();

        DBObject o = i.next();

        Integer count = (Integer) o.get("count");
        Long total_receivers = (Long) o.get("total_receivers");
        Long appOpenedCounter = (Long) o.get("app_open_counter");

        MessageMetrics messageMetrics = new MessageMetrics(new Long(count), total_receivers ,appOpenedCounter);

        return new PageResult<PushMessageInformation, MessageMetrics>(pushMessageInformationList, messageMetrics);
        /*String baseQuery = "from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId";
        if (search != null) {
            baseQuery += " AND pmi.rawJsonMessage LIKE :search";
        }
        final String queryJPQL = "select pmi " + baseQuery + " ORDER BY pmi.submitDate " + ascendingOrDescending(ascending);
        final String metricsJPQL = "select new org.jboss.aerogear.unifiedpush.dto.MessageMetrics(count(*), sum(totalReceivers), sum(appOpenCounter)) " + baseQuery;

        TypedQuery<PushMessageInformation> typedQuery = createQuery(queryJPQL)
                .setParameter("pushApplicationId", pushApplicationId);
        if (search != null) {
            typedQuery.setParameter("search", "%" + search + "%");
        }
        typedQuery.setFirstResult(page * pageSize).setMaxResults(pageSize);
        List<PushMessageInformation> pushMessageInformationList = typedQuery.getResultList();

        Query metricsQuery = createUntypedQuery(metricsJPQL).setParameter("pushApplicationId", pushApplicationId);
        if (search != null) {
            metricsQuery.setParameter("search", "%" + search + "%");
        }
        MessageMetrics messageMetrics = (MessageMetrics) metricsQuery.getSingleResult();

        return new PageResult<PushMessageInformation, MessageMetrics>(pushMessageInformationList, messageMetrics);*/
    }

    @Override
    public long getNumberOfPushMessagesForLoginName(String loginName) {

        String sQ = String.format("db.push_application.find( { 'developer': '%s' }, {'api_key' : 1} )",loginName);
        List<Object []> vL = entityManager.createNativeQuery(sQ).getResultList();
        StringBuilder sb = new StringBuilder();
        Iterator<Object []> i = vL.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()[1]).append("'");

            if (i.hasNext())
            {
                sb.append(",");
            }
        }
        String sQ2 = String.format("db.push_message_info.count( { 'push_application_id': { '$in': [%s]}})",sb.toString());
        return (Long) entityManager.createNativeQuery(sQ2).getSingleResult();
        /*return createQuery("select count(pmi) from PushMessageInformation pmi where pmi.pushApplicationId " +
                "IN (select p.pushApplicationID from PushApplication p where p.developer = :developer)", Long.class)
                .setParameter("developer", loginName).getSingleResult();*/
    }

    @Override
    public List<String> findVariantIDsWithWarnings(String loginName) {

        String sQ = String.format("db.variant.find( { 'developer': '%s' }, {'id' : 1} )",loginName);
        List<String> vL = entityManager.createNativeQuery(sQ).getResultList();
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = vL.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()).append("'");

            if (i.hasNext())
            {
                sb.append(",");
            }
        }

        String sQ2 = String.format("db.variant_metric_info.find( { 'variant_id': { '$in': [%s]} }, {'id' : 1} )",sb.toString());
        return entityManager.createNativeQuery(sQ2).getResultList();
        /*return createQuery("select distinct vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.variantID IN (select t.variantID from Variant t where t.developer = :developer)" +
                " and vmi.deliveryStatus = false", String.class)
                .setParameter("developer", loginName)
                .getResultList();*/
    }

    @Override
    public List<PushMessageInformation> findLatestActivity(String loginName, int maxResults) {

        String qS = String.format("db.push_application.find( { 'developer' : '%s'}, {'api_key' : 1} )", loginName);
        List<Object []> ids =  entityManager.createNativeQuery(qS).getResultList();

        StringBuilder sb = new StringBuilder();
        Iterator<Object []> i = ids.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()[1]).append("'");

            if(i.hasNext())
            {
                sb.append(",");
            }
        }


        String qS2 = String.format("db.push_message_info.find( { '$query': { 'push_application_id' : { '$in' : [%s] } }, '$orderby': { 'submit_date' : -1} })",
                sb.toString());
        return entityManager.createNativeQuery(String.format(qS2)).setMaxResults(maxResults).getResultList();
        /*return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId" +
                " IN (select p.pushApplicationID from PushApplication p where p.developer = :developer)" +
                " ORDER BY pmi.submitDate " + DESC)
                .setParameter("developer", loginName)
                .setMaxResults(maxResults)
                .getResultList();*/
    }

    @Override
    public void deletePushInformationOlderThan(Date oldest) {
        // TODO: use criteria API...



        String sQ = String.format("db.push_message_info.find({})");
        List<PushMessageInformation> l = entityManager.createNativeQuery(sQ, PushMessageInformation.class).getResultList();

        StringBuilder sb = new StringBuilder();
        Iterator<PushMessageInformation> i = l.iterator();

        while (i.hasNext())
        {

            PushMessageInformation pmi = (PushMessageInformation) i.next();

            // ogm does not parse date formats
            if (oldest.after(pmi.getSubmitDate()) )
            sb.append("'").append(pmi.getPushApplicationId()).append("'");

            if (i.hasNext())
            {
                sb.append(",");
            }
            entityManager.remove(pmi);
        }


        String sQ2 = String.format("db.variant_metric_info.find( { 'pushMessageInformation_id': { '$in': [%s]} })", sb.toString());
        List<VariantMetricInformation> vml = entityManager.createNativeQuery(sQ2).getResultList();

        VariantMetricInformationDao vmd = new JPAVariantMetricInformationDao();
        for (VariantMetricInformation vmi : vml)
        {
            vmd.delete(vmi);
        }





        /*
        entityManager.createQuery("delete from VariantMetricInformation vmi where vmi.pushMessageInformation.id in (select pmi FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest)")
                .setParameter("oldest", oldest)
                .executeUpdate();

        int affectedRows = entityManager.createQuery("delete FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest")
                .setParameter("oldest", oldest)
                .executeUpdate();
        */

        //logger.info("Deleting ['" + affectedRows + "'] outdated PushMessageInformation objects");
    }

    //Admin queries
    @Override
    public List<String> findVariantIDsWithWarnings() {
        String qS = "db.variant_metric_info.find( {'delivery_status' : false}, {'variant_id' : 1 } )";
        List<Object[]> objects = entityManager.createNativeQuery(String.format(qS)).getResultList();
        List<String> result = new ArrayList<String>();
        for (Object [] o : objects)
        {
            result.add((String) o[1]);
        }
        return result;
        /*return createQuery("select distinct vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.deliveryStatus = false", String.class)
                .getResultList();*/
    }

    @Override
    public List<PushMessageInformation> findLatestActivity(int maxResults) {
        String qS = "db.push_message_info.find( { '$query' : {}, '$orderby': { 'submit_date' : -1 } } )";
        return entityManager.createNativeQuery(String.format(qS)).setMaxResults(maxResults).getResultList();
        /*return createQuery("select pmi from PushMessageInformation pmi" +
                " ORDER BY pmi.submitDate " + DESC)
                .setMaxResults(maxResults)
                .getResultList();*/
    }

    @Override
    public long getNumberOfPushMessagesForApplications() {
        String qS = "db.push_message_info.count( {})";
        return  (Long) entityManager.createNativeQuery(qS).getSingleResult();
        /*return createQuery("select count(pmi) from PushMessageInformation pmi", Long.class).getSingleResult();*/
    }

    /**
     * Helper that returns 'ASC' when true and 'DESC' when false.
     */
    private String ascendingOrDescending(boolean asc) {
        if (asc) {
            return ASC;
        } else {
            return DESC;
        }
    }

    @Override
    public Class<PushMessageInformation> getType() {
        return PushMessageInformation.class;
    }
}
