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

import org.jboss.aerogear.unifiedpush.message.AbstractJMSTest;
import org.jboss.aerogear.unifiedpush.message.Config;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.HealthNetworkService;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageConsumer;
import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageListener;
import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageProducer;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

/**
 * An archive for specifying Arquillian micro-deployments with selected parts of UPS
 */
public class UnifiedPushSenderArchive extends UnifiedPushArchiveBase<UnifiedPushSenderArchive> {

    public UnifiedPushSenderArchive(Archive<?> delegate) {
        super(UnifiedPushSenderArchive.class, delegate);
 
        addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    public static UnifiedPushSenderArchive forTestClass(Class<?> clazz) {
        return ShrinkWrap.create(UnifiedPushSenderArchive.class, String.format("%s.war", clazz.getSimpleName()));
    }
    
    public UnifiedPushSenderArchive withMessaging() {
        return withApi()
            .withUtils()
            .withMessageModel()
            .withDAOs()
            .withServices()
            .addPackage(org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent.class.getPackage())
            .addPackage(org.jboss.aerogear.unifiedpush.message.holder.AbstractMessageHolder.class.getPackage())
            .addPackage(org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException.class.getPackage())
            .addClasses(AbstractJMSMessageProducer.class, AbstractJMSMessageListener.class, AbstractJMSMessageConsumer.class)
            .addClasses(AbstractJMSTest.class, JmsClient.class)
            .addClasses(DispatchToQueue.class, Dequeue.class)
            .addAsWebInfResource("hornetq-jms.xml");
    }

    @Override
    public UnifiedPushSenderArchive withApi() {
        return addPackage(org.jboss.aerogear.unifiedpush.api.PushApplication.class.getPackage());
    }

    public UnifiedPushSenderArchive withUtils() {
        return addPackage(org.jboss.aerogear.unifiedpush.utils.AeroGearLogger.class.getPackage())
                .addClasses(ConfigurationUtils.class);
    }

    public UnifiedPushSenderArchive withMessageModel() {
        return addClasses(UnifiedPushMessage.class, InternalUnifiedPushMessage.class, Config.class, Criteria.class, Message.class)
                .addPackage(org.jboss.aerogear.unifiedpush.message.windows.Windows.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.message.apns.APNs.class.getPackage())
                .addMavenDependencies("org.codehaus.jackson:jackson-mapper-asl");
    }
    
	public UnifiedPushSenderArchive withAllServices() {
		return addPackages(true, HealthNetworkService.class.getPackage()); 
	}
	
    @Override
    public UnifiedPushSenderArchive withDAOs() {
        return addPackage(org.jboss.aerogear.unifiedpush.dao.PushApplicationDao.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.dto.Count.class.getPackage());
    }
}
