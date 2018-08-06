package org.jboss.aerogear.unifiedpush.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DocumentServiceImpl implements DocumentService {
	@Autowired
	private DocumentDao<DocumentContent, DocumentKey> documentDao;

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

	private DocumentContent createDocument(DocumentMetadata metadata, String content, String id) {
		return new DocumentContent(new DocumentKey(metadata), content, id);
	}

	@Override
	@Async
	public void delete(String pushApplicationId) {
		documentDao.delete(UUID.fromString(pushApplicationId));
	}

	public void delete(UUID pushApplicaitonId, Alias alias) {
		documentDao.delete(pushApplicaitonId, alias);
	}

}
