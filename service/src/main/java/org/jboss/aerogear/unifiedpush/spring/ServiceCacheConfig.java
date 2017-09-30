package org.jboss.aerogear.unifiedpush.spring;

import java.util.Arrays;

import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DatabaseDao;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.CaffeineSpec;

@Configuration
@EnableCaching
public class ServiceCacheConfig {
	@Bean
	@Primary
	public CacheManager cacheManager() {
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=100000,expireAfterAccess=600s"));
		caffeineCacheManager.setCacheNames(Arrays.asList(DatabaseDao.CACHE_NAME, AliasDao.CACHE_NAME,
				IKeycloakService.CACHE_NAME, GenericVariantService.CACHE_NAME,
				PushApplicationService.APPLICATION_CACHE_BY_ID, PushApplicationService.APPLICATION_CACHE_BY_VAR_ID,
				PushApplicationService.APPLICATION_CACHE_BY_NAME));

		return caffeineCacheManager;
	}
}
