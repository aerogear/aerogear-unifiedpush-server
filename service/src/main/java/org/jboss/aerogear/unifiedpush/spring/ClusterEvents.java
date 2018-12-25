package org.jboss.aerogear.unifiedpush.spring;

import java.util.UUID;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.jboss.aerogear.unifiedpush.spring.ServiceCacheConfig.ClusterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

@Listener(clustered = true)
public class ClusterEvents {
	private final Logger logger = LoggerFactory.getLogger(ClusterEvents.class);

	private CacheManager cacheManager;

	public ClusterEvents(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@CacheEntryCreated
	public void entryCreated(CacheEntryCreatedEvent<UUID, ClusterEvent> event) {
		logger.debug("Entry was removed from cache {} entity with key {}", event.getValue().getCacheName(),
				event.getValue());

		cacheManager.getCache(event.getValue().getCacheName()).evict(new SimpleKey(event.getValue().getKeys()));
	}
}
