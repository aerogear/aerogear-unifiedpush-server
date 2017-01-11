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
		return save(user);
	}

	@Override
	@SuppressWarnings("unchecked")
	public User save(User entity) {
		// Double check alias doesn't exists
		User user = findOne(entity.getKey());
		if (user == null)
			return super.save(entity);

		return update(entity);
	}

	@Override
	public List<Alias> findAll(UUID pushApplicationId) {
		return find(pushApplicationId).collect(Collectors.toList());
	}

	@Override
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
		select.where(QueryBuilder.eq("mobile", mobile.toLowerCase()));

		return operations.selectOne(select, domainClass);
	}

	public Stream<User> find(UUID pushApplicationId) {
		Select select = QueryBuilder.select().from(super.tableName);
		select.where(QueryBuilder.eq("push_application_id", pushApplicationId));

		return operations.stream(select, domainClass);
	}

	public void removeAll(UUID pushApplicationId) {
		find(pushApplicationId).forEach((user) -> {
			delete(user.getKey());
		});
	}

	@Override
	public void remove(String alias) {
		Alias aliasObj = findByAlias(alias);
		if (aliasObj != null)
			delete(new UserKey(aliasObj.getPushApplicationId(), aliasObj.getId()));
	}

	@Override
	protected UserKey getId(User entity) {
		return entity.getKey();
	}
}
