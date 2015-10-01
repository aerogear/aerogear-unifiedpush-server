package org.jboss.aerogear.unifiedpush.service;

import java.util.Date;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Document;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface DocumentService {
	
	void saveForPushApplication(PushApplication pushApplication, Document document);
	
	List<Document> getPushApplicationDocuments(PushApplication pushApplication, Date afterDate);
	
	void saveForAlias(PushApplication pushApplication, String alias, Document document);
	
	List<Document> getAliasDocuments(PushApplication pushApplication, String alias, Date afterDate);
}
