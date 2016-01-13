package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata.DocumentType;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

@Stateless
public class DocumentServiceImpl implements DocumentService {

	@Inject
	private DocumentDao documentDao;

	@Inject
	private PushApplicationService pushApplicationService;

	@Override
	public void saveForPushApplication(PushApplication pushApp, String alias, String content, String qualifier, String id, boolean overwrite) {
		documentDao.create(createMessage(content, pushApp, DocumentType.INSTALLATION,
			alias, qualifier, id), overwrite);
	}

	@Override
	public List<DocumentMessage> getDocuments(PushApplication pushApplication, DocumentType publisher) {
		return documentDao.findDocuments(createMetadata(pushApplication, publisher, DocumentMetadata.NULL_ALIAS,
				DocumentMetadata.NULL_QUALIFIER, null, false));
	}

	@Override
	public String getLatestDocumentForAlias(Variant variant, DocumentType publisher, String alias, String qualifier) {
		PushApplication pushApplication = pushApplicationService.findByVariantID(variant.getVariantID());
		DocumentMessage document = documentDao.findLatestDocumentForAlias(createMetadata(pushApplication, publisher, alias,
				qualifier, null, true));

		if (document != null)
			return document.getContent();

		return null;
	}
	
	@Override
	public List<String> getLatestDocumentsForApplication(
			PushApplication pushApp, String qualifier, String id) {
		List<String> contents = new ArrayList<>();
		final List<DocumentMessage> docs = documentDao.findLatestDocumentsForApplication(createMetadata(pushApp, DocumentType.INSTALLATION,
				DocumentMetadata.NULL_ALIAS, qualifier, id, true));
		for (DocumentMessage doc : docs) {
			contents.add(doc.getContent());
		}
		return contents;
	}
	
	@Override
	public void saveForAliases(PushApplication pushApplication, Map<String, String> aliasToDocument, String qualifier,
			String id, boolean overwrite) {
		for (Map.Entry<String, String> entry : aliasToDocument.entrySet()) {
			save(entry.getValue(), pushApplication, DocumentType.APPLICATION, entry.getKey(), qualifier, id, overwrite);
		}
	}

	private void save(String document, PushApplication pushApplication, DocumentType publisher, String alias,
			String qualifier, String id, boolean overwrite) {
		documentDao.create(createMessage(document, pushApplication, publisher, alias, qualifier, id), overwrite);
	}

	private DocumentMessage createMessage(String content, PushApplication pushApplication, DocumentType publisher,
			String alias, String qualifier, String id) {
		DocumentMessage message = new DocumentMessage();
		message.setContent(content);
		DocumentMetadata meta = new DocumentMetadata();
		meta.setPushApplication(pushApplication);
		meta.setPublisher(publisher);
		meta.setAlias(alias);
		meta.setQualifier(qualifier);
		meta.setId(id);
		message.setMetadata(meta);
		return message;
	}

	private DocumentMetadata createMetadata(PushApplication pushApplication, DocumentType publisher, String alias,
			String qualifier, String id, Boolean latest) {
		DocumentMetadata message = new DocumentMetadata();
		message.setPushApplication(pushApplication);
		message.setPublisher(publisher);
		message.setAlias(alias);
		message.setQualifier(qualifier);
		message.setLatest(latest);
		message.setId(id);
		return message;
	}

}
