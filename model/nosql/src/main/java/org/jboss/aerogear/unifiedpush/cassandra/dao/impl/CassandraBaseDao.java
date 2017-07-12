package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.TypedIdCassandraRepository;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory;
import org.springframework.util.Assert;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

public abstract class CassandraBaseDao<T, ID extends Serializable> implements TypedIdCassandraRepository<T, ID> {
	@Autowired
	protected CassandraOperations operations;

	private CassandraRepositoryFactory factory;
	private CassandraEntityInformation<T, ID> entityInformation;

	protected final Class<T> domainClass;
	protected String tableName;

	protected abstract ID getId(T entity);

	public CassandraBaseDao(Class<T> domainClass) {
		this.domainClass = domainClass;
	}

	@PostConstruct
	public void initialize(){
		factory = new CassandraRepositoryFactory(operations);
		entityInformation = factory.getEntityInformation(domainClass);
		tableName = entityInformation.getTableName().getUnquoted();
	}


	@Override
	public <S extends T> S insert(S entity) {
		Assert.notNull(entity, "Entity must not be null");

		return operations.insert(entity);
	}

	@Override
	public <S extends T> List<S> insert(Iterable<S> entities) {
		Assert.notNull(entities, "The given Iterable of entities must not be null");

		List<S> result = new ArrayList<>();

		for (S entity : entities) {

			S saved = operations.insert(entity);

			if (saved != null) {
				result.add(saved);
			}
		}

		return result;
	}

	@Override
	public <S extends T> S save(S entity) {
		Assert.notNull(entity, "Entity must not be null");

		if (entityInformation.isNew(entity)) {
			return operations.insert(entity);
		} else {
			return operations.update(entity);
		}
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null");
		List<S> result = new ArrayList<>();
		for (S entity : entities) {

			S saved;

			if (entityInformation.isNew(entity)) {
				saved = operations.insert(entity);
			} else {
				saved = operations.update(entity);
			}

			if (saved != null) {
				result.add(saved);
			}
		}

		return result;
	}

	@Override
	public Optional<T> findById(ID id) {
		Assert.notNull(id, "The given id must not be null");

		return Optional.ofNullable(operations.selectOneById(id, entityInformation.getJavaType()));
	}

	@Override
	public boolean existsById(ID id) {
		Assert.notNull(id, "The given id must not be null");

		return operations.exists(id, entityInformation.getJavaType());
	}

	@Override
	public long count() {
		return operations.count(entityInformation.getJavaType());
	}

	@Override
	public void deleteById(ID id) {
		operations.deleteById(id, domainClass);
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "The given entity must not be null");

		deleteById(entityInformation.getId(entity)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Cannot obtain Id from [%s]", entity))));
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		Assert.notNull(entities, "The given Iterable of entities must not be null");

		entities.forEach(operations::delete);
	}

	@Override
	public void deleteAll() {
		operations.truncate(entityInformation.getJavaType());
	}

	@Override
	public List<T> findAllById(Iterable<ID> ids) {
		Assert.notNull(ids, "The given Iterable of id's must not be null");

		return operations.selectBySimpleIds(ids, entityInformation.getJavaType());
	}

	@Override
	public List<T> findAll() {
		Select select = QueryBuilder.select().all().from(entityInformation.getTableName().toCql());

		return operations.select(select, entityInformation.getJavaType());
	}

	protected List<T> findAll(Select query) {
		return operations.select(query, domainClass);
	}
}