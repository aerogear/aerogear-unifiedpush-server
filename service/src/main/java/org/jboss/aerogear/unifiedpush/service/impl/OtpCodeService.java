package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;

public interface OtpCodeService {

	OtpCode save(OtpCodeKey key);

	void delete(OtpCodeKey key);

	OtpCode findOne(OtpCodeKey id);
}
