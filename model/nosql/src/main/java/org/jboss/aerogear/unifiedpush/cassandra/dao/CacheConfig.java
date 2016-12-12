package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.CaffeineSpec;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=10000,expireAfterAccess=600s"));
		caffeineCacheManager.setCacheNames(Arrays.asList(DatabaseDao.CACHE_NAME, AliasDao.CACHE_NAME));

		return caffeineCacheManager;
	}
}