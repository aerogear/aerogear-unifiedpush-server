package org.jboss.aerogear.unifiedpush.service;

import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata.DocumentType;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

public interface DocumentService {

	void saveForPushApplication(PushApplication pushApp, String alias, String content, String qualifier, String id, boolean overwrite);

	List<DocumentMessage> getDocuments(PushApplication pushApplication, DocumentType publisher);
	
	String getLatestDocumentForAlias(Variant variant, DocumentType publisher, String alias, String qualifier);

	void saveForAliases(PushApplication pushApplication, Map<String, String> aliasToDocument, String qualifier, String id,
			boolean overwrite);
	
	List<String> getLatestDocumentsForApplication(PushApplication pushApp, String qualifer, String id);
}
