package org.jboss.aerogear.unifiedpush.cassandra;

import org.jboss.aerogear.unifiedpush.cassandra.dao.CacheConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.CassandraBaseDao;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@Import({ ConfigurationEnvironment.class, CacheConfig.class })
@ComponentScan(basePackageClasses = { CassandraBaseDao.class })
@EnableCassandraRepositories(basePackageClasses = { CassandraBaseDao.class })
public class CassandraConfig extends AbstractCassandraConfiguration {

	public static final String PROP_KEYSPACE_DEFV = "unifiedpush_server";
	private static final String PROP_CONTACP_DEFV = "127.0.0.1";
	private static final String PROP_PORT_DEFV = "9042";

	private static final String PROP_KEYSPACE_NAME_KEY = "aerogear.config.cassandra.keyspace";
	private static final String PROP_CONTACT_POINTS_KEY = "aerogear.config.cassandra.contactpoints";
	private static final String PROP_PORT_KEY = "aerogear.config.cassandra.port";

	@Autowired
	private ConfigurationEnvironment config;

	@Override
	public String getKeyspaceName() {
		return config.getProperty(PROP_KEYSPACE_NAME_KEY, PROP_KEYSPACE_DEFV);
	}

	@Bean
	public CassandraClusterFactoryBean cluster() {
		CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
		cluster.setContactPoints(config.getProperty(PROP_CONTACT_POINTS_KEY, PROP_CONTACP_DEFV));
		cluster.setPort(Integer.valueOf(config.getProperty(PROP_PORT_KEY, PROP_PORT_DEFV)));
		return cluster;
	}

	public CassandraClusterFactoryBean newCluster() {
		return cluster();
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

		return bean;
	}

	/**
	 * Override CassandraSessionFactoryBean to control and retry failed
	 * connections.
	 *
	 * TODO - Check that super.session() impl is not changed over time.
	 */
	@Bean
	public CassandraSessionFactoryBean session() throws ClassNotFoundException {

		RetryCassandraSessionFactoryBean session = new RetryCassandraSessionFactoryBean();

		session.setCluster(cluster().getObject());
		session.setConverter(cassandraConverter());
		session.setKeyspaceName(getKeyspaceName());
		session.setSchemaAction(getSchemaAction());
		session.setStartupScripts(getStartupScripts());
		session.setShutdownScripts(getShutdownScripts());

		return session;
	}
}