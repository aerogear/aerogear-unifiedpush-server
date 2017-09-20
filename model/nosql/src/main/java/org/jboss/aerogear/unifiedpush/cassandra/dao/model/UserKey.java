package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class UserKey implements Serializable {
	private static final long serialVersionUID = -6419944724251681328L;
	public static final String FIELD_PUSH_APPLICATION_ID = "push_application_id";
	public static final String FIELD_USER_ID = "user_id";
	public static final String FIELD_ALIAS = "alias";

	@PrimaryKeyColumn(name = FIELD_PUSH_APPLICATION_ID, ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID pushApplicationId;

	@PrimaryKeyColumn(name = FIELD_USER_ID, ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private UUID id;

	@PrimaryKeyColumn(name = FIELD_ALIAS, ordinal = 0, type = PrimaryKeyType.CLUSTERED)
	private String alias;

	public UserKey() {
	}

	public UserKey(UUID pushApplicationId, UUID id) {
		super();
		this.pushApplicationId = pushApplicationId;
		this.id = id;
	}

	public UserKey(UUID pushApplicationId, UUID id, String alias) {
		this(pushApplicationId, id);
		this.alias = alias;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(UUID pushApplicationId) {
		this.pushApplicationId = pushApplicationId;
	}

}
