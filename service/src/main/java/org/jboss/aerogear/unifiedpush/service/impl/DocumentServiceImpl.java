package org.jboss.aerogear.unifiedpush.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Document;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.file.FileManager;
import org.jboss.aerogear.unifiedpush.service.file.PushApplicationFileService;

@Stateless
public class DocumentServiceImpl implements DocumentService {

	@Inject
	private PushApplicationFileService pushApplicationFileService;
	
	@Inject
	private FileManager fileManager;
	
	@Override
	public void saveForPushApplication(PushApplication pushApplication,
			Document document) {
		
	}

	@Override
	public List<Document> getPushApplicationDocuments(
			PushApplication pushApplication, Date afterDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveForAlias(PushApplication pushApplication, String alias,
			Document document) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Document> getAliasDocuments(PushApplication pushApplication,
			String alias, Date afterDate) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private byte[] serializeDocument(Document document) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(document);
		return baos.toByteArray();
	}
	
	private String getFileName(Document document) {
		return new StringBuilder("doc_").append(document.getType())
				.append(System.currentTimeMillis()).toString();
	}

}
