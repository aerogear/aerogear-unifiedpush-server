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
package org.jboss.aerogear.unifiedpush.service.impl;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.jpa.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;

public class GenericVariantServiceImpl implements GenericVariantService {

    @Inject
    private VariantDao variantDao;

    @Override
    public Variant addVariant(Variant variant) {
        return variantDao.create(variant);
    }

    @Override
    public Variant findByVariantID(String variantID) {
        return variantDao.findByVariantID(variantID);
    }

    @Override
    public Variant findByVariantIDForDeveloper(String variantID, String loginName) {
        return variantDao.findByVariantIDForDeveloper(variantID, loginName);
    }

    @Override
    public void addInstallation(Variant variant, InstallationImpl installation) {

        variant.getInstallations().add(installation);
        variantDao.update(variant);
    }

    @Override
    public Variant updateVariant(Variant variant) {
        return variantDao.update(variant);
    }

    @Override
    public void removeVariant(Variant variant) {
        variantDao.delete(variant);
    }
}
