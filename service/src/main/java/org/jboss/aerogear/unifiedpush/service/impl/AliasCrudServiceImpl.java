/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.service.AliasCrudService;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

@Stateless
@Interceptors(SpringContextInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AliasCrudServiceImpl implements AliasCrudService {

	@Autowired
	private AliasDao aliasDao;

	@Override
	public void remove(String alias) {
		aliasDao.delete(aliasDao.findByAlias(alias));
	}

	@Override
	public Alias find(String alias) {
		return aliasDao.findByAlias(alias);
	}

	@Override
	public void removeAll(UUID pushApplicationId) {
		aliasDao.delete(pushApplicationId);
	}

	@Override
	public void create(Alias alias) {
		aliasDao.create(alias);
	}
}