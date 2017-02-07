package org.jboss.aerogear.unifiedpush.cassandra.dao;

import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.repository.CrudRepository;

public interface OtpCodeDao extends CrudRepository<OtpCode, OtpCodeKey> {
	OtpCode save(OtpCode entity, WriteOptions options);

	void deleteAll(OtpCodeKey id);
}
