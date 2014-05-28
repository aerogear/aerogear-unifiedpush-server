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
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JPAPushMessageInformationDao extends JPABaseDao implements PushMessageInformationDao {

    private final Logger logger = Logger.getLogger(JPAPushMessageInformationDao.class.getName());

    @Override
    public List<PushMessageInformation> findAllForPushApplication(String pushApplicationId) {

        List<PushMessageInformation> messageInformations = createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId")
                .setParameter("pushApplicationId", pushApplicationId).getResultList();

        return messageInformations;
    }

    @Override
    public List<PushMessageInformation> findAllForVariant(String variantID) {
        List<PushMessageInformation> messageInformations = createQuery("select pmi from PushMessageInformation pmi JOIN pmi.variantInformations vi where vi.variantID = :variantID")
                .setParameter("variantID", variantID).getResultList();

        return messageInformations;
    }

    @Override
    public long getNumberOfPushMessagesForApplications(List<String> pushApplicationIds) {
        return (Long) createQuery("select count(pmi) from PushMessageInformation pmi where pmi.pushApplicationId IN :pushApplicationIds")
                .setParameter("pushApplicationIds", pushApplicationIds).getSingleResult();
    }

    @Override
    public PushMessageInformation find(String id) {
        return entityManager.find(PushMessageInformation.class, id);
    }

    @Override
    public void create(PushMessageInformation pushMessageInformation) {
        persist(pushMessageInformation);
    }

    @Override
    public void update(PushMessageInformation pushMessageInformation) {
        merge(pushMessageInformation);
    }

    @Override
    public void delete(PushMessageInformation pushMessageInformation) {
        PushMessageInformation entity = find(pushMessageInformation.getId())  ;
        remove(entity);
    }

    @Override
    public void deletePushInformationOlderThan(Date oldest) {

        // TODO: use criteria API...
        List<PushMessageInformation> oldMessages = createQuery("select pmi FROM PushMessageInformation pmi WHERE pmi.submitDate < :oldest")
                .setParameter("oldest", oldest).getResultList();

        logger.log(Level.INFO, "Deleting ['" + oldMessages.size() + "'] outdated PushMessageInformation objects");

        for (PushMessageInformation oldMessage : oldMessages) {
            remove(oldMessage);
        }
    }
}
