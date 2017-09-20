package org.jboss.aerogear.unifiedpush.cassandra.dao;

import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.repository.CrudRepository;

public interface OtpCodeDao extends CrudRepository<OtpCode, OtpCodeKey> {
	OtpCode save(OtpCode entity, InsertOptions options);

	void deleteAll(OtpCodeKey id);
}
