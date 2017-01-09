package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;
import java.util.UUID;

public class DocumentMetadata implements Serializable {
	private static final long serialVersionUID = -7488201263547869851L;

	public static String getDatabase(String value) {
		return value == null || value.equalsIgnoreCase(NULL_DATABASE) ? NULL_DATABASE : value.toUpperCase();
	}

	public static String getAlias(String value) {
		return value == null || value.equalsIgnoreCase(NULL_ALIAS) ? NULL_ALIAS : value;
	}

	public static String getId(String value) {
		return value == null || value.equalsIgnoreCase(NULL_ID) ? NULL_ID : value;
	}

	public static final String NULL_DATABASE = "NULL";
	public static final String NULL_ALIAS = "NULL";
	public static final String NULL_ID = "NULL";

	private String id;
	private String pushApplicationId;
	private String database;

	/*
	 * InstallationId is optional and should be used only when alias is null
	 * (Anonymous mode)
	 */
	private UUID userId;
	private String installationId; // AKA anonymous

	// None Persistence property
	private UUID snapshot;

	public DocumentMetadata() {
	}


	public DocumentMetadata(String pushApplicationId, String database) {
		this.pushApplicationId = pushApplicationId;
		this.database = database;
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String id) {

		this(pushApplicationId, database, alias, null, id, null);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String id, //
			String snapshot) {

		this(pushApplicationId, database, alias, null, id, snapshot);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String installationId, //
			String id, //
			String snapshot) {

		this.setPushApplicationId(pushApplicationId);
		this.setUserId(alias != null ? alias.getId() : null);
		this.setInstallationId((installationId != null && installationId.length() > 0 ? installationId : null));
		this.setDatabase(database);
		this.setSnapshot(snapshot == null ? null : UUID.fromString(snapshot));
		this.setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(String pushApplicationId) {
		this.pushApplicationId = pushApplicationId;
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

	public String getInstallationId() {
		return installationId;
	}

	public void setInstallationId(String installationId) {
		this.installationId = installationId;
	}

	public UUID getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(UUID snapshot) {
		this.snapshot = snapshot;
	}
}
