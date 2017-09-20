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
package org.jboss.aerogear.unifiedpush.message.configuration;

import static org.junit.Assert.assertEquals;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.SenderConfig;
import org.jboss.aerogear.unifiedpush.service.AbstractNoCassandraServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { SenderConfig.class })
public class TestSenderConfigurationProducer extends AbstractNoCassandraServiceTest {

	@Autowired
	private ApplicationContext applicationContext;

	static {
		System.setProperty("aerogear.android.batchSize", "999");
		System.setProperty("aerogear.ios.batchSize", "1");
	}

	@Test
	public void testAndroid() {
		try {
			SenderConfiguration configuration = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
					applicationContext.getAutowireCapableBeanFactory(), SenderConfiguration.class,
					VariantType.ANDROIDQ);
			assertEquals(10, configuration.batchesToLoad());
			assertEquals(999, configuration.batchSize());
		} finally {
			System.clearProperty("aerogear.android.batchSize");
		}
	}

	@Test
	public void testIOS() {
		try {

			SenderConfiguration configuration = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
					applicationContext.getAutowireCapableBeanFactory(), SenderConfiguration.class, VariantType.IOSQ);
			assertEquals(3, configuration.batchesToLoad());
			assertEquals(1, configuration.batchSize());
		} finally {
			System.clearProperty("aerogear.ios.batchSize");
		}
	}
}
