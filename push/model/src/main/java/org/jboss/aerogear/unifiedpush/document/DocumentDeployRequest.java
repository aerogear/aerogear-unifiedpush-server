package org.jboss.aerogear.unifiedpush.document;

import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class DocumentDeployRequest {
	private String type;
	private UnifiedPushMessage pushMessage;
	private Map<String, List<String>> aliasToDocuments;
	
	public DocumentDeployRequest() {}
	
	public DocumentDeployRequest(String type, UnifiedPushMessage pushMessage,
			Map<String, List<String>> aliasToDocuments) {
		super();
		this.type = type;
		this.pushMessage = pushMessage;
		this.aliasToDocuments = aliasToDocuments;
	}
	
	public UnifiedPushMessage getPushMessage() {
		return pushMessage;
	}
	public void setPushMessage(UnifiedPushMessage pushMessage) {
		this.pushMessage = pushMessage;
	}
	public Map<String, List<String>> getAliasToDocuments() {
		return aliasToDocuments;
	}
	public void setAliasToDocuments(Map<String, List<String>> aliasToDocuments) {
		this.aliasToDocuments = aliasToDocuments;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
