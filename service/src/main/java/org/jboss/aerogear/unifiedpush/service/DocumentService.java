package org.jboss.aerogear.unifiedpush.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

public interface DocumentService {
	
	void saveForPushApplication(String deviceToken, Variant variant, String content, String qualifier);
	
	List<String> getPushApplicationDocuments(PushApplication pushApplication, String type, Date afterDate);
	
	void saveForAliases(PushApplication pushApplication, Map<String, List<String>> aliasToDocuments, String qualifier);
	
	List<String> getAliasDocuments(Variant variant, String alias, String type, Date afterDate);
}
