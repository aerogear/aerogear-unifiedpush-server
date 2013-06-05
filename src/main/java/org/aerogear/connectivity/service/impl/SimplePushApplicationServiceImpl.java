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
import org.aerogear.connectivity.model.SimplePushVariant;
import org.aerogear.connectivity.service.SimplePushApplicationService;

public class SimplePushApplicationServiceImpl implements
        SimplePushApplicationService {

    @Inject
    private SimplePushApplicationDao simplePushApplicationDao;
    
    @Override
    public SimplePushVariant addSimplePushVariant(
            SimplePushVariant simplePushVariant) {
        return simplePushApplicationDao.create(simplePushVariant);
    }

    @Override
    public List<SimplePushVariant> findAllSimplePushVariants() {
        return simplePushApplicationDao.findAll();
    }

    @Override
    public SimplePushVariant findByVariantID(String variantID) {
        return simplePushApplicationDao.findByVariantID(variantID);
    }

    @Override
    public SimplePushVariant updateSimplePushVariant(
            SimplePushVariant simplePushVariant) {
        return simplePushApplicationDao.update(simplePushVariant);
    }

    @Override
    public void removeSimplePushVariant(SimplePushVariant simplePushVariant) {
        simplePushApplicationDao.delete(simplePushVariant);
    }
}
