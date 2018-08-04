/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.jboss.aerogear.unifiedpush.api.WebInstallation;
import org.jboss.aerogear.unifiedpush.dao.WebInstallationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAWebInstallationDao extends JPABaseDao<WebInstallation, String> implements WebInstallationDao {

    private static final Logger logger = LoggerFactory.getLogger(JPAWebInstallationDao.class);
    
    private static final String FIND_WEB_INSTALLATION_BY_INSTALLATION_ID = "select webInstallation"
            + " from WebInstallation webInstallation"
            + " where webInstallation.installation.id = :installationId";
    
    @Override
    public void removeWebInstallationByInstallationId(String installationId) {
        try{
            TypedQuery<WebInstallation> query = createQuery(FIND_WEB_INSTALLATION_BY_INSTALLATION_ID);
            query.setParameter("installationId", installationId);
            WebInstallation webInstallation = query.getSingleResult();
            delete(webInstallation);            
        } catch(NoResultException e) {
            logger.debug("No WebInstallation found for specified Installation Id.");
        }
    }
      
    @Override
    public List<WebInstallation> findWebInstallationForVariantByDeviceToken(String variantID, List<String> deviceTokens) {

        return createQuery("select webInstallation from WebInstallation webInstallation "
                + " join webInstallation.installation installation "
                + " join installation.variant abstractVariant"
                + " where abstractVariant.variantID = :variantID"
                + " and installation.deviceToken IN :deviceTokens")
                .setParameter("variantID", variantID)
                .setParameter("deviceTokens", deviceTokens)
                .getResultList();
    }    
    
    @Override
    public Class<WebInstallation> getType() {
        return WebInstallation.class;
    }    
    
}
