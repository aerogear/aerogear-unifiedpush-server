package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

@Repository
class NoSQLUserDaoImpl extends CassandraBaseDao<User, UserKey> implements AliasDao {

	public NoSQLUserDaoImpl() {
		super(User.class);
	}

	@Override
	public Alias create(Alias alias) {
		User user = new User().clone(alias);
		if (StringUtils.isNotEmpty(user.getEmail())) {
			// Keep aliases as lower case so we can later match ignore case.
			user.setEmail(user.getEmail().toLowerCase());
		}
		return super.save(user);
	}

	@Override
	public List<Alias> findAll(UUID pushApplicationId) {
		return find(pushApplicationId).collect(Collectors.toList());
	}

	public Alias findByAlias(String alias) {
		// First query by email
		User fromEmail = findByEmail(alias);
		if (fromEmail != null) {
			return fromEmail;
		}

		// Then query by mobile
		return findByMobile(alias);
	}

	private User findByEmail(String email) {
		Select select = QueryBuilder.select().from(super.tableName).allowFiltering();
		select.where(QueryBuilder.eq("email", email.toLowerCase()));

		return operations.selectOne(select, domainClass);
	}

	private User findByMobile(String mobile) {
		Select select = QueryBuilder.select().from(super.tableName);
		select.where(QueryBuilder.eq("email", mobile.toLowerCase()));

		return operations.selectOne(select, domainClass);
	}

	public Stream<User> find(UUID pushApplicationId) {
		Select select = QueryBuilder.select().from(super.tableName);
		select.where(QueryBuilder.eq("push_application_id", pushApplicationId));

		return operations.stream(select, domainClass);
	}

	@Override
	public Alias find(Alias alias) {
		return super.findOne(new UserKey(alias.getPushApplicationId(), alias.getId()));
	}

	@Override
	public void delete(UUID pushApplicationId) {
		// First query all users.
		find(pushApplicationId).forEach((user) -> {
			delete(user);
		});
	}

	@Override
	public void delete(Alias alias) {
		super.delete(new User().clone(alias));
	}

	@Override
	protected UserKey getId(User entity) {
		return entity.getKey();
	}
}
