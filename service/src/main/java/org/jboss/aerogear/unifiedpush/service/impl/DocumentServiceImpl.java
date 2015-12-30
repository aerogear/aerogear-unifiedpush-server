package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage.DocumentType;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
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

	@Inject
	private InstallationDao installationDao;

	@Override
	public void saveForPushApplication(String deviceToken, Variant variant, String content, String qualifier) {
		Installation clientInstallation = clientInstallationService.findInstallationForVariantByDeviceToken(
				variant.getVariantID(), deviceToken);
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		documentDao.create(createMessage(content, pushApplication, DocumentType.INSTALLATION,
				clientInstallation.getAlias(), qualifier));
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
	public void saveForAliases(PushApplication pushApplication, Map<String, String> aliasToDocument, String qualifier) {
		Set<String> enabledDevices = installationDao.filterDisabledDevices(aliasToDocument.keySet());
		
		for (Map.Entry<String, String> entry : aliasToDocument.entrySet()) {
			String alias = entry.getKey();
			if (enabledDevices.contains(alias) || alias.equalsIgnoreCase(DocumentMessage.NULL_ALIAS)) {
				save(entry.getValue(), pushApplication, DocumentType.APPLICATION, alias, qualifier);
			}
		}
	}

	private void save(String document, PushApplication pushApplication, DocumentType publisher, String alias,
			String qualifier) {
		documentDao.create(createMessage(document, pushApplication, publisher, alias, qualifier));
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
