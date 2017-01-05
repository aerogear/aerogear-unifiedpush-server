package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.List;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public interface AliasDao {
	public static final String CACHE_NAME = "users";

	Alias create(Alias alias);

	List<Alias> findAll(UUID pushApplicationId);

	@CacheEvict(value = CACHE_NAME)
	void delete(Alias alias);

	@Cacheable(value = CACHE_NAME, unless = "#result == null")
	Alias findByAlias(String alias);

	Alias find(Alias alias);
}
