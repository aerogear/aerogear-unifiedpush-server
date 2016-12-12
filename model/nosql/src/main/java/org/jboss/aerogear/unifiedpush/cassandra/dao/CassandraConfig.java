package org.jboss.aerogear.unifiedpush.cassandra.dao;

import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.CassandraBaseDao;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@Import({ ConfigurationEnvironment.class, CacheConfig.class})
@ComponentScan(basePackageClasses = { CassandraBaseDao.class })
@EnableCassandraRepositories(basePackageClasses = { CassandraBaseDao.class })
public class CassandraConfig extends AbstractCassandraConfiguration {

	public static final String PROP_KEYSPACE_DEFV = "unifiedpush_server";
	private static final String PROP_KEYSPACE_NAME = "aerogear.config.cassandra.keyspace";
	private static final String PROP_CONTACT_POINTS = "aerogear.config.cassandra.contactpoints";
	private static final String PROP_CONTACP_DEFV = "127.0.0.1";
	private static final String PROP_PORT = "aerogear.config.cassandra.port";
	private static final String PROP_PORT_DEFV = "9142";

	@Autowired
	private ConfigurationEnvironment config;

	@Override
	public String getKeyspaceName() {
		return config.getProperty(PROP_KEYSPACE_NAME, PROP_KEYSPACE_DEFV);
	}

	@Bean
	public CassandraClusterFactoryBean cluster() {
		CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
		cluster.setContactPoints(config.getProperty(PROP_CONTACT_POINTS, PROP_CONTACP_DEFV));
		cluster.setPort(Integer.valueOf(config.getProperty(PROP_PORT, PROP_PORT_DEFV)));
		return cluster;
	}

	/**
	 * Manually assemble entity mapping. this is required because xml mapping is
	 * not fully supported.
	 */
	@Bean
	@Override
	public CassandraMappingContext cassandraMapping() throws ClassNotFoundException {

		BasicCassandraMappingContext bean = new BasicCassandraMappingContext();
		bean.initialize();
		bean.setInitialEntitySet(CassandraEntityClassScanner.scan(getEntityBasePackages()));
		bean.setBeanClassLoader(beanClassLoader);

		return bean;
	}
}