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
		return value == null || value.length() == 0 || value.equalsIgnoreCase(NULL_ID) ? null : value;
	}

	public static final String NULL_DATABASE = "NULL";
	public static final String NULL_ALIAS = "NULL";
	private static final String NULL_ID = "NULL";

	private String documentId;
	private String pushApplicationId;
	private String database;
	private UUID userId;

	/*
	 * InstallationId is optional and should be used only when userId (alias) is
	 * null (Anonymous mode)
	 */
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
			Alias alias){

		this(pushApplicationId, database, alias, null, null, null);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String documentId) {

		this(pushApplicationId, database, alias, null, documentId, null);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String documentId, //
			String snapshot) {

		this(pushApplicationId, database, alias, null, documentId, snapshot);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String installationId, //
			String documentId, //
			String snapshot) {

		this.setPushApplicationId(pushApplicationId);
		this.setUserId(alias != null ? alias.getId() : null);
		this.setInstallationId((installationId == null || installationId.length() == 0 ? null : installationId));
		this.setDatabase(database);
		this.setDocumentId(documentId);
		this.setSnapshot(snapshot == null || installationId.length() == 0 ? null : UUID.fromString(snapshot));
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
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
