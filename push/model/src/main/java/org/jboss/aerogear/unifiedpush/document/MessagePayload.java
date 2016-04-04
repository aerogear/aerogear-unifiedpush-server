package org.jboss.aerogear.unifiedpush.document;

import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class MessagePayload {
	private UnifiedPushMessage pushMessage;
	private String payload;

	// Payload qualifier, Optional - if missing DocumentMetadata.NULL_QUALIFIER will be used.
	private String qualifier;

	// Payload Id, Optional - if missing DocumentMetadata.NULL_ID will be used.
	private String id;

	public MessagePayload() {
	}

	public MessagePayload(UnifiedPushMessage pushMessage, String payload, String qualifier, String id) {
		super();
		this.pushMessage = pushMessage;
		this.payload = payload;
		this.qualifier = qualifier;
		this.id = id;
	}

	public MessagePayload(UnifiedPushMessage pushMessage, String payload) {
		super();
		this.pushMessage = pushMessage;
		this.payload = payload;
	}

	public UnifiedPushMessage getPushMessage() {
		return pushMessage;
	}

	public void setPushMessage(UnifiedPushMessage pushMessage) {
		this.pushMessage = pushMessage;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
