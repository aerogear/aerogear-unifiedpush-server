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
	 * We can't mixup EJB and spring beans.
	 * TODO - Change AliasDao To AliasService when mixing is supported.
	 */
	@Autowired
	private DocumentDao<DocumentContent, DocumentKey> documentDao;
	@Autowired
	private AliasDao aliasDao;

	@Override
	public void save(PushApplication pushApplication, String alias, String content, String databse, String id,
			boolean overwrite) {
		Alias user = aliasDao.findByAlias(alias);
		documentDao.create(createDocument(content, pushApplication, user, databse, id));
	}

	@Override
	public String getLatestFromAlias(PushApplication pushApplication, String alias, String databse, String id) {
		Alias user = aliasDao.findByAlias(alias);
		DocumentContent document = (DocumentContent) documentDao.findOne(createKey(pushApplication, user, databse, id));

		if (document != null)
			return document.getContent();

		return null;
	}

	@Override
	public List<String> getLatestFromAliases(PushApplication pushApp, String database, String id) {
		List<String> contents = new ArrayList<>();
		List<Alias> aliases = aliasDao.findAll(UUID.fromString(pushApp.getPushApplicationID()));

		final List<IDocument<DocumentKey>> docs = documentDao.findLatestForAliases(createKey(pushApp, database, id),
				aliases);

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
				Alias user = aliasDao.findByAlias(alias);
				save(message.getPayload(), pushApplication, user, DocumentMetadata.getDatabase(message.getQualifier()),
						DocumentMetadata.getId(message.getId()), overwrite);
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
		documentDao.create(createDocument(document, pushApplication, alias, database, id));
	}

	private DocumentContent createDocument(String content, PushApplication pushApplication, Alias alias,
			String database, String id) {

		DocumentMetadata meta = createMetadata(pushApplication, alias, database, id, null);
		DocumentContent message = new DocumentContent(new DocumentKey(meta), content);

		return message;
	}

	private DocumentKey createKey(PushApplication pushApplication, Alias alias, String database, String id) {
		return new DocumentKey(createMetadata(pushApplication, alias, database, id, null));
	}

	private DocumentKey createKey(PushApplication pushApplication, String database, String id) {
		return new DocumentKey(createMetadata(pushApplication, null, database, id, null));
	}

	private DocumentMetadata createMetadata(PushApplication pushApplication, Alias alias, String database, String id,
			String snapshot) {
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setPushApplicationId(pushApplication.getPushApplicationID());

		// Alias is always stored as lowercase, and matched insensitively.
		metadata.setUserId(alias != null && //
				!alias.equals(DocumentMetadata.NULL_ALIAS) ? alias.getId() : NullUUID.NULL.getUuid());
		metadata.setDatabase(database);
		metadata.setSnapshot(snapshot);
		metadata.setId(id);
		return metadata;
	}

	@Override
	public void delete(String pushApplicationId) {
		documentDao.delete(UUID.fromString(pushApplicationId));
	}

}
