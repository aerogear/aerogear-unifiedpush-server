package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.CassandraConfig;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DistinctUitils;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CassandraAccessor;
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

@Repository
class NoSQLUserDaoImpl extends CassandraBaseDao<User, UserKey> implements AliasDao {
	private final Logger logger = LoggerFactory.getLogger(NoSQLUserDaoImpl.class);

	private static final String MV_BY_PUSH_APPLICATION = "users_by_application";
	private static final String MV_BY_ALIAS_AND_APPLICATION = "users_by_alias_application";
	private static final String MV_BY_ALIAS = "users_by_alias";
	private static final List<Integer> months;

	@Autowired
	private CacheManager cacheManager;

	static {
		months = Arrays.stream(Month.values()).map(month -> month.getValue()).collect(Collectors.toList());
	}

	public NoSQLUserDaoImpl(@Autowired CassandraOperations operations, @Autowired CassandraConfig configuraion) {
		super(User.class, new CassandraRepositoryFactory(operations).getEntityInformation(User.class), operations,
				configuraion);

		Assert.isTrue(
				((CassandraAccessor) operations.getCqlOperations()).getConsistencyLevel() == getConsistencyLevel(),
				"ConsistencyLevel Must be QUORUM");
	}

	@Override
	public List<User> create(Alias alias) {
		List<User> users = new ArrayList<User>();

		if (StringUtils.isNotEmpty(alias.getEmail())) {
			// Create user by alias
			users.add(User.copy(alias, alias.getEmail(), User.AliasType.EMAIL.ordinal()));

			// Keep email as lower case so we can later match ignore case.
			if (isLowerCaseRequired(alias.getEmail())) {
				users.add(User.copy(alias, alias.getEmail().toLowerCase(), User.AliasType.EMAIL_LOWER.ordinal()));
			}
		}

		if (StringUtils.isNotEmpty(alias.getOther())) {
			users.add(User.copy(alias, alias.getOther(), User.AliasType.OTHER.ordinal()));
		}

		users.stream().forEach(user -> {
			super.save(user);
		});

		if (users == null || users.size() == 0) {
			logger.warn("Attempt to store an alias without a valid AliasType. alias:{}", alias.toString());
		}

		return users;
	}

	public User update(User entity) {
		// Read before write validation.
		User user = findById(entity.getKey()).orElse(null);
		if (user == null) {
			return super.insert(entity);
		}

		return super.save(entity);
	}

	/*
	 * PERFORMANCE, This method might lead to pure performance or OutOfMemory.
	 * Avoid usage as possible.
	 */
	@Override
	public List<Alias> findAll(UUID pushApplicationId) {
		List<Row> rows = findUserIds(pushApplicationId).filter(DistinctUitils.distinctByKey(row -> row.getUUID(0)))
				.collect(Collectors.toList());

		List<Alias> aliases = new ArrayList<>();
		rows.forEach(row -> {
			aliases.add(new Alias(pushApplicationId, row.getUUID(0)));
		});

		return aliases;
	}

	@Override
	public Alias findByAlias(UUID pushApplicationId, String alias) {
		// Always find latest alias assuming users_by_alias is sorted DESC.
		// pushApplicationId should be null only by associate/verify API.
		Optional<Row> row = findUserIds(alias, pushApplicationId).findFirst();
		if (row.isPresent()) {
			UserKey ukey = getKey(row.get());
			return findOne(ukey.getPushApplicationId(), ukey.getId());
		}

		return null;
	}

	public Alias findOne(UUID pushApplicationId, UUID userId) {
		// Get all possible aliases for a userId
		List<User> users = getUsers(pushApplicationId, userId);

		if (users == null || users.size() == 0) {
			return null;
		}

		Alias alias = new Alias(pushApplicationId, userId);

		users.forEach(user -> {
			// We ignore User.AliasType.EMAIL_LOWER.
			if (user.getType() == User.AliasType.EMAIL.ordinal()) {
				alias.setEmail(user.getAlias());
			} else if (user.getType() == User.AliasType.OTHER.ordinal()) {
				alias.setOther(user.getAlias());
			}
		});

		return alias;
	}

	private List<User> getUsers(UUID pushApplicationId, UUID userId) {
		Select select = QueryBuilder.select().from(super.tableName);
		select.where(QueryBuilder.eq(UserKey.FIELD_PUSH_APPLICATION_ID, pushApplicationId));
		select.where(QueryBuilder.eq(UserKey.FIELD_USER_ID, userId));

		// Get all possible aliases for a userId
		return operations.select(select, super.domainClass);
	}

	/*
	 * Remove all aliases according to application id.
	 */
	public void removeAll(UUID pushApplicationId) {
		findUserIds(pushApplicationId).forEach(row -> {
			deleteById(new UserKey(pushApplicationId, row.getUUID(0)));
		});
	}

	/*
	 * Select push_application_id, user_id and alias according to optional
	 * aliases.
	 */
	private Stream<Row> findUserIds(String alias, UUID pushApplicationId) {
		List<String> aliases = optionalAliases(alias, null);

		StringBuffer cql = new StringBuffer("SELECT ") //
				.append(UserKey.FIELD_PUSH_APPLICATION_ID).append(",") //
				.append(UserKey.FIELD_USER_ID).append(",") //
				.append(UserKey.FIELD_ALIAS) //
				.append(" FROM ").append(pushApplicationId == null ? MV_BY_ALIAS : MV_BY_ALIAS_AND_APPLICATION) //
				.append(" WHERE alias IN ('").append(StringUtils.join(aliases, "','")).append("')");
		if (pushApplicationId != null) {
			cql.append(" AND ").append(UserKey.FIELD_PUSH_APPLICATION_ID).append("=").append(pushApplicationId);
		}

		return StreamSupport.stream(
				operations.getCqlOperations().queryForResultSet(new SimpleStatement(cql.toString())).spliterator(),
				false);
	}

	/*
	 * Select user_id from all 12 partitions (by month). For a planet scale
	 * sizing, we can also create MV by day (365 partitions).
	 */
	public Stream<Row> findUserIds(UUID pushApplicationId) {
		StringBuffer cql = new StringBuffer("SELECT ") //
				.append(UserKey.FIELD_USER_ID) //
				.append(" FROM ").append(MV_BY_PUSH_APPLICATION) //
				.append(" WHERE ").append(UserKey.FIELD_PUSH_APPLICATION_ID).append("=").append(pushApplicationId) //
				.append(" AND month IN (").append(StringUtils.join(months, ",")).append(")");

		return StreamSupport.stream(
				operations.getCqlOperations().queryForResultSet(new SimpleStatement(cql.toString())).spliterator(),
				false);
	}

	@Override
	public Stream<UserKey> findAllUserTenantRelations() {
		StringBuilder cql = new StringBuilder("SELECT ")
				.append(UserKey.FIELD_USER_ID).append(", ").append(UserKey.FIELD_ALIAS)
				.append(", ").append(UserKey.FIELD_PUSH_APPLICATION_ID)
				.append(" FROM ").append(MV_BY_ALIAS);

		return StreamSupport.stream(
				operations.getCqlOperations().queryForResultSet(new SimpleStatement(cql.toString())).spliterator(),
				false)
				.map(NoSQLUserDaoImpl::getKey);
	}

	@Override
	public Stream<UserKey> findUserTenantRelations(String alias) {
		Stream<Row> findUserIds = findUserIds(alias, null);
		return findUserIds.map(NoSQLUserDaoImpl::getKey);
	}

	@Override
	public List<UserKey> remove(UUID pushApplicationId, String alias) {
		Stream<Row> findUserIds = findUserIds(alias, pushApplicationId);
		List<UserKey> userKeys = findUserIds.map(NoSQLUserDaoImpl::getKey).collect(Collectors.toList());
		for (UserKey userKey : userKeys) {
			deleteById(userKey);
		}
		return userKeys;
	}

	/*
	 * Remove all available aliases for a user id.
	 */
	@Override
	public void remove(UUID pushApplicationId, UUID id) {
		List<User> aliases = getUsers(pushApplicationId, id);
		deleteById(new UserKey(pushApplicationId, id));

		// Evict available aliases from cache.
		if (aliases != null) {
			aliases.stream().forEach(user -> {
				evict(user.getKey().getPushApplicationId(), user.getAlias());
			});
		}
	}

	@Override
	public void deleteById(UserKey key) {
		// Delete all aliases by partition key
		// Future spring-cassandra versions might handle null clustering key.
		if (key.getAlias() == null) {
			Delete delete = QueryBuilder.delete().from(super.tableName);
			delete.where(QueryBuilder.eq(UserKey.FIELD_PUSH_APPLICATION_ID, key.getPushApplicationId()));
			delete.where(QueryBuilder.eq(UserKey.FIELD_USER_ID, key.getId()));
			operations.getCqlOperations().execute(delete);
		} else {
			super.deleteById(key);
			evict(key.getPushApplicationId(), key.getAlias());
		}
	}

	/*
	 * If lower-case value equals current value, return both alias and
	 * alias.lowerCase()
	 */
	private List<String> optionalAliases(String alias, List<String> aliases) {
		if (aliases == null) {
			aliases = new ArrayList<String>();
		}

		if (StringUtils.isNoneEmpty(alias)) {
			aliases.add(alias);

			if (isLowerCaseRequired(alias)) {
				aliases.add(alias.toLowerCase());
			}
		}

		return aliases;
	}

	private boolean isLowerCaseRequired(String alias) {
		return !alias.equals(alias.toLowerCase());

	}

	private static UserKey getKey(Row row) {
		return new UserKey(row.getUUID(0), row.getUUID(1), row.getString(2));
	}

	// Aliases are cached by pushApplicationId and alias name.
	private void evict(UUID pushApplicationId, String alias) {
		Cache cache = cacheManager.getCache(AliasDao.CACHE_NAME);
		cache.evict(new SimpleKey(pushApplicationId, alias));
	}
}
