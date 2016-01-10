package org.jboss.aerogear.unifiedpush.service.impl;

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
	public void saveForPushApplication(String deviceToken, Variant variant, String content, String qualifier, boolean overwrite) {
		Installation clientInstallation = clientInstallationService.findInstallationForVariantByDeviceToken(
				variant.getVariantID(), deviceToken);
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		documentDao.create(createMessage(content, pushApplication, DocumentType.INSTALLATION,
				clientInstallation.getAlias(), qualifier), overwrite);
	}

	@Override
	public List<DocumentMessage> getDocuments(PushApplication pushApplication, DocumentType publisher) {
		return documentDao.findDocuments(createMessage(pushApplication, publisher, DocumentMessage.NULL_ALIAS,
				DocumentMessage.NULL_QUALIFIER, false));
	}

	@Override
	public String getLatestDocument(Variant variant, DocumentType publisher, String alias, String qualifier) {
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		DocumentMessage document = documentDao.findLatestDocument(createMessage(pushApplication, publisher, alias,
				qualifier, true));

		if (document != null)
			return document.getContent();

		return null;
	}
	
	@Override
	public void saveForAliases(PushApplication pushApplication, Map<String, String> aliasToDocument, String qualifier,
			boolean overwrite) {
		for (Map.Entry<String, String> entry : aliasToDocument.entrySet()) {
			save(entry.getValue(), pushApplication, DocumentType.APPLICATION, entry.getKey(), qualifier, overwrite);
		}
	}

	private void save(String document, PushApplication pushApplication, DocumentType publisher, String alias,
			String qualifier, boolean overwrite) {
		documentDao.create(createMessage(document, pushApplication, publisher, alias, qualifier), overwrite);
	}

	private DocumentMessage createMessage(String content, PushApplication pushApplication, DocumentType publisher,
			String alias, String qualifier) {
		DocumentMessage message = new DocumentMessage();
		message.setContent(content);
		message.setPushApplication(pushApplication);
		message.setPublisher(publisher);
		message.setAlias(alias);
		message.setQualifier(qualifier);
		return message;
	}

	private DocumentMessage createMessage(PushApplication pushApplication, DocumentType publisher, String alias,
			String qualifier, Boolean latest) {
		DocumentMessage message = new DocumentMessage();
		message.setPushApplication(pushApplication);
		message.setPublisher(publisher);
		message.setAlias(alias);
		message.setQualifier(qualifier);
		message.setLatest(latest);
		return message;
	}

}
