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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.helper.JPATransformHelper;
import org.jboss.aerogear.unifiedpush.model.jpa.PushApplicationEntity;

import javax.persistence.Query;
import java.util.List;

public class JPAPushApplicationDao extends JPABaseDao implements PushApplicationDao {

    @Override
    public PushApplication create(PushApplication pushApplication) {
        PushApplicationEntity entity = JPATransformHelper.toEntity(pushApplication);

        persist(entity);

        return pushApplication;
    }

    @Override
    public void update(PushApplication pushApplication) {
        PushApplicationEntity entity = JPATransformHelper.toEntity(pushApplication);

        merge(entity);
    }

    @Override
    public void delete(PushApplication pushApplication) {
        PushApplicationEntity entity = entityManager.find(PushApplicationEntity.class, pushApplication.getId());
        remove(entity);
    }

    @Override
    public List<PushApplication> findAllForDeveloper(String loginName) {

        List<PushApplicationEntity> entities = createQuery("select pa from " + PushApplicationEntity.class.getSimpleName() + " pa where pa.developer = :developer")
                .setParameter("developer", loginName).getResultList();

        return JPATransformHelper.fromPushApplicationEntityCollection(entities);
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {

        PushApplicationEntity entity = getSingleResultForQuery(createQuery(
                "select pa from " + PushApplicationEntity.class.getSimpleName() + " pa where pa.pushApplicationID = :pushApplicationID and pa.developer = :developer")
                .setParameter("pushApplicationID", pushApplicationID)
                .setParameter("developer", loginName));

        return JPATransformHelper.fromEntity(entity);
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {

        PushApplicationEntity entity = getSingleResultForQuery(createQuery("select pa from " + PushApplicationEntity.class.getSimpleName() + " pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));

        return JPATransformHelper.fromEntity(entity);
    }

    @Override
    public PushApplication find(String id) {
        PushApplicationEntity entity = entityManager.find(PushApplicationEntity.class, id);
        return  JPATransformHelper.fromEntity(entity);
    }

    private PushApplicationEntity getSingleResultForQuery(Query query) {
        List<PushApplicationEntity> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }
}
