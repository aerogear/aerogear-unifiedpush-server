/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedIn;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Map;

@Stateless
public class PushApplicationServiceImpl implements PushApplicationService {

    @Inject
    private PushApplicationDao pushApplicationDao;

    @Inject
    @LoggedIn
    private Instance<String> loginName;

    public PushApplicationServiceImpl() {
    }

    @Override
    public void addPushApplication(PushApplication pushApp) {
        final String id = pushApp.getPushApplicationID();

        if (findByPushApplicationID(id) != null) {
            throw new IllegalArgumentException("App ID already exists: " + id);
        }

        try {
            pushApp.setDeveloper(loginName.get());
            pushApplicationDao.create(pushApp);
            pushApplicationDao.flushAndClear();
        } catch (RuntimeException e) {
            throw new RuntimeException("Transaction failed for app with ID: " + id, e);
        }
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {
        return pushApplicationDao.findByPushApplicationID(pushApplicationID);
    }

    @Override
    public void addVariant(PushApplication pushApp, Variant variant) {
        pushApp.getVariants().add(variant);
        pushApplicationDao.update(pushApp);
    }

    @Override
    public Map<String, Long> countInstallationsByType(String pushApplicationID) {
        return pushApplicationDao.countInstallationsByType(pushApplicationID);
    }

    @Override
    public void updatePushApplication(PushApplication pushApp) {
        pushApplicationDao.update(pushApp);
    }

    @Override
    public void removePushApplication(PushApplication pushApp) {
        pushApplicationDao.delete(pushApp);
    }

}
