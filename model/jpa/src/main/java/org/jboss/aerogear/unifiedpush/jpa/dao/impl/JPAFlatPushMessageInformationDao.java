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

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

public class JPAFlatPushMessageInformationDao extends JPABaseDao<FlatPushMessageInformation, String> implements FlatPushMessageInformationDao{

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private static final Logger logger = LoggerFactory.getLogger(JPAFlatPushMessageInformationDao.class);


    @Override
    public List<FlatPushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending) {
        return findAllForPushApplicationByParams(pushApplicationId, null, ascending, null, null);
    }

    @Override
    public List<FlatPushMessageInformation> findAllForPushApplicationByParams(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize) {
        String baseQuery = "from FlatPushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId";
        if (search != null) {
            baseQuery += " AND pmi.rawJsonMessage LIKE :search";
        }
        final String queryJPQL = "select pmi " + baseQuery + " ORDER BY pmi.submitDate " + ascendingOrDescending(ascending);

        TypedQuery<FlatPushMessageInformation> typedQuery = createQuery(queryJPQL)
                .setParameter("pushApplicationId", pushApplicationId);
        if (search != null) {
            typedQuery.setParameter("search", "%" + search + "%");
        }
        if (pageSize != null) {
            typedQuery.setFirstResult(page * pageSize).setMaxResults(pageSize);
        }

        return typedQuery.getResultList();
    }

    @Override
    public long getNumberOfPushMessagesForPushApplication(String pushApplicationId) {
        return createQuery("select count(*) from FlatPushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId", Long.class)
                .setParameter("pushApplicationId", pushApplicationId).getSingleResult();
    }

    @Override
    public MessageMetrics findMessageMetricsForPushApplicationByParams(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize) {
        String metricsJPQL = "select new org.jboss.aerogear.unifiedpush.dto.MessageMetrics(count(*), sum(appOpenCounter)) from FlatPushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId";
        if (search != null) {
            metricsJPQL += " AND pmi.rawJsonMessage LIKE :search";
        }

        final Query metricsQuery = createUntypedQuery(metricsJPQL).setParameter("pushApplicationId", pushApplicationId);
        if (search != null) {
            metricsQuery.setParameter("search", "%" + search + "%");
        }

        return (MessageMetrics) metricsQuery.getSingleResult();
    }

    @Override
    public PageResult<FlatPushMessageInformation, MessageMetrics> findAllForPushApplication(String pushApplicationId, String search, boolean ascending, Integer page, Integer pageSize) {

        final List<FlatPushMessageInformation> pushMessageInformationList = findAllForPushApplicationByParams(pushApplicationId, search, ascending, page, pageSize);
        final MessageMetrics messageMetrics = findMessageMetricsForPushApplicationByParams(pushApplicationId, search, ascending, page, pageSize);

        return new PageResult<>(pushMessageInformationList,  messageMetrics);
    }

    @Override
    public long getNumberOfPushMessagesForLoginName(String loginName) {
        return createQuery("select count(pmi) from FlatPushMessageInformation pmi, PushApplication pa " +
                "where pmi.pushApplicationId = pa.pushApplicationID and pa.developer = :developer)", Long.class)
                .setParameter("developer", loginName).getSingleResult();
    }

    @Override
    public List<String> findVariantIDsWithWarnings(String loginName) {

        return createQuery("select distinct vmi.variantID from VariantErrorStatus vmi " +
            " WHERE vmi.variant.developer = :developer)", String.class)
            .setParameter("developer", loginName)
            .getResultList();
    }

    @Override
    public List<FlatPushMessageInformation> findLatestActivity(String loginName, int maxResults) {
        return createQuery("select pmi from FlatPushMessageInformation pmi, PushApplication pa" +
                " WHERE pmi.pushApplicationId = pa.pushApplicationID AND pa.developer = :developer)" +
                " ORDER BY pmi.submitDate " + DESC)
                .setParameter("developer", loginName)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Override
    public void deletePushInformationOlderThan(Date oldest) {
        // TODO: use criteria API...
        entityManager.createQuery("delete from VariantErrorStatus vmi where vmi.pushMessageInformation.id in (select pmi FROM FlatPushMessageInformation pmi WHERE pmi.submitDate < :oldest)")
                .setParameter("oldest", oldest)
                .executeUpdate();

        int affectedRows = entityManager.createQuery("delete FROM FlatPushMessageInformation pmi WHERE pmi.submitDate < :oldest")
                .setParameter("oldest", oldest)
                .executeUpdate();

        logger.info("Deleting ['" + affectedRows + "'] outdated FlatPushMessageInformation objects");
    }

    //Admin queries
    @Override
    public List<String> findVariantIDsWithWarnings() {
        return createQuery("select distinct vmi.variantID from VariantErrorStatus vmi", String.class)
                .getResultList();
    }

    @Override
    public List<FlatPushMessageInformation> findLatestActivity(int maxResults) {
        return createQuery("select pmi from FlatPushMessageInformation pmi" +
                " ORDER BY pmi.submitDate " + DESC)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Override
    public long getNumberOfPushMessagesForApplications() {
        return createQuery("select count(pmi) from FlatPushMessageInformation pmi", Long.class).getSingleResult();
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
    public Class<FlatPushMessageInformation> getType() {
        return FlatPushMessageInformation.class;
    }
}
