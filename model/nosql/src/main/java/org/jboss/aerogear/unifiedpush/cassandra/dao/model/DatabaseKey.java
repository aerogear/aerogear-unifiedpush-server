package org.jboss.aerogear.unifiedpush.cassandra.dao.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class DatabaseKey implements Serializable {
	private static final long serialVersionUID = -6419944724251681328L;

	@PrimaryKeyColumn(name = "push_application_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID pushApplicationId;
	@PrimaryKeyColumn(name = "snapshot", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private Date snapshot;

	public DatabaseKey() {
	}

	public DatabaseKey(UUID pushApplicationId) {
		super();
		this.pushApplicationId = pushApplicationId;
		this.snapshot = new Date();
	}

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(UUID pushApplicationId) {
		this.pushApplicationId = pushApplicationId;
	}
}
