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

import org.aerogear.connectivity.jpa.dao.AndroidApplicationDao;
import org.aerogear.connectivity.model.AndroidVariant;
import org.aerogear.connectivity.service.AndroidApplicationService;

public class AndroidApplicationServiceImpl implements AndroidApplicationService {

    @Inject
    private AndroidApplicationDao androidApplicationDao;

    @Override
    public AndroidVariant addAndroidApplication(AndroidVariant app) {
        return androidApplicationDao.create(app);
    }

    @Override
    public List<AndroidVariant> findAllAndroidApplications() {
        return androidApplicationDao.findAll();
    }

    @Override
    public AndroidVariant findByVariantID(String variantID) {
        return androidApplicationDao.findByVariantID(variantID);
    }

    @Override
    public AndroidVariant updateAndroidApplication(
            AndroidVariant androidApp) {
        return androidApplicationDao.update(androidApp);
    }

    @Override
    public void removeAndroidApplication(AndroidVariant androidApp) {
        androidApplicationDao.delete(androidApp);
    }

}
