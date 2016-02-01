package org.jboss.aerogear.unifiedpush.document;

import java.util.Map;

import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class DocumentDeployRequest {
	private String qualifier;
	private UnifiedPushMessage pushMessage;
	private Map<String, String> aliasToDocument;

	public DocumentDeployRequest() {
	}

	public DocumentDeployRequest(String qualifier, UnifiedPushMessage pushMessage, Map<String, String> aliasToDocument) {
		super();
		this.qualifier = qualifier;
		this.pushMessage = pushMessage;
		this.aliasToDocument = aliasToDocument;
	}

	public UnifiedPushMessage getPushMessage() {
		return pushMessage;
	}

	public void setPushMessage(UnifiedPushMessage pushMessage) {
		this.pushMessage = pushMessage;
	}

	public Map<String, String> getAliasToDocument() {
		return aliasToDocument;
	}

	public void setAliasToDocuments(Map<String, String> aliasToDocument) {
		this.aliasToDocument = aliasToDocument;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

}
