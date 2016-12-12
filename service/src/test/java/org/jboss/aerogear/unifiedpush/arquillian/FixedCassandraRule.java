package org.jboss.aerogear.unifiedpush.arquillian;

import org.springframework.cassandra.test.integration.CassandraRule;

public class FixedCassandraRule extends CassandraRule {

	public FixedCassandraRule(String yamlConfigurationResource) {
		super(yamlConfigurationResource);
	}

	public FixedCassandraRule(String yamlConfigurationResource, long startUpTimeout) {
		super(yamlConfigurationResource, startUpTimeout);
	}

	public void afterClass() throws Throwable {
		super.after();
	}

}
