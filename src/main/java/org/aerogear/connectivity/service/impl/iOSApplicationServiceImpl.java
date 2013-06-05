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

package org.aerogear.connectivity.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.aerogear.connectivity.jpa.dao.iOSApplicationDao;
import org.aerogear.connectivity.model.iOSApplication;
import org.aerogear.connectivity.service.iOSApplicationService;

public class iOSApplicationServiceImpl implements iOSApplicationService {

    @Inject
    private iOSApplicationDao iOSApplicationDao;

    @Override
    public iOSApplication addiOSApplication(iOSApplication iOSApp) {
        return iOSApplicationDao.create(iOSApp);
    }

    @Override
    public List<iOSApplication> findAlliOSApplications() {
        return iOSApplicationDao.findAll();
    }

    @Override
    public iOSApplication findByVariantID(String variantID) {
        return iOSApplicationDao.findByVariantID(variantID);
    }

    @Override
    public iOSApplication updateiOSApplication(iOSApplication iOSApp) {
        return iOSApplicationDao.update(iOSApp);
    }

    @Override
    public void removeiOSApplication(iOSApplication iOSApp) {
        iOSApplicationDao.delete(iOSApp);
    }

}
