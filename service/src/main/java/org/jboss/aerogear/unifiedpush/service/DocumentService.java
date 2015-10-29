package org.jboss.aerogear.unifiedpush.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

public interface DocumentService {
	
	void saveForPushApplication(String deviceToken, Variant variant, String content);
	
	List<String> getPushApplicationDocuments(PushApplication pushApplication, String type, Date afterDate);
	
	void saveForAlias(PushApplication pushApplication, String alias, String document);
	
	void saveForAliases(PushApplication pushApplication, Map<String, List<String>> aliasToDocuments);
	
	List<String> getAliasDocuments(Variant variant, String alias, String type, Date afterDate);
}
