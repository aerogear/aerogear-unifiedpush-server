package org.jboss.aerogear.unifiedpush.service;

import java.util.Date;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Document;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

public interface DocumentService {
	
	void saveForPushApplication(String deviceToken, Variant variant, Document document);
	
	List<Document> getPushApplicationDocuments(PushApplication pushApplication, Date afterDate);
	
	void saveForAlias(PushApplication pushApplication, String alias, Document document);
	
	List<Document> getAliasDocuments(PushApplication pushApplication, String alias, Date afterDate);
}
