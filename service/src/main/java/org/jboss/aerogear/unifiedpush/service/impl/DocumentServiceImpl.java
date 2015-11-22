package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage.DocumentType;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

@Stateless
public class DocumentServiceImpl implements DocumentService {

	@Inject
	private DocumentDao documentDao;
	
	@Inject
	private PushApplicationService pushApplicationService;
	
	@Inject
	private ClientInstallationService clientInstallationService;
    
	@Override
	public void saveForPushApplication(String deviceToken, Variant variant,
			String content, String qualifier) {
		Installation clientInstallation = clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), deviceToken);
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		documentDao.create(createMessage(content, clientInstallation.getAlias(), 
				pushApplication.getPushApplicationID(), DocumentType.APPLICATION_DOCUMENT, qualifier));
	}
	
	@Override
	public List<String> getPushApplicationDocuments(PushApplication pushApplication, String type, Date afterDate) {
		return documentDao.findPushDocumentsAfter(pushApplication, type, afterDate);
	}

	@Override
	public void saveForAlias(PushApplication pushApplication, String alias,
			String document, String qualifier) {
		documentDao.create(createMessage(document, pushApplication.getPushApplicationID(), alias, DocumentType.INSTALLATION_DOCUMENT, qualifier));
	}

	@Override
	public List<String> getAliasDocuments(Variant variant, String alias, String type, Date afterDate) {
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		return documentDao.findAliasDocumentsAfter(pushApplication, alias, type, afterDate);
	}

	@Override
	public void saveForAliases(PushApplication pushApplication, Map<String, List<String>> aliasToDocuments, String qualifier) {
		for (Map.Entry<String, List<String>> entry : aliasToDocuments.entrySet()) {
			String alias = entry.getKey();
			List<String> documents = entry.getValue();
			for (String document : documents) {
				saveForAlias(pushApplication, alias, document, qualifier);
			}
		}
	
	} 
	
	private DocumentMessage createMessage(String content, String source, String destination, DocumentType documentType, String qualifier) {
		DocumentMessage message = new DocumentMessage(); 
		message.setSource(source);
		message.setDestination(destination);
		message.setContent(content);
		message.setType(documentType);
		message.setQualifier(qualifier);
		return message;
	}

}
