package org.jboss.aerogear.unifiedpush.api.document;

import java.io.Serializable;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;

public class DocumentMetadata implements Serializable {
	private static final long serialVersionUID = -7488201263547869851L;

	public static String getDatabase(String value) {
		return value == null || value.equalsIgnoreCase(NULL_DATABASE) ? NULL_DATABASE : value.toUpperCase();
	}

	public static String getAlias(String value) {
		return value == null || value.length() == 0 || value.equalsIgnoreCase(NULL_ALIAS) ? NULL_ALIAS : value;
	}

	public static String getId(String value) {
		return value == null || value.length() == 0 || value.equalsIgnoreCase(NULL_ID) ? null : value;
	}

	public static final String NULL_DATABASE = "NULL";
	public static final String NULL_ALIAS = "NULL";
	private static final String NULL_ID = "NULL";

	private UUID pushApplicationId;
	private String database;
	private UUID userId;
	private String documentId;

	/*
	 * InstallationId is optional and should be used only when userId (alias) is
	 * null (Anonymous mode)
	 */
	private String installationId; // AKA anonymous

	// None Persistence property
	private UUID snapshot;

	public DocumentMetadata() {
	}

	private DocumentMetadata(UUID pushApplicationId, String database) {
		this.pushApplicationId = pushApplicationId;
		this.database = getDatabase(database);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias) {
		this(UUID.fromString(pushApplicationId), database, alias);
	}

	public DocumentMetadata(UUID pushApplicationId, //
			String database, //
			Alias alias) {

		this(pushApplicationId, database, alias, null, null, null);
	}

	public DocumentMetadata(UUID pushApplicationId, //
			String database, //
			Alias alias, //
			String documentId) {

		this(pushApplicationId, database, alias, null, documentId, null);
	}

	public DocumentMetadata(String pushApplicationId, //
			String database, //
			Alias alias, //
			String documentId) {

		this(UUID.fromString(pushApplicationId), database, alias, documentId);
	}

	public DocumentMetadata(UUID pushApplicationId, //
			String database, //
			Alias alias, //
			String documentId, //
			UUID snapshot) {

		this(pushApplicationId, database, alias, null, documentId, snapshot);
	}

	public DocumentMetadata(UUID pushApplicationId, //
			String database, //
			Alias alias, //
			String installationId, //
			String documentId, //
			UUID snapshot) {

		this(pushApplicationId, database);
		this.setUserId(alias != null ? alias.getId() : null);
		this.setInstallationId((installationId == null || installationId.length() == 0 ? null : installationId));
		this.setDocumentId(documentId);
		this.setSnapshot(snapshot);
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public UUID getPushApplicationId() {
		return pushApplicationId;
	}

	public void setPushApplicationId(UUID pushApplicationId) {
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
