package org.jboss.aerogear.unifiedpush.spring;

import java.util.Arrays;

import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.CacheConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DatabaseDao;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.CaffeineSpec;

@Configuration
@Import({ ConfigurationEnvironment.class, CacheConfig.class })
@ComponentScan(basePackageClasses = { IConfigurationService.class })
public class ServiceConfig {

	@Bean
	@Primary
	public CacheManager cacheManager() {
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=10000,expireAfterAccess=600s"));
		caffeineCacheManager
				.setCacheNames(Arrays.asList(DatabaseDao.CACHE_NAME, AliasDao.CACHE_NAME, IKeycloakService.CACHE_NAME));

		return caffeineCacheManager;
	}
}
