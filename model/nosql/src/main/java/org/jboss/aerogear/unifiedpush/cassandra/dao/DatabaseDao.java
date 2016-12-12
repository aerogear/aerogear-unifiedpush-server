package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.cassandra.dao.model.Database;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DatabaseKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DatabaseQueryKey;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface DatabaseDao extends CrudRepository<Database, DatabaseKey> {
	public static final String CACHE_NAME = "databases";

	@Cacheable(value = DatabaseDao.CACHE_NAME, unless = "#result == null")
	Database findOne(DatabaseQueryKey key);

	Stream<Database> find(UUID pushApplicationId);

	void create(Database database);

	void delete(Database database);

}
