package org.jboss.aerogear.unifiedpush.cassandra.test.integration;

import org.jboss.aerogear.unifiedpush.cassandra.dao.CassandraConfig;
import org.junit.ClassRule;
import org.springframework.cassandra.core.SessionCallback;
import org.springframework.cassandra.test.integration.AbstractKeyspaceCreatingIntegrationTest;
import org.springframework.cassandra.test.integration.KeyspaceRule;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

import com.datastax.driver.core.Session;

/**
 * Base Class to prepare a fixed keyspace to give tests a keyspace context.
 */
public class FixedKeyspaceCreatingIntegrationTest extends UpsAbstractEmbeddedCassandraIntegrationTest {
	/**
	 * Class rule to prepare a keyspace to give tests a keyspace context. The
	 * keyspace name is random and changes per test.
	 */
	@ClassRule
	public final static KeyspaceRule keyspaceRule = new KeyspaceRule(cassandraEnvironment,
			CassandraConfig.PROP_KEYSPACE_DEFV);

	/**
	 * The session that's connected to the keyspace used in the current
	 * instance's test.
	 */
	protected Session session;

	/**
	 * The name of the keyspace to use for this test instance.
	 */
	protected final String keyspace;

	/**
	 * Creates a new {@link AbstractKeyspaceCreatingIntegrationTest}.
	 */
	public FixedKeyspaceCreatingIntegrationTest() {
		this(keyspaceRule.getKeyspaceName());
	}

	private FixedKeyspaceCreatingIntegrationTest(final String keyspace) {

		Assert.hasText(keyspace, "Keyspace must not be empty");

		this.keyspace = keyspace;
		this.session = keyspaceRule.getSession();

		cassandraRule.before(new SessionCallback<Object>() {

			@Override
			public Object doInSession(Session s) throws DataAccessException {

				if (!keyspace.equals(s.getLoggedKeyspace())) {
					s.execute(String.format("USE %s;", keyspace));
				}
				return null;
			}
		});
	}

	/**
	 * Returns the {@link Session}. The session is logged into the
	 * {@link #getKeyspace()}.
	 *
	 * @return
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * Returns the keyspace name.
	 *
	 * @return
	 */
	public String getKeyspace() {
		return keyspace;
	}

	/**
	 * Drop a Keyspace if it exists.
	 *
	 * @param keyspace
	 */
	public void dropKeyspace(String keyspace) {
		session.execute("DROP KEYSPACE IF EXISTS " + keyspace);
	}
}