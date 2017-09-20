package org.jboss.aerogear.unifiedpush.cassandra.test.integration;

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

/**
 * Base Class to prepare a fixed keyspace to give tests a keyspace context.
 */
@TestExecutionListeners(listeners = CassandraUnitTestClassExecutionListener.class, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@CassandraDataSet(keyspace = "unifiedpush_server", value = "cassandra-test-cql-dataload.cql")
@EmbeddedCassandra
public class FixedKeyspaceCreatingIntegrationTest {

}