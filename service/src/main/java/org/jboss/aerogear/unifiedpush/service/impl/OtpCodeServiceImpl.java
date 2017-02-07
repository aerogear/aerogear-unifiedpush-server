package org.jboss.aerogear.unifiedpush.service.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.jboss.aerogear.unifiedpush.cassandra.dao.OtpCodeDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCode;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.OtpCodeKey;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

@Stateless
@Interceptors(SpringContextInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
		return codeDao.findOne(id);
	}
}
