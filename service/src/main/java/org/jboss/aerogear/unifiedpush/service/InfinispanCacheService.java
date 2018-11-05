package org.jboss.aerogear.unifiedpush.service;

import java.util.concurrent.ConcurrentMap;

public interface InfinispanCacheService {
	<K, V> ConcurrentMap<K, V> getCache(String cachename);
}
