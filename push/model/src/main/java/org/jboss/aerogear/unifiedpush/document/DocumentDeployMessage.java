package org.jboss.aerogear.unifiedpush.document;

import java.util.ArrayList;
import java.util.List;

/**
 * Store/Send several messages using a single API.
 */
public class DocumentDeployMessage {
	private List<MessagePayload> payloads;

	public DocumentDeployMessage() {
	}

	public DocumentDeployMessage(List<MessagePayload> payloads) {
		super();
		this.payloads = payloads;
	}

	public List<MessagePayload> getPayloads() {
		return payloads;
	}

	public void setPayloads(List<MessagePayload> payloads) {
		this.payloads = payloads;
	}

	public void addPayload(MessagePayload payload){
		if (payloads==null){
			payloads = new ArrayList<>();
		}

		payloads.add(payload);
	}

}
