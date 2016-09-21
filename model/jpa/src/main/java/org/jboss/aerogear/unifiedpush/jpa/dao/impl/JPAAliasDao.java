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
	public List<Alias> findAliasesByPushApplicationID(String pushApplicationID) {
		List<Alias> results = entityManager
				.createQuery(
						"select a from Alias a where a.pushApplicationID = :pushApplicationID",
						Alias.class)
				.getResultList();

		if (results.isEmpty()) {
			return null;
		}

		return results;
	}

	@Override
	/**
	 * Match alias by name ignore case
	 */
	public Alias findByName(String alias) {
		List<Alias> results = entityManager
				.createQuery("select a from Alias a where a.name = :name", Alias.class)
				.setParameter("name", alias.toLowerCase()).getResultList();

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0);
	}

	@Override
	public void create(Alias entity) {
		// Keep aliases as lower case so we can later match ignore case.
		entity.setName(entity.getName().toLowerCase());
		super.create(entity);
	}
}