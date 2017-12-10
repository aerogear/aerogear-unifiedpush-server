package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import org.jboss.aerogear.unifiedpush.cassandra.dao.OtpCodeDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.cql.CassandraAccessor;
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;

@Repository
class OtpCodeDaoImpl extends CassandraBaseDao<OtpCode, OtpCodeKey> implements OtpCodeDao {
	private static final int CODE_TTL = 60 * 60; // 1 hours in seconds

	// Since we use a custom InsertOptions, we should also set
	// ConsistencyLevel.QUORUM
	private static final InsertOptions writeOptions = InsertOptions.builder().consistencyLevel(ConsistencyLevel.QUORUM)
			.ttl(CODE_TTL).build();

	public OtpCodeDaoImpl(@Autowired CassandraOperations operations) {
		super(OtpCode.class, new CassandraRepositoryFactory(operations).getEntityInformation(OtpCode.class),
				operations);

		Assert.isTrue(
				((CassandraAccessor) operations.getCqlOperations()).getConsistencyLevel() == ConsistencyLevel.QUORUM,
				"ConsistencyLevel Must be QUORUM");
	}

	@SuppressWarnings("unchecked")
	@Override
	public OtpCode save(OtpCode entity) {
		operations.insert(entity, writeOptions);
		return entity;
	}

	@Override
	public OtpCode save(OtpCode entity, InsertOptions options) {
		operations.insert(entity, options);
		return entity;
	}

	public void deleteAll(OtpCodeKey id) {
		Delete delete = QueryBuilder.delete().from(super.tableName);
		delete.where(QueryBuilder.eq(OtpCodeKey.FIELD_VARIANT_ID, id.getVariantId()));
		delete.where(QueryBuilder.eq(OtpCodeKey.FIELD_TOKEN_ID, id.getTokenId()));

		operations.getCqlOperations().execute(delete);
	}
}
