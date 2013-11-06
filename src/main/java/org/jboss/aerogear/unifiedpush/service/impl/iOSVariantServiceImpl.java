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

import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.jpa.dao.iOSVariantDao;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.service.iOSVariantService;

public class iOSVariantServiceImpl implements iOSVariantService {

    @Inject
    private iOSVariantDao iOSApplicationDao;

    @Override
    public iOSVariant addiOSVariant(iOSVariant iOSApp) {
        return iOSApplicationDao.create(iOSApp);
    }
}
