package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.datastax.driver.core.Row;

public interface AliasDao {
	public static final String CACHE_NAME = "aliases";

	List<User> create(Alias alias);

	List<Alias> findAll(UUID pushApplicationId);

	@CacheEvict(value = CACHE_NAME, allEntries = true)
	void removeAll(UUID pushApplicationId);

	Stream<UserKey> findUserTenantRelations(String alias);

	@CacheEvict(value = CACHE_NAME)
	List<UserKey> remove(UUID pushApplicationId, String alias);

	@Cacheable(value = CACHE_NAME, condition = "#pushApplicationId != null", unless = "#result == null")
	Alias findByAlias(UUID pushApplicationId, String alias);

	/**
	 * Remove all aliases according to given parameters. All aliases are manually
	 * evicted from cache.
	 *
	 * @param pushApplicationId selected push application
	 * @param userId            User in UUID v1 format
	 */
	void remove(UUID pushApplicationId, UUID userId);

	Stream<Row> findUserIds(UUID pushApplicationId);

	Alias findOne(UUID pushApplicationId, UUID userId);

	Stream<UserKey> findAllUserTenantRelations();
}
