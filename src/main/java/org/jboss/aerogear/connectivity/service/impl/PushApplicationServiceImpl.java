/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.connectivity.jpa.dao.PushApplicationDao;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.jboss.aerogear.connectivity.service.PushApplicationService;

public class PushApplicationServiceImpl implements PushApplicationService {

    @Inject
    private PushApplicationDao pushApplicationDao;

    @Override
    public PushApplication addPushApplication(PushApplication pushApp) {
        return pushApplicationDao.create(pushApp);
    }

    @Override
    public List<PushApplication> findAllPushApplications() {
        return pushApplicationDao.findAll();
    }

    public List<PushApplication> findAllPushApplicationsForDeveloper(String loginName) {
        return pushApplicationDao.findAllForDeveloper(loginName);
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {
        return pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID, loginName);
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {
        return pushApplicationDao.findByPushApplicationID(pushApplicationID);
    }

    @Override
    public void addiOSVariant(PushApplication pushApp, iOSVariant iOVariant) {
        pushApp.getIOSVariants().add(iOVariant);
        pushApplicationDao.update(pushApp);
    }

    @Override
    public void addAndroidVariant(PushApplication pushApp, AndroidVariant androidVariant) {
        pushApp.getAndroidVariants().add(androidVariant);
        pushApplicationDao.update(pushApp);
    }

    @Override
    public void addSimplePushVariant(PushApplication pushApp,
            SimplePushVariant simplePushVariant) {
        pushApp.getSimplePushVariants().add(simplePushVariant);

        pushApplicationDao.update(pushApp);
    }

    @Override
    public List<iOSVariant> alliOSVariantsForPushApplication(
            PushApplication pushApp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AndroidVariant> allAndroidVariantsForPushApplication(
            PushApplication pushApp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimplePushVariant> allSimplePushVariantsForPushApplication(
            PushApplication pushApp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PushApplication updatePushApplication(PushApplication pushApp) {
        return pushApplicationDao.update(pushApp);
    }

    @Override
    public void removePushApplication(PushApplication pushApp) {
        pushApplicationDao.delete(pushApp);
    }
}
