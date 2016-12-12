package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.List;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.springframework.cache.annotation.Cacheable;

public interface AliasDao {
	public static final String CACHE_NAME = "users";

	Alias create(Alias alias);

	List<Alias> findAll(UUID pushApplicationId);

	void delete(UUID pushApplicationId);

	void delete(Alias alias);

	@Cacheable(value = CACHE_NAME, unless = "#result == null")
	Alias findByAlias(String alias);

	Alias find(Alias alias);
}
