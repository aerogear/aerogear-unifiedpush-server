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
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;

@Repository
class NoSQLUserDaoImpl extends CassandraBaseDao<User, UserKey> implements AliasDao {
	private static final String MV_BY_PUSH_APPLICATION = "users_by_application";
	private static final String MV_BY_ALIAS_AND_APPLICATION = "users_by_alias_application";
	private static final String MV_BY_ALIAS = "users_by_alias";
	private static final List<Integer> months;

	static {
		months = Arrays.stream(Month.values()).map(month -> month.getValue()).collect(Collectors.toList());
	}

	public NoSQLUserDaoImpl() {
		super(User.class);
	}

	@Override
	public List<User> create(Alias alias) {
		List<User> users = new ArrayList<User>();

		if (StringUtils.isNotEmpty(alias.getEmail())) {

			// Make sure email is unique cross other applications.
			Optional<Row> existing = findUserIds(alias.getEmail(), null)
					.filter(row -> !alias.getPushApplicationId().equals(getKey(row).getPushApplicationId()))
					.findFirst();

			if (existing.isPresent()) {
				UserKey key = getKey(existing.get());
				throw new AliasAlreadyExists(alias.getEmail(), key.getPushApplicationId());
			}

			// Create user by alias
			users.add(User.copy(alias, alias.getEmail()));

			// Keep email as lower case so we can later match ignore case.
			if (isLowerCaseRequired(alias.getEmail())) {
				users.add(User.copy(alias, alias.getEmail().toLowerCase()));
			}
		}

		if (StringUtils.isNotEmpty(alias.getMobile())) {
			users.add(User.copy(alias, alias.getMobile()));
		}

		users.stream().forEach(user -> {
			super.save(user);
		});

		return users;
	}

	@Override
	@SuppressWarnings("unchecked")
	public User update(User entity) {
		// Read before write validation.
		User user = findOne(entity.getKey());
		if (user == null)
			return super.save(entity);

		return update(entity);
	}

	@Override
	public List<Alias> findAll(UUID pushApplicationId) {
		return findUserIds(pushApplicationId).collect(ArrayList::new, (m, row) -> {
			m.add(new Alias(pushApplicationId, row.getUUID(0)));
		}, ArrayList::addAll);
	}

	@Override
	public Alias findByAlias(UUID pushApplicationId, String alias) {
		// Always find latest alias assuming users_by_alias is sorted DESC.
		// pushApplicationId should be null only by associate/verify API.
		Optional<Row> row = findUserIds(alias, pushApplicationId).findFirst();
		if (row.isPresent()) {
			UserKey ukey = getKey(row.get());
			return new Alias(ukey.getPushApplicationId(), ukey.getId(), ukey.getAlias());
		}

		return null;
	}

	/*
	 * Remove all aliases according to application id.
	 */
	public void removeAll(UUID pushApplicationId) {
		findUserIds(pushApplicationId).forEach((row) -> {
			delete(new UserKey(pushApplicationId, row.getUUID(0)));
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
	public void remove(UUID pushApplicationId, String alias) {
		findUserIds(alias, pushApplicationId).forEach(row -> {
			delete(getKey(row));
		});
	}

	@Override
	public void delete(UserKey key) {
		// Delete all aliases by partition key
		// Future spring-cassandra versions might handle null clustering key.
		if (key.getAlias() == null) {
			Delete delete = QueryBuilder.delete().from(super.tableName);
			delete.where(QueryBuilder.eq(UserKey.FIELD_PUSH_APPLICATION_ID, key.getPushApplicationId()));
			delete.where(QueryBuilder.eq(UserKey.FIELD_USER_ID, key.getId()));
			operations.getCqlOperations().execute(delete);
		} else {
			super.delete(key);
		}
	}

	@Override
	protected UserKey getId(User entity) {
		return entity.getKey();
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

	private UserKey getKey(Row row) {
		return new UserKey(row.getUUID(0), row.getUUID(1), row.getString(2));
	}
}
