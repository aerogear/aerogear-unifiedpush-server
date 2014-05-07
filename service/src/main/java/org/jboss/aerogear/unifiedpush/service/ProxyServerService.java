package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.ProxyServer;

public interface ProxyServerService {
	
	void addProxy(ProxyServer proxy);
	
	void updateProxy(ProxyServer proxy);
	
	ProxyServer findByProxyID(String proxyID);
	
	ProxyServer findProxy();
	
	void removeProxy(ProxyServer proxy);
}
