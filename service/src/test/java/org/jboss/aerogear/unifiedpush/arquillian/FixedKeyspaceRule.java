package org.jboss.aerogear.unifiedpush.arquillian;

import org.jboss.aerogear.unifiedpush.cassandra.dao.CassandraConfig;
import org.springframework.cassandra.test.integration.CassandraRule;
import org.springframework.cassandra.test.integration.KeyspaceRule;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Class rule to prepare a fixed keyspace to give tests a keyspace context. This rule uses {@link CassandraRule} to obtain a
 * Cassandra connection context. It can be used as {@link org.junit.ClassRule} and {@link org.junit.rules.TestRule}.
 * <p>
 * This rule maintains the keyspace throughout the test lifecycle. The keyspace is created when running the preparing
 * {@link #before()} methods. At the same time, the {@link #getSession() session} is logged into the created keyspace
 * and can be used for further interaction during the test. {@link #after()} the test is finished this rule drops the
 * keyspace.
 * <p>
 * Neither {@link Cluster} nor {@link Session} should be closed outside by any caller otherwise the rule cannot perform
 * its cleanup after the test run.
 */
public class FixedKeyspaceRule extends KeyspaceRule {

	public FixedKeyspaceRule(CassandraRule cassandraRule) {
		super(cassandraRule, CassandraConfig.PROP_KEYSPACE_DEFV);
	}

	public FixedKeyspaceRule(CassandraRule cassandraRule, String keyspaceName) {
		super(cassandraRule, keyspaceName);
	}

	public void beforeClass() throws Throwable {
		super.before();
	}

	public void afterClass() throws Throwable {
		super.after();
	}
}
