package org.jboss.aerogear.unifiedpush.cassandra.dao.impl;

import java.io.Serializable;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.Database;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class DocumentKey implements Serializable {
	private static final long serialVersionUID = 5338182709484691924L;

	/**
	 * COMPOSITE (aka COMPOUND) PrimaryKey Columns which is responsible for data
	 * distribution across all nodes.
	 *
	 * When running in anonymous mode (e.g no alias), device id (AKA token_id)
	 * is used as PK part.
	 */
	@PrimaryKeyColumn(name = "push_application_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID pushApplicationId;
	@PrimaryKeyColumn(name = "database", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String database;

	// User id can be NullUUID ('00000000-0000-0000-0000-000000000000') AKA
	// global doc.
	@PrimaryKeyColumn(name = "user_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
	private UUID userId;

	@PrimaryKeyColumn(name = "snapshot", ordinal = 0, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	UUID snapshot;

	public DocumentKey() {
	}

	public DocumentKey(DocumentMetadata metadata) {
		this.pushApplicationId = UUID.fromString(metadata.getPushApplicationId());
		this.database = metadata.getDatabase();

		if (metadata.getUserId() == null) {
			// Global mode - Document doesn't belong to specific
			// user/installation.
			this.userId = NullUUID.NULL.getUuid();
		} else {
			this.userId = metadata.getUserId();
		}
	}

	public DocumentKey(Database database) {
		this(database.getKey().getPushApplicationId(), database.getDatabase());
	}

	public DocumentKey(UUID pushApplicationId, String database) {
		this.pushApplicationId = pushApplicationId;
		this.database = database;
		this.userId = NullUUID.NULL.getUuid();
	}

	/**
	 * Protected constructor, used only from dao layer.
	 */
	DocumentKey(UUID pushApplicationId, String database, UUID userId) {
		this.pushApplicationId = pushApplicationId;
		this.database = database;
		this.userId = userId;
	}

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(String pushApplicationId) {
		this.pushApplicationId = UUID.fromString(pushApplicationId);
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getSnapshot() {
		return snapshot;
	}
}
