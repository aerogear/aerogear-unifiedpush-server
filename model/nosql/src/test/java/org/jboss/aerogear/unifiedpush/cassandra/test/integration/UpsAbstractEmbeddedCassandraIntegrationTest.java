package org.jboss.aerogear.unifiedpush.cassandra.test.integration;

import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.cassandra.core.SessionCallback;
import org.springframework.cassandra.test.integration.CassandraRule;
import org.springframework.cassandra.test.integration.support.CqlDataSet;
import org.springframework.dao.DataAccessException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Copy implementation from AbstractEmbeddedCassandraIntegrationTest in order to
 * use custom yaml file name.
 *
 * This implementation prevent confusion by using a ups specific
 * embedded-cassandra.yaml.
 */
public class UpsAbstractEmbeddedCassandraIntegrationTest {

	/**
	 * Initiate a Cassandra environment in test class scope.
	 */
	@ClassRule
	public final static CassandraRule cassandraEnvironment = new CassandraRule("ups-embedded-cassandra.yaml");

	/**
	 * Initiate a Cassandra environment in test scope.
	 */
	@Rule
	public final CassandraRule cassandraRule = cassandraEnvironment.testInstance()
			.before(new SessionCallback<Object>() {
				@Override
				public Object doInSession(Session session) throws DataAccessException {
					UpsAbstractEmbeddedCassandraIntegrationTest.this.cluster = session.getCluster();
					return null;
				}
			});

	/**
	 * The {@link Cluster} that's connected to Cassandra.
	 */
	protected Cluster cluster;

	/**
	 * Creates a random UUID.
	 *
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Returns the {@link Cluster}.
	 *
	 * @return
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Executes a CQL script from a classpath resource in given
	 * {@code keyspace}.
	 *
	 * @param cqlResourceName
	 * @param keyspace
	 */
	public void execute(String cqlResourceName, String keyspace) {
		cassandraRule.execute(CqlDataSet.fromClassPath(cqlResourceName).executeIn(keyspace));
	}
}
