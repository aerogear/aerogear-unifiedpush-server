package org.jboss.aerogear.unifiedpush.dao;

import org.jboss.aerogear.unifiedpush.api.ProxyServer;

public interface ProxyServerDao extends GenericBaseDao<ProxyServer, String> {

	ProxyServer find();
	
}
