package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Alias;

public interface AliasDao extends GenericBaseDao<Alias, Long> {

	List<Alias> findAliasesByPushApplicationID(String pushApplicationID);

	int deleteByPushApplicationID(String pushApplicationID);

	Alias findByName(String alias);

}
