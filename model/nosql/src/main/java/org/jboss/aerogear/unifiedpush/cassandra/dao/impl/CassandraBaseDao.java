package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CassandraAccessor;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;

import com.datastax.driver.core.ConsistencyLevel;

public abstract class CassandraBaseDao<T, ID extends Serializable> extends SimpleCassandraRepository<T, ID> {

	@Autowired
	protected CassandraOperations operations;
	protected String tableName;
	protected final Class<T> domainClass;

	public CassandraBaseDao(Class<T> domainClass, CassandraEntityInformation<T, ID> metadata,
			CassandraOperations operations) {
		super(metadata, operations);

		this.domainClass = domainClass;
		this.tableName = metadata.getTableName().getUnquoted();

		if (CassandraAccessor.class.isAssignableFrom(operations.getCqlOperations().getClass())) {
			// Used for Select Queries
			((CassandraAccessor) operations.getCqlOperations()).setConsistencyLevel(ConsistencyLevel.QUORUM);
		}
	}

	@Override
	public <S extends T> S insert(S entity) {
		throw new UnsupportedOperationException("TypedIdCassandraRepository is @Deprecated, use CassandraRepository.save");
	}

	@Override
	public <S extends T> List<S> insert(Iterable<S> entities) {
		throw new UnsupportedOperationException("TypedIdCassandraRepository is @Deprecated, use CassandraRepository.save");
	}




}