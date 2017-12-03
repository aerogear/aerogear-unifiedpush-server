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
package org.jboss.aerogear.unifiedpush.service;

import static org.mockito.Mockito.mock;

import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.OtpCodeDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.service.AbstractNoCassandraServiceTest.ServiceTestConfig;
import org.jboss.aerogear.unifiedpush.spring.ServiceConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfig.class, ServiceConfig.class })
public abstract class AbstractNoCassandraServiceTest extends AbstractBaseServiceTest {
	@InjectMocks
	@Autowired
	protected AliasService aliasCrudService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Override
	protected void specificSetup() {
		// TODO Auto-generated method stub
	}

	@Configuration
	static class ServiceTestConfig {
		private AliasDao mockAliasDao = mock(AliasDao.class);
		@SuppressWarnings("unchecked")
		private DocumentDao<DocumentContent, DocumentKey> mockDocumentDao = mock(DocumentDao.class);
		private OtpCodeDao mockOtpCodeDao = mock(OtpCodeDao.class);

		@Bean
		public AliasDao aliasDaoMock() {
			return mockAliasDao;
		}

		@Bean
		public DocumentDao<DocumentContent, DocumentKey> documentDaoMock() {
			return mockDocumentDao;
		}

		@Bean
		public OtpCodeDao aliasOtpCodeDao() {
			return mockOtpCodeDao;
		}
	}

}
