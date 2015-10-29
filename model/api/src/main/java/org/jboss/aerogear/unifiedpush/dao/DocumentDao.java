package org.jboss.aerogear.unifiedpush.dao;

import java.util.Date;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface DocumentDao {
	
	void create(DocumentMessage document);
	
	List<String> findPushDocumentsAfter(PushApplication pushApplication, String type, Date date);
	
	List<String> findAliasDocumentsAfter(PushApplication pushApplication, String alias, String qualifier, Date date);
	
}
