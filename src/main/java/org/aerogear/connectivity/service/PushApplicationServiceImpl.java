/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.aerogear.connectivity.service;

import java.util.List;

import javax.inject.Inject;

import org.aerogear.connectivity.jpa.dao.PushApplicationDao;
import org.aerogear.connectivity.model.AndroidApplication;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.SimplePushApplication;
import org.aerogear.connectivity.model.iOSApplication;

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
    
    @Override
    public PushApplication findPushApplicationById(String id) {
        return pushApplicationDao.find(PushApplication.class, id);
    }

    @Override
    public void addiOSApplication(PushApplication pushApp, iOSApplication iOSapp) {
        pushApp.getIOSApps().add(iOSapp);
        //pushApp.getMobileApplications().add(iOSapp);
        pushApplicationDao.update(pushApp);
    }

    @Override
    public void addAndroidApplication(PushApplication pushApp, AndroidApplication androidApp) {
        pushApp.getAndroidApps().add(androidApp);
        //pushApp.getMobileApplications().add(androidApp);
        pushApplicationDao.update(pushApp);
    }
    @Override
    public void addSimplePushApplication(PushApplication pushApp,
            SimplePushApplication simplePushApp) {
        pushApp.getSimplePushApps().add(simplePushApp);
        
        pushApplicationDao.update(pushApp);
    }

    @Override
    public List<iOSApplication> alliOSApplicationsForPushApplication(
            PushApplication pushApp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AndroidApplication> allAndroidApplicationsForPushApplication(
            PushApplication pushApp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SimplePushApplication> allSimplePushApplicationsForPushApplication(
            PushApplication pushApp) {
        // TODO Auto-generated method stub
        return null;
    }
}
