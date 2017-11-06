package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;

public abstract class CassandraBaseDao<T, ID extends Serializable> extends SimpleCassandraRepository<T, ID> {

	@Autowired
	protected CassandraOperations operations;
	protected String tableName;
	protected final Class<T> domainClass;

	public CassandraBaseDao(Class<T> domainClass, CassandraEntityInformation<T, ID> metadata, CassandraOperations operations) {
		super(metadata, operations);
		this.domainClass = domainClass;
		this.tableName = metadata.getTableName().getUnquoted();;
	}
}