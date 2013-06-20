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

package org.jboss.aerogear.connectivity.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.connectivity.jpa.dao.MobileVariantInstanceDao;
import org.jboss.aerogear.connectivity.model.MobileVariantInstanceImpl;
import org.jboss.aerogear.connectivity.service.MobileVariantInstanceService;

public class MobileVariantInstanceServiceImpl implements MobileVariantInstanceService {

    @Inject
    private MobileVariantInstanceDao dao;

    public MobileVariantInstanceImpl addMobileVariantInstance(MobileVariantInstanceImpl mobileApplicationInstance) {
        return dao.create(mobileApplicationInstance);
    }

    @Override
    public List<MobileVariantInstanceImpl> findAllMobileVariantInstancesByToken(String token) {
        return dao.findByToken(token);
    }

    @Override
    public void removeMobileVariantInstances(
            List<MobileVariantInstanceImpl> instances) {

        // uh... :)
        
        for (MobileVariantInstanceImpl mobileApplicationInstance : instances) {
            dao.delete(mobileApplicationInstance);
        }
    }

    @Override
    public MobileVariantInstanceImpl updateMobileVariantInstance(
            MobileVariantInstanceImpl mobileApplicationInstance) {
        return dao.update(mobileApplicationInstance);
    }
}
