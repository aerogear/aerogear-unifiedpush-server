package org.jboss.aerogear.unifiedpush.spring;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.InitialContext;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.provider.SpringCache;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DatabaseDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.RealmsService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
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
	private final Logger logger = LoggerFactory.getLogger(ServiceCacheConfig.class);
	private final static String OTP_CACHE = "otpcodes";
	private final static String EVENTS_CACHE = "cluster-events-cache";

	private static final List<String> cacheNames = Arrays.asList(//
			DatabaseDao.CACHE_NAME, //
			AliasDao.CACHE_NAME, //
			IKeycloakService.CACHE_NAME, //
			RealmsService.REALMS_CACHE_NAME, //
			GenericVariantService.CACHE_NAME, ///
			PushApplicationService.APPLICATION_CACHE_BY_ID, //
			PushApplicationService.APPLICATION_CACHE_BY_VAR_ID, //
			PushApplicationService.APPLICATION_CACHE_BY_NAME, //
			ServiceCacheConfig.OTP_CACHE);

	private CacheManager cacheManager = null;

	@Bean
	@Primary
	public CacheManager CacheManager() {
		cacheManager = infinispanCacheManager();

		if (cacheManager == null) {
			localCacheManager();
		}

		return cacheManager;
	}

	private CacheManager infinispanCacheManager() {
		try {
			if (cacheManager == null) {
				synchronized (this) {
					if (cacheManager == null) {
						EmbeddedCacheManager infiCache = (EmbeddedCacheManager) new InitialContext()
								.lookup("java:jboss/infinispan/container/aerogear");
						if (infiCache != null) {
							cacheManager = new SpringEmbeddedCacheManager((EmbeddedCacheManager) infiCache);
							cacheNames.forEach(cache -> initContainerManaged(infiCache, cache, null));

							initContainerManaged(infiCache, EVENTS_CACHE, new ClusterEvents(cacheManager));
						}
					}
				}

				logger.info("Using container managed Infinispan cache container, lookup={}",
						"java:jboss/infinispan/container/aerogear");
			}
		} catch (Throwable e) {
			logger.warn("Unable to lookup infinispan cache container java:jboss/infinispan/container/aerogear");
		}

		return cacheManager;
	}

	protected void initContainerManaged(EmbeddedCacheManager cacheContainer, String cachename, Object listener) {
		try {
			org.infinispan.Cache<Object, Object> cache = cacheContainer.getCache(cachename);

			if (cache != null && listener != null) {
				logger.debug("Adding cache listener to cache " + cachename);

				cache.addListener(listener);
			}
		} catch (Exception e) {
			logger.warn("Unable to init infinispan cache " + cachename);
		}
	}

	@SuppressWarnings("unchecked")
	private <K, V> ConcurrentMap<K, V> getCache(String cachename) {
		try {
			Cache cache = cacheManager.getCache(cachename);

			if (cache == null) {
				return new ConcurrentHashMap<>();
			}

			if (SpringCache.class.isAssignableFrom(cache.getClass())) {
				return (ConcurrentMap<K, V>) ((SpringCache) cache).getNativeCache();
			}

			return (ConcurrentMap<K, V>) cache;
		} catch (Exception e) {
			logger.warn("Unable to locate infinispan cache " + cachename + ", rolling back to ConcurrentHashMap impl!");
			return new ConcurrentHashMap<>();
		}
	}

	public ConcurrentMap<OtpCodeKey, Set<Object>> getOtpCache() {
		return getCache(OTP_CACHE);
	}

	public ConcurrentMap<UUID, ClusterEvent> getClusterEventsCache() {
		return getCache(EVENTS_CACHE);
	}

	public CacheManager localCacheManager() {
		if (cacheManager == null) {
			synchronized (this) {
				if (cacheManager == null) {
					CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
					caffeineCacheManager
							.setCaffeineSpec(CaffeineSpec.parse("maximumSize=100000,expireAfterAccess=600s"));
					caffeineCacheManager.setCacheNames(cacheNames);
					cacheManager = caffeineCacheManager;
				}
			}
		}

		return cacheManager;
	}

	public static class ClusterEvent implements Serializable {
		private static final long serialVersionUID = -2628808862845692788L;

		private String cacheName;
		private Object[] keys;

		public static ClusterEvent forAlias(Object... keys) {
			return new ClusterEvent(AliasDao.CACHE_NAME, keys);
		}

		private ClusterEvent(String cacheName, Object... keys) {
			super();
			this.cacheName = cacheName;
			this.keys = keys;
		}

		public String getCacheName() {
			return cacheName;
		}

		public Object[] getKeys() {
			return keys;
		}

		@Override
		public String toString() {
			return "ClusterEvent [keys=" + Arrays.toString(keys) + "]";
		}
	}
}
