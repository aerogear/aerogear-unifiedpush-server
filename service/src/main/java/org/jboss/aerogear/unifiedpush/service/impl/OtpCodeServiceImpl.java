package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.unifiedpush.cassandra.dao.OtpCodeDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OtpCodeServiceImpl implements OtpCodeService {

	@Autowired
	private OtpCodeDao codeDao;

	@Override
	public OtpCode save(OtpCodeKey key) {
		return codeDao.save(new OtpCode(key));
	}

	@Override
	public void delete(OtpCodeKey key) {
		codeDao.deleteAll(key);
	}

	@Override
	public OtpCode findOne(OtpCodeKey id) {
		return codeDao.findById(id).orElse(null);
	}
}
