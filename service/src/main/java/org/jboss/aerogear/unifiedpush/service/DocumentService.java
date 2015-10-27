package org.jboss.aerogear.unifiedpush.service;

import java.util.Date;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Document;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

public interface DocumentService {
	
	void saveForPushApplication(String deviceToken, Variant variant, Document document);
	
	List<Document> getPushApplicationDocuments(PushApplication pushApplication, String type, Date afterDate);
	
	void saveForAlias(PushApplication pushApplication, String alias, Document document);
	
	List<Document> getAliasDocuments(Variant variant, String alias, String type, Date afterDate);
}
