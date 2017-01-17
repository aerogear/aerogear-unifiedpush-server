package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.util.UUID;

public class AliasAlreadyExists extends RuntimeException {
	private static final long serialVersionUID = 1874258278307431759L;

	public final String alias;
	public final UUID pushApplicationId;

	private AliasAlreadyExists(String alias, UUID pushApplicationId, String msg) {
		super(msg);
		this.alias = alias;
		this.pushApplicationId = pushApplicationId;
	}

	public AliasAlreadyExists(String alias, UUID pushApplicationId) {
		this(alias, pushApplicationId,
				String.format("Cannot add already existing alias \"%s\"", alias));
	}

	public AliasAlreadyExists(String alias) {
		this(alias, null);
	}
}
