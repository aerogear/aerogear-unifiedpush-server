package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import org.jboss.aerogear.unifiedpush.cassandra.dao.OtpCodeDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;

@Repository
class OtpCodeDaoImpl extends CassandraBaseDao<OtpCode, OtpCodeKey> implements OtpCodeDao {
	private static final int CODE_TTL = 60 * 60; // 1 hours in seconds
	private static final WriteOptions writeOptions = new WriteOptions();

	public OtpCodeDaoImpl() {
		super(OtpCode.class);
		writeOptions.setTtl(CODE_TTL);
	}

	@Override
	protected OtpCodeKey getId(OtpCode entity) {
		return entity.getKey();
	}

	@SuppressWarnings("unchecked")
	@Override
	public OtpCode save(OtpCode entity) {
		return operations.insert(entity, writeOptions);
	}

	@Override
	public OtpCode save(OtpCode entity, WriteOptions options) {
		return operations.insert(entity, options);
	}

	public void deleteAll(OtpCodeKey id) {
		Delete delete = QueryBuilder.delete().from(super.tableName);
		delete.where(QueryBuilder.eq(OtpCodeKey.FIELD_VARIANT_ID, id.getVariantId()));
		delete.where(QueryBuilder.eq(OtpCodeKey.FIELD_TOKEN_ID, id.getTokenId()));

		operations.getCqlOperations().execute(delete);
	}
}
