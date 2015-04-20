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


import java.util.Date;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;


public class JPAPushMessageInformationDao extends JPABaseDao<PushMessageInformation, String> implements PushMessageInformationDao {

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private final AeroGearLogger logger = AeroGearLogger.getInstance(JPAPushMessageInformationDao.class);


    @Override
    public List<PushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending) {
        return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId ORDER BY pmi.submitDate " + ascendingOrDescending(ascending))
                .setParameter("pushApplicationId", pushApplicationId).getResultList();
    }

    @Override
    public long countMessagesForPushApplication(String pushApplicationId) {
        return createQuery("select count(*) from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId", Long.class)
                .setParameter("pushApplicationId", pushApplicationId).getSingleResult();
    }

    @Override
    public long countMessagesForVariant(String variantId) {
        return createQuery("select count(*) from PushMessageInformation pmi JOIN pmi.variantInformations vi where vi.variantID = :variantId", Integer.class)
                .setParameter("variantId", variantId).getSingleResult();
    }

    @Override
    public PageResult<PushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending, Integer page, Integer pageSize) {
        final String query = "select pmi from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId ORDER BY pmi.submitDate " + ascendingOrDescending(ascending);
        final String countQuery = "select count(*) from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId";
        return executePagedQuery(pushApplicationId, "pushApplicationId", page, pageSize, query, countQuery);
    }

    @Override
    //TODO sub optimal fetch join doesn't work well with min max all results will get fetched and min max will be in memory
    public PageResult<PushMessageInformation> findAllForVariant(String variantID, boolean ascending, Integer page, Integer pageSize) {
        final String query = "select pmi from PushMessageInformation pmi JOIN fetch pmi.variantInformations vi where vi.variantID = :variantID ORDER BY pmi.submitDate " + ascendingOrDescending(ascending);
        final String countQuery = "select count(*) from PushMessageInformation pmi JOIN pmi.variantInformations vi where vi.variantID = :variantID";
        return executePagedQuery(variantID, "variantID", page, pageSize, query, countQuery);
    }

    private PageResult<PushMessageInformation> executePagedQuery(String param, String paramName, Integer page, Integer pageSize, String query, String countQuery) {
        List<PushMessageInformation> pushMessageInformationList = createQuery(query)
                .setParameter(paramName, param)
                .setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();

        Long count = createQuery(countQuery, Long.class).setParameter(paramName, param).getSingleResult();

        return new PageResult<PushMessageInformation>(pushMessageInformationList, count);
    }

    @Override
    public long getNumberOfPushMessagesForApplications(String loginName) {
        return createQuery("select count(pmi) from PushMessageInformation pmi where pmi.pushApplicationId " +
                "IN (select p.pushApplicationID from PushApplication p where p.developer = :developer)", Long.class)
                .setParameter("developer", loginName).getSingleResult();
    }

    @Override
    public List<String> findVariantIDsWithWarnings(String loginName) {
        return createQuery("select distinct vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.variantID IN (select t.variantID from Variant t where t.developer = :developer)" +
                " and vmi.deliveryStatus = false", String.class)
                .setParameter("developer", loginName)
                .getResultList();
    }

    @Override
    public List<PushMessageInformation> findLastThreeActivity(String loginName) {
        return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId" +
                " IN (select p.pushApplicationID from PushApplication p where p.developer = :developer)" +
                " ORDER BY pmi.submitDate " + DESC)
                .setParameter("developer", loginName)
                .setMaxResults(3)
                .getResultList();
    }

    @Override
    public void deletePushInformationOlderThan(Date oldest) {
        // TODO: use criteria API...
        entityManager.createQuery("delete from VariantMetricInformation vmi where vmi.pushMessageInformation.id in (select pmi FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest)")
                .setParameter("oldest", oldest)
                .executeUpdate();

        int affectedRows = entityManager.createQuery("delete FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest")
                .setParameter("oldest", oldest)
                .executeUpdate();

        logger.info("Deleting ['" + affectedRows + "'] outdated PushMessageInformation objects");
    }

    //Admin queries
    @Override
    public List<String> findVariantIDsWithWarnings() {
        return createQuery("select distinct vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.variantID IN (select t.variantID from Variant t)" +
                " and vmi.deliveryStatus = false", String.class)
                .getResultList();
    }

    @Override
    public List<PushMessageInformation> findLastThreeActivity() {
        return createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId" +
                " IN (select p.pushApplicationID from PushApplication p)" +
                " ORDER BY pmi.submitDate " + DESC)
                .setMaxResults(3)
                .getResultList();
    }

    @Override
    public long getNumberOfPushMessagesForApplications() {
        return createQuery("select count(pmi) from PushMessageInformation pmi where pmi.pushApplicationId " +
                "IN (select p.pushApplicationID from PushApplication p)", Long.class).getSingleResult();
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
