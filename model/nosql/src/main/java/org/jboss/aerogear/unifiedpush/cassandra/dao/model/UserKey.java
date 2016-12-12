package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import com.datastax.driver.core.utils.UUIDs;

@PrimaryKeyClass
public class UserKey implements Serializable {
	private static final long serialVersionUID = -6419944724251681328L;

	@PrimaryKeyColumn(name = "push_application_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID pushApplicationId;
	@PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private UUID id;

	public UserKey() {
	}

	public UserKey(UUID pushApplicationId) {
		super();
		this.pushApplicationId = pushApplicationId;
		this.id = UUIDs.timeBased();
	}

	public UserKey(UUID pushApplicationId, UUID id) {
		super();
		this.pushApplicationId = pushApplicationId;
		this.id = id;
	}

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(UUID pushApplicationId) {
		this.pushApplicationId = pushApplicationId;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
