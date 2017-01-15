package org.jboss.aerogear.unifiedpush.cassandra.dao;

import java.time.Instant;
import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;

public enum NullUUID {

	NULL(UUIDs.startOf(Instant.EPOCH.toEpochMilli()));

	// Members
	private UUID uuid;

	// Constructor
	NullUUID(UUID uuid) {
		this.uuid = uuid;
	}

	// Getters
	public UUID getUuid() {
		return this.uuid;
	}
}