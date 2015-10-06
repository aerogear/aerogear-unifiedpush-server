package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Document;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
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
			Document document) {
		Installation clientInstallation = clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), deviceToken);
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		documentDao.create(createMessage(document, clientInstallation.getAlias(), pushApplication.getPushApplicationID()));
	}
	
	@Override
	public List<Document> getPushApplicationDocuments(PushApplication pushApplication, Date afterDate) {
		return documentDao.findPushDocumentsAfter(pushApplication, afterDate);
	}

	@Override
	public void saveForAlias(PushApplication pushApplication, String alias,
			Document document) {
		documentDao.create(createMessage(document, pushApplication.getPushApplicationID(), alias));
	}

	@Override
	public List<Document> getAliasDocuments(PushApplication pushApplication, String alias, Date afterDate) {
		return documentDao.findAliasDocumentsAfter(pushApplication, alias, null, afterDate);
	}

	private DocumentMessage createMessage(Document document, String source, String destination) {
		DocumentMessage message = new DocumentMessage(); 
		message.setSource(source);
		message.setDestination(destination);
		message.setDocument(document);
		return message;
	}
}
