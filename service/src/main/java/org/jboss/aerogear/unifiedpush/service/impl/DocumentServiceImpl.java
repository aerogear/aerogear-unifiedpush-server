package org.jboss.aerogear.unifiedpush.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentServiceImpl implements DocumentService {
	@Autowired
	private DocumentDao<DocumentContent, DocumentKey> documentDao;
	@Autowired
	private AliasService aliasService;
	@Autowired
	private ConfigurationEnvironment configuration;

	public DocumentContent save(DocumentContent doc){
		return (DocumentContent) documentDao.create(doc);
	}

	@Override
	public DocumentContent save(DocumentMetadata metadate, String content, String id) {
		return (DocumentContent) documentDao.create(createDocument(metadate, content, id));
	}

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
	public DocumentContent findLatest(DocumentMetadata metadata, String id) {
		DocumentContent document = (DocumentContent) documentDao.findOne(new DocumentKey(metadata), id);

		if (document != null)
			return document;

		return null;
	}

	@Override
	public List<DocumentContent> findLatest(PushApplication pushApplication, String database, String id, List<Alias> aliases) {
		List<DocumentContent> contents = new ArrayList<>();

		DocumentMetadata metadata = new DocumentMetadata(pushApplication.getPushApplicationID(), database, null);

		final List<DocumentContent> docs = documentDao.findLatestForAliases(new DocumentKey(metadata), aliases,
				id);

		if (docs != null) {
			docs.forEach((doc) -> {
				contents.add(doc);
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

		return aliasService.find(pushApplicationId, alias);
	}

}
