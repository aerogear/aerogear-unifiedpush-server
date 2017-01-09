package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.apache.commons.lang3.StringUtils;
import org.jacoco.core.internal.data.NullUUID;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.IDocument;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

@Stateless
@Interceptors(SpringContextInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DocumentServiceImpl implements DocumentService {

	/**
	 * We can't mixup EJB and spring beans. TODO - Change AliasDao To
	 * AliasService when mixing is supported.
	 */
	@Autowired
	private DocumentDao<DocumentContent, DocumentKey> documentDao;
	@Autowired
	private AliasDao aliasDao;

	@Override
	public void save(DocumentMetadata metadate, String content) {
		documentDao.create(createDocument(metadate, content));
	}

	@Override
	public String getLatestFromAlias(PushApplication pushApplication, String alias, String databse, String id) {
		DocumentContent document = (DocumentContent) documentDao
				.findOne(createKey(pushApplication.getPushApplicationID(), getAlias(alias), databse, id));

		if (document != null)
			return document.getContent();

		return null;
	}

	@Override
	public List<String> getLatestFromAliases(PushApplication pushApplication, String database, String id) {
		List<String> contents = new ArrayList<>();
		List<Alias> aliases = aliasDao.findAll(UUID.fromString(pushApplication.getPushApplicationID()));

		final List<IDocument<DocumentKey>> docs = documentDao
				.findLatestForAliases(createKey(pushApplication.getPushApplicationID(), database, id), aliases);

		if (docs != null) {
			docs.forEach((doc) -> {
				contents.add(doc.getContent());
			});
		}

		return contents;
	}

	@Override
	public void save(PushApplication pushApplication, MessagePayload message, boolean overwrite) {
		// Store documents according to aliases
		if (message.getPushMessage() != null && message.getPushMessage().getCriteria() != null
				&& message.getPushMessage().getCriteria().getAliases() != null) {

			for (String alias : message.getPushMessage().getCriteria().getAliases()) {
				save(message.getPayload(), pushApplication, getAlias(alias),
						DocumentMetadata.getDatabase(message.getQualifier()), DocumentMetadata.getId(message.getId()),
						overwrite);
			}
			// Store payload without alias
		} else {
			save(message.getPayload(), pushApplication,
					new Alias(UUID.fromString(pushApplication.getPushApplicationID()), NullUUID.NULL.getUuid()),
					DocumentMetadata.getDatabase(message.getQualifier()), DocumentMetadata.getId(message.getId()),
					overwrite);
		}
	}

	private void save(String document, PushApplication pushApplication, Alias alias, String database, String id,
			boolean overwrite) {

		DocumentMetadata meta = new DocumentMetadata(pushApplication.getPushApplicationID(), database, alias, id, null);
		documentDao.create(createDocument(meta, document));
	}

	private DocumentContent createDocument(DocumentMetadata metadata, String content) {
		return new DocumentContent(new DocumentKey(metadata), content);
	}

	private DocumentKey createKey(String pushApplicationId, Alias alias, String database, String id) {
		return new DocumentKey(new DocumentMetadata(pushApplicationId, database, alias, id, null));
	}

	private DocumentKey createKey(String pushApplicationId, String database, String id) {
		return new DocumentKey(new DocumentMetadata(pushApplicationId, database, null, id, null));
	}

	@Override
	public void delete(String pushApplicationId) {
		documentDao.delete(UUID.fromString(pushApplicationId));
	}

	private Alias getAlias(String alias) {
		if (StringUtils.isEmpty(alias))
			return null;

		return aliasDao.findByAlias(alias);
	}

}
