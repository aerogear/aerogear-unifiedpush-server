package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;


public class DocumentMetadata implements Serializable {
	private static final long serialVersionUID = -7488201263547869851L;

	public static enum DocumentType {
		NULL, APPLICATION, INSTALLATION
	};

	public static DocumentType getPublisher(String publisher) {
		return publisher == null ? DocumentType.NULL : DocumentType.valueOf(publisher.toUpperCase());
	}

	public static String getQualifier(String value) {
		return value == null | value.equalsIgnoreCase(NULL_QUALIFIER) ? NULL_QUALIFIER : value.toUpperCase();
	}

	public static final String NULL_QUALIFIER = "NULL";
	public static final String NULL_ALIAS = "NULL";

	private String id;
	private PushApplication pushApplication;
	private DocumentType publisher;
	private String alias;
	private String qualifier;
	private Boolean latest;

	// When reading documents from directories, this property contain
	// file lastModified attribute and not timestamp from file parts.
	private Long timestamp;

	public DocumentMetadata() {
	}

	public DocumentMetadata(DocumentMetadata other) {
		this.pushApplication = other.pushApplication;
		this.publisher = other.publisher;
		this.alias = other.alias;
		this.qualifier = other.qualifier;
		this.latest = other.latest;
		this.timestamp = other.timestamp;
		this.id = other.id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DocumentType getPublisher() {
		return publisher;
	}

	public void setPublisher(DocumentType publisher) {
		this.publisher = publisher;
	}

	public PushApplication getPushApplication() {
		return pushApplication;
	}

	public void setPushApplication(PushApplication pushApplication) {
		this.pushApplication = pushApplication;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public Boolean getLatest() {
		return latest;
	}

	public void setLatest(Boolean latest) {
		this.latest = latest;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
