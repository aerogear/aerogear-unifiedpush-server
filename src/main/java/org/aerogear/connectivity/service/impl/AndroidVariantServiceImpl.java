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

import org.aerogear.connectivity.jpa.dao.AndroidVariantDao;
import org.aerogear.connectivity.model.AndroidVariant;
import org.aerogear.connectivity.service.AndroidVariantService;

public class AndroidVariantServiceImpl implements AndroidVariantService {

    @Inject
    private AndroidVariantDao androidApplicationDao;

    @Override
    public AndroidVariant addAndroidVariant(AndroidVariant app) {
        return androidApplicationDao.create(app);
    }

    @Override
    public List<AndroidVariant> findAllAndroidVariants() {
        return androidApplicationDao.findAll();
    }

    @Override
    public AndroidVariant findByVariantID(String variantID) {
        return androidApplicationDao.findByVariantID(variantID);
    }

    @Override
    public AndroidVariant updateAndroidVariant(
            AndroidVariant androidApp) {
        return androidApplicationDao.update(androidApp);
    }

    @Override
    public void removeAndroidVariant(AndroidVariant androidApp) {
        androidApplicationDao.delete(androidApp);
    }

}
