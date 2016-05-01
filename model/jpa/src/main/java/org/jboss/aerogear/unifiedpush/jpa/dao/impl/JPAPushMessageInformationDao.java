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


import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantMetricInformationDao;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class JPAPushMessageInformationDao extends JPABaseDao<PushMessageInformation, String> implements PushMessageInformationDao {

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private final AeroGearLogger logger = AeroGearLogger.getInstance(JPAPushMessageInformationDao.class);


    @Override
    public List<PushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending) {
        String sQ;
        if (ascending)
        {
            sQ = String.format("db.push_message_info.find( { 'id' : '%s'}, '$orderby': { 'submitDate' : 1 }  )", pushApplicationId);
        }
        else
        {
            sQ = String.format("db.push_message_info.find( { 'id' : '%s'}, '$orderby': { 'submitDate' : -1 }  )", pushApplicationId);
        }
        return entityManager.createNativeQuery(sQ).getResultList();

        /*return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId ORDER BY pmi.submitDate " + ascendingOrDescending(ascending))
                .setParameter("pushApplicationId", pushApplicationId).getResultList();*/
    }

    @Override
    public long getNumberOfPushMessagesForPushApplication(String pushApplicationId) {
        String qS = "db.push_message_information.count( {'pushApplicationId' : '%s' })";
        return  (Long) entityManager.createNativeQuery(String.format(qS,pushApplicationId)).getSingleResult();
        /*return createQuery("select count(*) from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId", Long.class)
                .setParameter("pushApplicationId", pushApplicationId).getSingleResult();*/
    }

    @Override
    public long getNumberOfPushMessagesForVariant(String variantID) {
        String qS = "db.variant_metric_information.count( {'variantID' : '%s' })";
        return  (Long) entityManager.createNativeQuery(String.format(qS,variantID)).getSingleResult();
        /*return createQuery("select count(*) from VariantMetricInformation vmi where vmi.variantID = :variantID", Long.class)
                .setParameter("variantID", variantID).getSingleResult();*/
    }

    @Override
    public PageResult<PushMessageInformation, MessageMetrics> findAllForPushApplication(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize) {

        String baseQuery = "from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId";
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

        return new PageResult<PushMessageInformation, MessageMetrics>(pushMessageInformationList, messageMetrics);
    }

    @Override
    public long getNumberOfPushMessagesForLoginName(String loginName) {

        String sQ = String.format("db.push_message_information.find( { 'developer': '%s' }, {'id' : 1} )",loginName);
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
        String sQ2 = String.format("db.push_message_info.count( { 'id': { '$in': [%s]}})",sb.toString());
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

        String sQ2 = String.format("db.variant_metric_info.find( { 'variantID': { '$in': [%s]} }, {'id' : 1} )",sb.toString());
        return entityManager.createNativeQuery(sQ2).getResultList();
        /*return createQuery("select distinct vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.variantID IN (select t.variantID from Variant t where t.developer = :developer)" +
                " and vmi.deliveryStatus = false", String.class)
                .setParameter("developer", loginName)
                .getResultList();*/
    }

    @Override
    public List<PushMessageInformation> findLatestActivity(String loginName, int maxResults) {
        return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId" +
                " IN (select p.pushApplicationID from PushApplication p where p.developer = :developer)" +
                " ORDER BY pmi.submitDate " + DESC)
                .setParameter("developer", loginName)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Override
    public void deletePushInformationOlderThan(Date oldest) {
        // TODO: use criteria API...

        String sQ = String.format("db.push_message_info.find( { 'submitDate': {'$lt' : '%s'} }, {'id': 1})",oldest);
        List<String> l = entityManager.createNativeQuery(sQ).getResultList();

        StringBuilder sb = new StringBuilder();
        Iterator<String> i = l.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()).append("'");

            if (i.hasNext())
            {
                sb.append(",");
            }
        }


        String sQ2 = String.format("db.variant_metric_info.find( { 'pushMessageInformation.id': { '$in': [%s]} })");
        List<VariantMetricInformation> vml = entityManager.createNativeQuery(sQ2).getResultList();

        VariantMetricInformationDao vmd = new JPAVariantMetricInformationDao();
        for (VariantMetricInformation vmi : vml)
        {
            vmd.delete(vmi);
        }

        String sQ3 = String.format("db.push_message_info.find( { 'submitDate': '%s' })",oldest);
        int affectedRows = entityManager.createNativeQuery(sQ3).getMaxResults();

        /*
        entityManager.createQuery("delete from VariantMetricInformation vmi where vmi.pushMessageInformation.id in (select pmi FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest)")
                .setParameter("oldest", oldest)
                .executeUpdate();

        int affectedRows = entityManager.createQuery("delete FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest")
                .setParameter("oldest", oldest)
                .executeUpdate();
        */

        logger.info("Deleting ['" + affectedRows + "'] outdated PushMessageInformation objects");
    }

    //Admin queries
    @Override
    public List<String> findVariantIDsWithWarnings() {
        String qS = "db.variant_metric_information.find( {'deliveryStatus' : false}, {variantID : 1} )";
        return entityManager.createNativeQuery(String.format(qS)).getResultList();
        /*return createQuery("select distinct vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.deliveryStatus = false", String.class)
                .getResultList();*/
    }

    @Override
    public List<PushMessageInformation> findLatestActivity(int maxResults) {
        String qS = "db.push_message_information.find( {}, $orderby: { 'submitDate' : -1 } )";
        return entityManager.createNativeQuery(String.format(qS)).setMaxResults(maxResults).getResultList();
        /*return createQuery("select pmi from PushMessageInformation pmi" +
                " ORDER BY pmi.submitDate " + DESC)
                .setMaxResults(maxResults)
                .getResultList();*/
    }

    @Override
    public long getNumberOfPushMessagesForApplications() {
        String qS = "db.push_message_information.count( {})";
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
