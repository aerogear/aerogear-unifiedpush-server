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

import java.io.File;

import org.jboss.aerogear.unifiedpush.message.AbstractJMSTest;
import org.jboss.aerogear.unifiedpush.message.Config;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.Priority;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.apns.APNs;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.exception.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.holder.AbstractMessageHolder;
import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageListener;
import org.jboss.aerogear.unifiedpush.message.jms.AbstractJMSMessageProducer;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.jms.MessageHolderWithTokensConsumer;
import org.jboss.aerogear.unifiedpush.message.jms.MessageHolderWithTokensProducer;
import org.jboss.aerogear.unifiedpush.message.jms.MessageHolderWithVariantsConsumer;
import org.jboss.aerogear.unifiedpush.message.jms.MessageHolderWithVariantsProducer;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.message.windows.Windows;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

/**
 * An archive for specifying Arquillian micro-deployments with selected parts of
 * UPS
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
		return withServices() //
				.withCassandra() //
				.withAssert() //
				.withModelJPA() //
				.withTestDS() //
				.withTestResources() //
				.withMockito() //
				.withMessageModel() //
				.addPackage(BatchLoadedEvent.class.getPackage()) //
				.addPackage(AbstractMessageHolder.class.getPackage()) //
				.addPackage(MessageDeliveryException.class.getPackage()) //
				.addClasses(AbstractJMSMessageProducer.class, AbstractJMSMessageListener.class) //
				.addClasses(AbstractJMSTest.class, JmsClient.class)//
				.addClasses(DispatchToQueue.class, Dequeue.class);
	}

	public UnifiedPushSenderArchive withMessageDrivenBeans() {
		return addClasses(AbstractJMSMessageListener.class)
				.addClasses(MessageHolderWithVariantsConsumer.class, MessageHolderWithVariantsProducer.class)
				.addClasses(MessageHolderWithTokensConsumer.class, MessageHolderWithTokensProducer.class)
				.addAsWebInfResource(
						new File("../../servers/ups-wildfly/src/main/webapp/WEB-INF/jboss-ejb3.xml"), "jboss-ejb3.xml");
	}

	public UnifiedPushSenderArchive withMessageModel() {
		return addClasses(UnifiedPushMessage.class, InternalUnifiedPushMessage.class, Config.class, Criteria.class,
				Message.class, Priority.class)
						.addPackage(Windows.class.getPackage())
						.addPackage(APNs.class.getPackage());
	}

	@Override
	public UnifiedPushSenderArchive withTestResources() {
		return super.withTestResources().addAsResource("cert/certificate.p12");
	}
}
