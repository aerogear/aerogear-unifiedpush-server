package org.jboss.aerogear.unifiedpush.arquillian;

import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.springframework.cassandra.core.SessionCallback;
import org.springframework.cassandra.test.integration.CassandraRule;
import org.springframework.cassandra.test.integration.KeyspaceRule;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

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
	private final CassandraRule cassandraRule;
	private Session session;
	private final String keyspaceName;


	public FixedKeyspaceRule(CassandraRule cassandraRule) {
		super(cassandraRule, CassandraConfig.PROP_KEYSPACE_DEFV);

		this.cassandraRule = cassandraRule;
		this.keyspaceName = CassandraConfig.PROP_KEYSPACE_DEFV;
	}

	public FixedKeyspaceRule(CassandraRule cassandraRule, String keyspaceName) {
		super(cassandraRule, keyspaceName);
		this.cassandraRule = cassandraRule;
		this.keyspaceName = keyspaceName;
	}

	public void beforeClass() throws Throwable {
		before();
	}

	public void afterClass() throws Throwable {
		after();
	}

	@Override
	protected void before() throws Throwable {

		// Support initialized and initializing CassandraRule.
		if (cassandraRule.getCluster() != null) {
			this.session = cassandraRule.getSession();
		} else {
			cassandraRule.before(new SessionCallback<Object>() {
				@Override
				public Object doInSession(Session session) throws DataAccessException {
					FixedKeyspaceRule.this.session = cassandraRule.getSession();
					return null;
				}
			});
		}

		Assert.state(session != null, "Session was not initialized");

		session.execute(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH durable_writes = false AND "
				+ "replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};", keyspaceName));
		session.execute(String.format("USE %s;", keyspaceName));
	}

	@Override
	protected void after() {
		session.execute("USE system;");
		session.execute(String.format("DROP KEYSPACE %s;", keyspaceName));
	}
}
