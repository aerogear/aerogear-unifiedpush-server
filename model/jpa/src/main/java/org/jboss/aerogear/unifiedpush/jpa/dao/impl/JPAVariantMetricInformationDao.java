/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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

package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.VariantMetricInformationDao;


public class JPAVariantMetricInformationDao extends JPABaseDao<VariantMetricInformation, String> implements VariantMetricInformationDao {

    @Override
    public VariantMetricInformation findVariantMetricInformationByVariantID(String variantID, String pushmessageinformationId){
        return getSingleResultForQuery(createQuery(
                "select vmi from VariantMetricInformation vmi where vmi.variantID = :variantId and vmi.pushMessageInformation.id = :pushmessageinformationId")
                .setParameter("variantId", variantID)
                .setParameter("pushmessageinformationId",pushmessageinformationId));

    }

    @Override
    public Class<VariantMetricInformation> getType() {
        return VariantMetricInformation.class;
    }

}
