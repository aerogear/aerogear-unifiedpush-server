package org.jboss.aerogear.unifiedpush.document;

import java.util.Map;

public class DocumentDeployMessage {
	// Optional - if missing DocumentMetadata.NULL_QUALIFIER will be used.
	private String qualifier;

	/*
	 * Optional - Global message payload. When globalPayload.UnifiedPushMessage
	 * exists, alias.UnifiedPushMessage will be ignored.
	 */
	private MessagePayload globalPayload;

	// Map between aliases and payloads
	private Map<String, MessagePayload> aliasPayload;

	public DocumentDeployMessage() {
	}

	public DocumentDeployMessage(String qualifier, MessagePayload globalPayload,
			Map<String, MessagePayload> aliasPayload) {
		super();
		this.qualifier = qualifier;
		this.globalPayload = globalPayload;
		this.aliasPayload = aliasPayload;
	}

	public Map<String, MessagePayload> getAliasPayload() {
		return aliasPayload;
	}

	public void setAliasPayload(Map<String, MessagePayload> aliasPayload) {
		this.aliasPayload = aliasPayload;
	}

	public MessagePayload getGlobalPayload() {
		return globalPayload;
	}

	public void setGlobalPayload(MessagePayload globalPayload) {
		this.globalPayload = globalPayload;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

}
