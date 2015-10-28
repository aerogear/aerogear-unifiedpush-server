package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.dao.AliasDao;

public class JPAAliasDao extends JPABaseDao<Alias, Long> implements AliasDao {

	@Override
	public Class<Alias> getType() {
		return Alias.class;
	}

	@Override
	public int deleteByPushApplicationID(String pushApplicationID) {
		return entityManager
				.createQuery(
						"delete from Alias a "
								+ "where a.pushApplicationID = :pushApplicationID")
				.setParameter("pushApplicationID", pushApplicationID).executeUpdate();
	}

	@Override
	public Alias findByName(String alias) {
		List<Alias> results = entityManager
				.createQuery("select a from Alias a where a.name = :name", Alias.class)
				.setParameter("name", alias).getResultList();
		
		if (results.isEmpty()) {
			return null;
		}
		
		return results.get(0);
	}

}
