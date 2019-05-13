/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test.archive;

import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoryDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.keycloak.KeycloakSecurityContext;

/**
 * An org.jboss.aerogear.unifiedpush.test.archive for specifying Arquillian micro-deployments with selected parts of UPS
 */
public class UnifiedPushArchiveImpl extends UnifiedPushArchiveBase {

    private PomEquippedResolveStage resolver;

    public UnifiedPushArchiveImpl(Archive<?> delegate) {
        super(delegate);
        resolver = Maven.resolver().loadPomFromFile("pom.xml");

        addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Override
    public UnifiedPushArchive addMavenDependencies(String... deps) {
        return addAsLibraries(resolver.resolve(deps).withTransitivity().asFile());
    }


    @Override
    public UnifiedPushArchive withApi() {
        return addPackage(org.jboss.aerogear.unifiedpush.api.PushApplication.class.getPackage());
    }

    @Override
    public UnifiedPushArchive withUtils() {
        return addClasses(ConfigurationUtils.class);
    }

    @Override
    public UnifiedPushArchive withMessageModel() {
        return addMavenDependencies("com.fasterxml.jackson.core:jackson-databind");
    }

    @Override
    public UnifiedPushArchive withDAOs() {
        return addPackage(org.jboss.aerogear.unifiedpush.dao.PushApplicationDao.class.getPackage())
                .addPackage("org.jboss.aerogear.unifiedpush.jpa.dao.impl")
                .addPackage("org.jboss.aerogear.unifiedpush.api")
                .addPackage("org.jboss.aerogear.unifiedpush.api.dao")
                .addClass(EntityFactory.class)
                .addClass(JPAVariantDao.class)
                .addClass(JPAPushApplicationDao.class)
                .addClass(JPACategoryDao.class)
                .addAsManifestResource("META-INF/persistence.xml")
                .addPackage(org.jboss.aerogear.unifiedpush.dto.Count.class.getPackage());
    }

    @Override
    public UnifiedPushArchive withServices() {
        return addPackage(org.jboss.aerogear.unifiedpush.service.PushApplicationService.class.getPackage())
                .addPackage(SearchManager.class.getPackage())
                .addPackage(HealthDetails.class.getPackage())
                .addPackage(DashboardData.class.getPackage())
                .addMavenDependencies("org.keycloak:keycloak-core");
    }

    @Override
    public UnifiedPushArchive withMockito() {
        return addMavenDependencies("org.mockito:mockito-core");
    }
}
