package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryExpired;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.jboss.aerogear.unifiedpush.service.InfinispanCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InfinispanCacheServiceImpl implements InfinispanCacheService {
	private final Logger logger = LoggerFactory.getLogger(InfinispanCacheServiceImpl.class);

	protected EmbeddedCacheManager cacheManager;

	@PostConstruct
	private void startup() {
		try {
			if (cacheManager == null) {
				synchronized (this) {
					if (cacheManager == null) {
						cacheManager = (EmbeddedCacheManager) new InitialContext()
								.lookup("java:jboss/infinispan/container/aerogear");
						if (EmbeddedCacheManager.class.isAssignableFrom(cacheManager.getClass())) {
							initContainerManaged(cacheManager, "otpcodes", false);
							initContainerManaged(cacheManager, "clusterevents", true);
						}
					}
				}

				logger.info("Using container managed Infinispan cache container, lookup={}",
						"java:jboss/infinispan/container/aerogear");
			}
		} catch (NamingException e) {
			logger.warn("Unable to lookup infinispan cache container java:jboss/infinispan/container/aerogear");
		}
	}

	protected void initContainerManaged(CacheContainer cacheContainer, String cachename, boolean isClusterListener) {
		try {
			Cache<Object, Object> cache = cacheManager.getCache(cachename, true);
			if (cache != null && isClusterListener) {
				addClusterListener(cache);
			}
		} catch (Exception e) {
			logger.warn("Unable to init infinispan cache " + cachename);
		}
	}

	public <K, V> ConcurrentMap<K, V> getCache(String cachename) {
		try {
			Cache<K, V> cache = cacheManager.getCache(cachename);
			if (cache == null) {
				return new ConcurrentHashMap<>();
			}

			return (ConcurrentMap<K, V>) cache;
		} catch (Exception e) {
			logger.warn("Unable to locate infinispan cache " + cachename + ", rolling back to ConcurrentHashMap impl!");
			return new ConcurrentHashMap<>();
		}
	}

	public void addClusterListener(Cache<?, ?> cache) {
		ClusterListener clusterListener = new ClusterListener();
		cache.addListener(clusterListener);
	}

	@SuppressWarnings("rawtypes")
	@Listener(clustered = true)
	protected static class ClusterListener {
		private final Logger logger = LoggerFactory.getLogger(ClusterListener.class);

		@CacheEntryCreated
		@CacheEntryModified
		@CacheEntryExpired
		@CacheEntryRemoved
		public void onCacheEvent(CacheEntryEvent event) {
			logger.info("TESTING - Adding new cluster event %s, %s", event.getKey(), event.getValue());
		}
	}
}
