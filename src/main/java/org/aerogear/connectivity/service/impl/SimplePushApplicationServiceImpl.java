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

import org.aerogear.connectivity.jpa.dao.SimplePushApplicationDao;
import org.aerogear.connectivity.model.SimplePushApplication;
import org.aerogear.connectivity.service.SimplePushApplicationService;

public class SimplePushApplicationServiceImpl implements
        SimplePushApplicationService {

    @Inject
    private SimplePushApplicationDao simplePushApplicationDao;
    
    @Override
    public SimplePushApplication addSimplePushApplication(
            SimplePushApplication spa) {
        return simplePushApplicationDao.create(spa);
    }

    @Override
    public List<SimplePushApplication> findAllSimplePushApplications() {
        return simplePushApplicationDao.findAll();
    }

    @Override
    public SimplePushApplication findByVariantID(String variantID) {
        return simplePushApplicationDao.findByVariantID(variantID);
    }

    @Override
    public SimplePushApplication updateSimplePushApplication(
            SimplePushApplication spa) {
        return simplePushApplicationDao.update(spa);
    }

    @Override
    public void removeSimplePushApplication(SimplePushApplication spa) {
        simplePushApplicationDao.delete(spa);
    }
}
