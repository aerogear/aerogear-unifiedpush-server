package org.jboss.aerogear.unifiedpush.document;

import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class MessagePayload {
	private UnifiedPushMessage pushMessage;
	private String payload;

	public MessagePayload() {
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
}
