package org.jboss.aerogear.unifiedpush;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@PropertySource(value = { "classpath:db.properties", "file://${org.jboss.aerogear.unifiedpush.conf}/db.properties" }, ignoreResourceNotFound = true)
public class PersistenceJPAConfig {
	private static final Logger logger = LoggerFactory.getLogger(PersistenceJPAConfig.class);
	private static String[] RESOURCES = new String[] { 
			"META-INF/orm.xml", 
			"org/jboss/aerogear/unifiedpush/api/Installation.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/Category.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/PushMessageInformation.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/VariantMetricInformation.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/Property.hbm.xml"
	};
	
	private static final String SYS_KEY_DB_NAME = "org.jboss.aerogear.unifiedpush.initdb.database";

	@Autowired
	private Environment env;

	@Bean
	public DataSource dataSource() {
		final ComboPooledDataSource dataSource = new ComboPooledDataSource();
		String databaseName;
		
		try {
			dataSource.setDriverClass(env.getProperty("jdbc.driverClassName"));
		} catch (final PropertyVetoException e) {
			logger.error("Error while loading datasource driver!", e);
		}
		
		// Override databaseName from -D if exists
		if (System.getProperties().contains(SYS_KEY_DB_NAME) && !StringUtils.isEmpty(System.getProperties().get(SYS_KEY_DB_NAME))){
			databaseName = System.getProperties().getProperty(SYS_KEY_DB_NAME);
		}else {
			databaseName = env.getProperty("jdbc.database");
		}
		
		
		dataSource.setJdbcUrl(String.format(env.getProperty("jdbc.url"), databaseName));
		dataSource.setUser(env.getProperty("jdbc.username"));

		return dataSource;
	}

	@Bean
	public EntityManagerFactory entityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setPersistenceUnitManager(persistenceUnitManager());
		
		final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		((AbstractJpaVendorAdapter) vendorAdapter).setGenerateDdl(true);
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());
		em.afterPropertiesSet();

		return em.getObject();
	}

	@Bean
	public PersistenceUnitManager persistenceUnitManager(){
		DefaultPersistenceUnitManager pm = new DefaultPersistenceUnitManager();
		DataSource dataSource = dataSource();
		
		pm.setDataSourceLookup(new SingleDataSourceLookup(dataSource()));
		pm.setDefaultDataSource(dataSource);
		pm.setMappingResources(RESOURCES);
		pm.setDefaultPersistenceUnitRootLocation(null);
		
		return pm;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager(entityManagerFactory());
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	Properties additionalProperties() {
		Properties props = null;
		final PropertiesFactoryBean propertiesBean = new PropertiesFactoryBean();
		propertiesBean.setLocation(new ClassPathResource("hbm.properties"));

		try {
			propertiesBean.afterPropertiesSet();
			props = propertiesBean.getObject();
		} catch (final IOException e) {
			logger.error("Error while reading hbm.properties from class path", e);
			props = new Properties();
		}
		// Add Hibernate Dialect from db.properties
		props.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
		props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
		return props;
	}

}