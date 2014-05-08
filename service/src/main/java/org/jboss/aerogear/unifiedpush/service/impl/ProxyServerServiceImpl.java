package org.jboss.aerogear.unifiedpush.service.impl;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.ProxyServer;
import org.jboss.aerogear.unifiedpush.dao.ProxyServerDao;
import org.jboss.aerogear.unifiedpush.service.ProxyServerService;

public class ProxyServerServiceImpl implements ProxyServerService {

	@Inject
	private ProxyServerDao proxyDao;
	
	@Override
	public void addProxy(ProxyServer proxy) {
		proxyDao.create(proxy);
	}

	@Override
	public void updateProxy(ProxyServer proxy) {
		proxyDao.update(proxy);
	}

	@Override
	public ProxyServer findByProxyID(String proxyID) {
		return proxyDao.find(proxyID);
	}
	
	@Override
	public ProxyServer findProxy() {
		return proxyDao.find();
	}

	@Override
	public void removeProxy(ProxyServer proxy) {
		proxyDao.delete(proxy);
	}

}
