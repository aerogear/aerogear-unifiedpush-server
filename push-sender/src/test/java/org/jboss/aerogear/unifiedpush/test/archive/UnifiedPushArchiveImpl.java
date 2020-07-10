/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test.archive;

import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoryDao;
import org.jboss.aerogear.unifiedpush.message.*;
import org.jboss.aerogear.unifiedpush.message.jms.*;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;
import org.jboss.aerogear.unifiedpush.service.impl.PushSearchByDeveloperServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.keycloak.KeycloakSecurityContext;

import java.io.File;

/**
 * An archive for specifying Arquillian micro-deployments with selected parts of UPS
 */
public class UnifiedPushArchiveImpl extends UnifiedPushArchiveBase {

    private PomEquippedResolveStage resolver;
    private static final String WEB_RESOURCE_PATH = "src/test/resources/WEB-INF/";

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
    public UnifiedPushArchive withMessaging() {

        return withApi()
                .withUtils()
                .withMessageModel()
                .withDAOs()
                .withServices()
                .addPackage(org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.message.holder.AbstractMessageHolder.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException.class.getPackage())
                .addClasses(AbstractJMSMessageProducer.class, AbstractJMSMessageListener.class)
                .addClasses(AbstractJMSTest.class, JmsClient.class)
                .addClasses(DispatchToQueue.class, Dequeue.class);
    }

    @Override
    public UnifiedPushArchive withMessageDrivenBeans() {
        return addClasses(AbstractJMSMessageListener.class)
                .addClasses(MessageHolderWithVariantsConsumer.class, MessageHolderWithVariantsProducer.class)
                .addClasses(MessageHolderWithTokensConsumer.class, MessageHolderWithTokensProducer.class)
                .addAsWebInfResource(new File(WEB_RESOURCE_PATH + "jboss-ejb3.xml"), "jboss-ejb3.xml");
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
        return addClasses(UnifiedPushMessage.class, InternalUnifiedPushMessage.class, Config.class, Criteria.class, Message.class, Priority.class)
                .addPackage(org.jboss.aerogear.unifiedpush.message.apns.APNs.class.getPackage())
                .addMavenDependencies("com.fasterxml.jackson.core:jackson-databind:2.10.4");
    }

    @Override
    public UnifiedPushArchive withDAOs() {
        return addPackage(org.jboss.aerogear.unifiedpush.dao.PushApplicationDao.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.dto.Count.class.getPackage());
    }

    @Override
    public UnifiedPushArchive forServiceTests() {
        return addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/orm.xml", "META-INF/orm.xml")
                .addAsResource("META-INF/org/jboss/aerogear/unifiedpush/api/Installation.hbm.xml", "org/jboss/aerogear/unifiedpush/api/Installation.hbm.xml")
                .addAsResource("META-INF/org/jboss/aerogear/unifiedpush/api/Category.hbm.xml", "org/jboss/aerogear/unifiedpush/api/Category.hbm.xml")
                .addAsResource("META-INF/org/jboss/aerogear/unifiedpush/api/FlatPushMessageInformation.hbm.xml", "org/jboss/aerogear/unifiedpush/api/FlatPushMessageInformation.hbm.xml")
                .addAsResource("META-INF/org/jboss/aerogear/unifiedpush/api/VariantErrorStatus.hbm.xml", "org/jboss/aerogear/unifiedpush/api/VariantErrorStatus.hbm.xml")
                .addMavenDependencies("org.hibernate:hibernate-core")
                .addMavenDependencies("org.apache.derby:derby")
                .addMavenDependencies("org.keycloak:keycloak-core")
                .addPackage("org.jboss.aerogear.unifiedpush.jpa.dao.impl")
                .addPackage("org.jboss.aerogear.unifiedpush.api")
                .addPackage("org.jboss.aerogear.unifiedpush.api.dao")
                .addPackage(JPACategoryDao.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.dto.Count.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.service.PushApplicationService.class.getPackage())
                .addPackage(PushSearchByDeveloperServiceImpl.class.getPackage())
                .addPackage(DashboardData.class.getPackage())
                .addPackage(KeycloakSecurityContext.class.getPackage())
                .addPackage(HealthDetails.class.getPackage())
                .addPackage(SearchManager.class.getPackage());
    }

    @Override
    public UnifiedPushArchive withServices() {
        return addPackage(org.jboss.aerogear.unifiedpush.service.PushApplicationService.class.getPackage());
    }

    @Override
    public UnifiedPushArchive withMockito() {
        return addMavenDependencies("org.mockito:mockito-core");
    }
}
