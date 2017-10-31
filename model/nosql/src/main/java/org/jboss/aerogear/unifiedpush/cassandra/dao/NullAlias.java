package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;

public class NullAlias {

	public static Alias getAlias(String pushApplicationId) {
		return new Alias(UUID.fromString(pushApplicationId), NullUUID.NULL.getUuid(), null);
	}

	public static Alias getAlias(UUID pushApplicationId) {
		return new Alias(pushApplicationId, NullUUID.NULL.getUuid(), null);
	}

	public static boolean isNullAlias(Alias alias) {
		return NullUUID.NULL.getUuid().equals(alias.getId());
	}
}
