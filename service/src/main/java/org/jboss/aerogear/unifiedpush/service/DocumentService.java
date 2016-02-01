package org.jboss.aerogear.unifiedpush.service;

import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage.DocumentType;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

public interface DocumentService {

	void saveForPushApplication(String deviceToken, Variant variant, String content, String qualifier);

	List<DocumentMessage> getDocuments(PushApplication pushApplication, DocumentType publisher);
	
	String getLatestDocument(Variant variant, DocumentType publisher, String alias, String qualifier);

	void saveForAliases(PushApplication pushApplication, Map<String, String> aliasToDocument, String qualifier);
}
