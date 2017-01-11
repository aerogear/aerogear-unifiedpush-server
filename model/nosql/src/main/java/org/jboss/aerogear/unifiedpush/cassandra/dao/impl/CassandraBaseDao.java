package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.util.CollectionUtils;
import org.springframework.data.cassandra.core.CassandraConverterRowCallback;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.TypedIdCassandraRepository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Select;

public abstract class CassandraBaseDao<T, ID extends Serializable> implements TypedIdCassandraRepository<T, ID> {
	// private final AeroGearLogger logger = AeroGearLogger.getInstance(CassandraBaseDao.class);

	@Autowired
	protected CassandraOperations operations;

	protected final Class<T> domainClass;
	protected String tableName;

	protected abstract ID getId(T entity);

	public CassandraBaseDao(Class<T> domainClass) {
		this.domainClass = domainClass;
	}

	@PostConstruct
	public void initialize(){
		this.tableName = operations.getTableName(domainClass).toString();
	}

	public <S extends T> S update(S entity) {
		return operations.update(entity);
	}

	@Override
	public <S extends T> S save(S entity) {
		return operations.insert(entity);
	}

	@Override
	public <S extends T> List<S> save(Iterable<S> entities) {
		return operations.insert(CollectionUtils.toList(entities));
	}

	@Override
	public T findOne(ID id) {
		return operations.selectOneById(id, domainClass);
	}

	@Override
	public boolean exists(ID id) {
		return operations.exists(id, domainClass);
	}

	@Override
	public long count() {
		return operations.count(domainClass);
	}

	@Override
	public void delete(ID id) {
		operations.deleteById(id, domainClass);
	}

	@Override
	public void delete(T entity) {
		delete(getId(entity));
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		operations.delete(CollectionUtils.toList(entities));
	}

	@Override
	public void deleteAll() {
		operations.truncate(domainClass);
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids) {
		return operations.selectBySimpleIds(ids, domainClass);
	}

	protected List<T> findAll(Select query) {
		return operations.select(query, domainClass);
	}

	protected List<T> getPage(Select query, Integer page, Integer pageSize){
		if (page > 1)
			query.setFetchSize(pageSize * (page-1));
		else
			query.setFetchSize(pageSize);

		ResultSet previousData = operations.getCqlOperations().queryForResultSet(query);

		// TODO - A better impl would use cassandra build in PagingState
		List<T> previousPageResult = new ArrayList<T>();
		Iterator<Row> iterator = previousData.iterator();
		while (previousData.getAvailableWithoutFetching() > 0 && iterator.hasNext()) {
			Row row = iterator.next();
			previousPageResult.add(new CassandraConverterRowCallback<T>(operations.getConverter(), this.domainClass).doWith(row));
		}

		// First page was requested
		if (page == 1) return previousPageResult;

		// Get Next (Relevant) page
		List<T> currentPageResult = new ArrayList<>();
        Iterator<Row> iterator2 = previousData.iterator();
        previousData.fetchMoreResults();
        iterator2.hasNext();
        while (previousData.getAvailableWithoutFetching() > 0 && currentPageResult.size() < pageSize) {
        	Row row = iterator2.next();
        	currentPageResult.add(new CassandraConverterRowCallback<T>(operations.getConverter(), this.domainClass).doWith(row));
        }

		return currentPageResult;
	}

	@Override
	@Deprecated
	public List<T> findAll() {
		throw new UnsupportedOperationException();
	}

}