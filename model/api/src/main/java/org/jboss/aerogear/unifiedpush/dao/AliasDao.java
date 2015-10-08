package org.jboss.aerogear.unifiedpush.dao;

import org.jboss.aerogear.unifiedpush.api.Alias;

public interface AliasDao extends GenericBaseDao<Alias, Long> {

	int deleteByPushApplicationID(String pushApplicationID);

	Alias findByName(String alias);
	
}
