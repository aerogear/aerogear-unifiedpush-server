package org.jboss.aerogear.unifiedpush.arquillian;

import java.lang.reflect.Method;

import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeKill;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.springframework.cassandra.test.integration.support.CqlDataSet;

public class LifecycleExecuter {
	/*
	 * Initiate Cassandra database before container deployment is taking place.
	 */
	public final static FixedCassandraRule cassandraEnvironment = new FixedCassandraRule("ups-embedded-cassandra.yaml");
	public final static FixedKeyspaceRule keyspaceRule = new FixedKeyspaceRule(cassandraEnvironment, CassandraConfig.PROP_KEYSPACE_DEFV);

	public void executeCql(String cqlResourceName, String keyspace) {
		cassandraEnvironment.execute(CqlDataSet.fromClassPath(cqlResourceName).executeIn(keyspace));
	}

	// Start cassandra embedded right after container start event.
	public void executeAfterStart(@Observes AfterStart event) {
		try {
			cassandraEnvironment.before();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// Stop cassandra embedded just before container stop event.
	public void executeBeforeKill(@Observes BeforeKill event) {

		try {
			cassandraEnvironment.afterClass();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void executeBeforeDeploy(@Observes BeforeDeploy event, TestClass testClass) {

		try {
			keyspaceRule.beforeClass();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		executeCql("cassandra-test-cql-dataload.cql", CassandraConfig.PROP_KEYSPACE_DEFV);

		// Propagate event
		execute(testClass.getMethods(org.jboss.aerogear.unifiedpush.arquillian.api.BeforeDeploy.class));
	}

	public void executeAfterDeploy(@Observes AfterDeploy event, TestClass testClass) {
		execute(testClass.getMethods(org.jboss.aerogear.unifiedpush.arquillian.api.AfterDeploy.class));
	}

	public void executeBeforeUnDeploy(@Observes BeforeUnDeploy event, TestClass testClass) {
		try {
			keyspaceRule.afterClass();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		execute(testClass.getMethods(org.jboss.aerogear.unifiedpush.arquillian.api.BeforeUnDeploy.class));
	}

	public void executeAfterUnDeploy(@Observes AfterUnDeploy event, TestClass testClass) {
		execute(testClass.getMethods(org.jboss.aerogear.unifiedpush.arquillian.api.AfterUnDeploy.class));
	}

	private void execute(Method[] methods) {
		if (methods == null) {
			return;
		}
		for (Method method : methods) {
			try {
				method.invoke(null);
			} catch (Exception e) {
				throw new RuntimeException("Could not execute @BeforeDeploy method: " + method, e);
			}
		}
	}
}