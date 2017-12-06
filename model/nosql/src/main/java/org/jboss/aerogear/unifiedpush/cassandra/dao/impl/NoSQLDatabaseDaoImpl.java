package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.cassandra.dao.DatabaseDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.Database;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DatabaseKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DatabaseQueryKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

@Repository
class NoSQLDatabaseDaoImpl extends CassandraBaseDao<Database, DatabaseKey> implements DatabaseDao {
	private static final String MV_BY_DATABASE = "databases_by_database";

	public NoSQLDatabaseDaoImpl(@Autowired CassandraOperations operations) {
		super(Database.class, new CassandraRepositoryFactory(operations).getEntityInformation(Database.class),
				operations);
	}

	public Database findOne(DatabaseQueryKey key) {
		Select select = QueryBuilder.select().from(MV_BY_DATABASE);
		select.where(QueryBuilder.eq("push_application_id", key.getPushApplicationId()));
		select.where(QueryBuilder.eq("database", key.getDatabase()));

		return operations.selectOne(select, domainClass);
	}

	public Stream<Database> find(UUID pushApplicationId) {
		Select select = QueryBuilder.select().from(super.tableName);
		select.where(QueryBuilder.eq("push_application_id", pushApplicationId));

		return operations.stream(select, domainClass);
	}

	@Override
	public void create(Database database) {
		super.insert(database);
	}

}
