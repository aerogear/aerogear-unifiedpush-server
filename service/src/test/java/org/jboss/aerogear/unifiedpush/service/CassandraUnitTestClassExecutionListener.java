package org.jboss.aerogear.unifiedpush.service;

import org.cassandraunit.spring.CassandraUnitDependencyInjectionIntegrationTestExecutionListener;
import org.springframework.test.context.TestContext;

public class CassandraUnitTestClassExecutionListener
		extends CassandraUnitDependencyInjectionIntegrationTestExecutionListener {

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		super.beforeTestClass(testContext);
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		try {
			super.cleanServer();
		} catch (Exception e) {
			// TODO: ignore exception due to
			// https://github.com/jsevellec/cassandra-unit/issues/220
		}
	}

}
