package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;

public interface DocumentDao {
	
	void create(DocumentMessage document, boolean overwrite);
	
	DocumentMessage findLatestDocument(DocumentMessage message);
	
	List<DocumentMessage> findDocuments(DocumentMessage message);
	
}
