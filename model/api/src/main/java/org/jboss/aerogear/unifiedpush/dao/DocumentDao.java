package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;

public interface DocumentDao {
	
	void create(DocumentMessage document, boolean overwrite);
	
	DocumentMessage findLatestDocumentForAlias(DocumentMetadata message);
	
	List<DocumentMessage> findLatestDocumentsForApplication(DocumentMetadata message);
	
	List<DocumentMessage> findDocuments(DocumentMetadata message);
	
}
