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

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.aerogear.unifiedpush.spring.ServiceCacheConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

@TestExecutionListeners(listeners = CassandraUnitTestClassExecutionListener.class, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@CassandraDataSet(keyspace = "unifiedpush_server", value = "cassandra-test-cql-dataload.cql")
@EmbeddedCassandra
@ContextConfiguration(classes = { CassandraConfig.class, ServiceCacheConfig.class })
public abstract class AbstractCassandraServiceTest extends AbstractBaseServiceTest {

}
