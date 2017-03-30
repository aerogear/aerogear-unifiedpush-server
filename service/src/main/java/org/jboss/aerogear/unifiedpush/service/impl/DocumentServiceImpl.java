package org.jboss.aerogear.unifiedpush.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.IDocument;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.spring.SpringContextInterceptor;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
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
	@Autowired
	private ConfigurationEnvironment configuration;

	public DocumentContent save(DocumentContent doc){
		return (DocumentContent) documentDao.create(doc);
	}

	@Override
	public DocumentContent save(DocumentMetadata metadate, String content, String id) {
		return (DocumentContent) documentDao.create(createDocument(metadate, content, id));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<DocumentContent> find(DocumentMetadata metadata, QueryOptions options) {
		// Always query X days period in case from date or limit are missing
		if (options != null && options.getFromDate() == null && options.getLimit() == null) {
			options.setFromDate(LocalDateTime.now().minusDays(configuration.getQueryDefaultPeriodInDays())
					.toInstant(ZoneOffset.UTC).toEpochMilli());
		}

		return (Stream<DocumentContent>) documentDao.find(new DocumentKey(metadata), options);
	}

	@Override
	public String getLatestFromAlias(PushApplication pushApplication, String alias, String database, String id) {
		DocumentMetadata metadata = new DocumentMetadata(pushApplication.getPushApplicationID(), database,
				getAlias(pushApplication.getPushApplicationID(), alias));

		DocumentContent document = (DocumentContent) documentDao.findOne(new DocumentKey(metadata), id);

		if (document != null)
			return document.getContent();

		return null;
	}

	@Override
	public List<String> getLatestFromAliases(PushApplication pushApplication, String database, String id) {
		List<String> contents = new ArrayList<>();
		List<Alias> aliases = aliasDao.findAll(UUID.fromString(pushApplication.getPushApplicationID()));

		DocumentMetadata metadata = new DocumentMetadata(pushApplication.getPushApplicationID(), database, null);

		final List<IDocument<DocumentKey>> docs = documentDao.findLatestForAliases(new DocumentKey(metadata), aliases,
				id);

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
				save(pushApplication, getAlias(pushApplication.getPushApplicationID(), alias), //
						DocumentMetadata.getDatabase(message.getQualifier()), //
						DocumentMetadata.getId(message.getId()), //
						message.getPayload());
			}
			// Store payload without alias
		} else {
			save(pushApplication,
					new Alias(UUID.fromString(pushApplication.getPushApplicationID()), NullUUID.NULL.getUuid()),
					DocumentMetadata.getDatabase(message.getQualifier()), DocumentMetadata.getId(message.getId()),
					message.getPayload());
		}
	}

	private DocumentContent save(PushApplication pushApplication, Alias alias, String database, String id,
			String document) {

		DocumentMetadata meta = new DocumentMetadata(pushApplication.getPushApplicationID(), database, alias);
		return (DocumentContent) documentDao.create(createDocument(meta, document, id));
	}

	private DocumentContent createDocument(DocumentMetadata metadata, String content, String id) {
		return new DocumentContent(new DocumentKey(metadata), content, id);
	}

	@Override
	public void delete(String pushApplicationId) {
		documentDao.delete(UUID.fromString(pushApplicationId));
	}

	private Alias getAlias(String pushApplicationId, String alias) {
		if (StringUtils.isEmpty(alias))
			return null;

		return aliasDao.findByAlias(pushApplicationId == null ? null : UUID.fromString(pushApplicationId), alias);
	}

}
