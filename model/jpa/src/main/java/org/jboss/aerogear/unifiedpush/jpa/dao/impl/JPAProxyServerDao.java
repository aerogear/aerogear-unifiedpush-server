package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.ProxyServer;
import org.jboss.aerogear.unifiedpush.dao.ProxyServerDao;

public class JPAProxyServerDao extends JPABaseDao implements ProxyServerDao {

	@Override
	public void create(ProxyServer proxy) {
		persist(proxy);
	}

	@Override
	public void update(ProxyServer proxy) {
		merge(proxy);	
	}

	@Override
	public void delete(ProxyServer proxy) {
		ProxyServer entity = entityManager.find(ProxyServer.class, proxy.getId());
        remove(entity);
	}

	@Override
	public ProxyServer find(String id) {
		ProxyServer entity = entityManager.find(ProxyServer.class, id);

        return entity;
	}

	@Override
	public ProxyServer find() {
		List<ProxyServer> result = createQuery("select pr from ProxyServer pr").getResultList();
		
		if (!result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}
}
