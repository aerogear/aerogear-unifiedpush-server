package org.jboss.aerogear.unifiedpush.jpa;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPABaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = JPABaseDao.class)
@ComponentScan(basePackageClasses = { JPABaseDao.class })
@PropertySource(name = "database", value = { "classpath:META-INF/db.properties",
		"file://${aerobase.config.dir}/db.properties" }, ignoreResourceNotFound = true)
@PropertySource(name = "hibernate", value = { "classpath:META-INF/hibernate.properties",
		"file://${aerobase.config.dir}/hibernate.properties" }, ignoreResourceNotFound = true)
public class JPAConfig {
	private static String[] RESOURCES = new String[] { "META-INF/orm.xml",
			"org/jboss/aerogear/unifiedpush/api/Installation.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/Category.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/FlatPushMessageInformation.hbm.xml",
			"org/jboss/aerogear/unifiedpush/api/VariantErrorStatus.hbm.xml" };

	@Autowired
	private Environment env;

	@Bean
	public EntityManagerFactory entityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setPersistenceUnitManager(persistenceUnitManager());

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(hibernateProperties());
		em.setDataSource(dataSource());
		em.afterPropertiesSet();

		return em.getObject();
	}

	@Bean
	public PersistenceUnitManager persistenceUnitManager() {
		DefaultPersistenceUnitManager pm = new DefaultPersistenceUnitManager();

		pm.setDefaultDataSource(dataSource());
		pm.setDataSourceLookup(new SingleDataSourceLookup(dataSource()));
		pm.setMappingResources(RESOURCES);
		pm.setDefaultPersistenceUnitRootLocation(null);

		return pm;
	}

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		return new HikariDataSource(new HikariConfig(databaseProperties()));
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emFactory) {
		return new JpaTransactionManager(emFactory);
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	Properties databaseProperties() {
		Properties props = new Properties();
		for (Iterator<?> it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
			org.springframework.core.env.PropertySource<?> propertySource = (org.springframework.core.env.PropertySource<?>) it
					.next();
			if (EnumerablePropertySource.class.isAssignableFrom(propertySource.getClass())
					&& propertySource.getName().equals("database")) {
				Arrays.stream(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
						.forEach(prop -> props.put(prop, propertySource.getProperty(prop)));
			}
		}

		return props;
	}

	Properties hibernateProperties() {
		Properties props = new Properties();
		for (Iterator<?> it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
			org.springframework.core.env.PropertySource<?> propertySource = (org.springframework.core.env.PropertySource<?>) it
					.next();
			if (EnumerablePropertySource.class.isAssignableFrom(propertySource.getClass())
					&& propertySource.getName().equals("hibernate")) {
				Arrays.stream(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
						.forEach(prop -> props.put(prop, propertySource.getProperty(prop)));
			}
		}

		return props;
	}

}
