package org.jboss.aerogear.unifiedpush.service.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Document;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.service.Configuration;

// TODO: move this class out of here!
@Stateless
public class DocumentDaoImpl implements DocumentDao {

	@Inject
	private FileManager fileManager;
	
	@Inject
	private Configuration configuration;
	
	@Override
	public void create(DocumentMessage message) {		
		try {
			Path directoryPath;
			switch (message.getType()) {
			case APPLICATION_DOCUMENT:
				directoryPath = getPushApplicationDocumentDirectoryPath(message.getDestination());
				break;
			case INSTALLATION_DOCUMENT:
				directoryPath = getAliasDocumentDirectoryPath(message.getSource(), message.getDestination());
				break;
			default:
				throw new IllegalArgumentException("for type: " + message.getType());
			}
			
			fileManager.save(Paths.get(directoryPath.toString(), 
					getDocumentFileName(message)), 
					serializeDocument(message.getDocument()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<Document> findPushDocumentsAfter(
			PushApplication pushApplication, Date date) {
		return getAfter(getPushApplicationDocumentDirectoryPath(pushApplication.getPushApplicationID()), date, null);
	}
	
	@Override
	public List<Document> findAliasDocumentsAfter(
			PushApplication pushApplication, String alias, final String qualifier, Date date) {
		return getAfter(getAliasDocumentDirectoryPath(pushApplication.getPushApplicationID(), alias), date, new DocumentNameFilter() {
			@Override
			public boolean accept(String type, String source, String destination,
					String documentQualifier, String time) {
				return documentQualifier == null || documentQualifier.equals(qualifier);
			}
		});
	}
	
	private List<Document> getAfter(Path directoryPath, final Date date, final DocumentNameFilter filter) {
		File directory = directoryPath.toFile();
		List<File> files = fileManager.list(directory.toPath(), new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String[] parts = pathname.getName().split("_");
				if (filter != null && !filter.accept(parts[1], parts[2], parts[3], parts[4], parts[5])) {
					return false;
				}
				Long messageTime = Long.parseLong(parts[5]);
				return messageTime >= date.getTime();
			}
		});
		
		List<Document> documents = new ArrayList<>(files.size());
		
		for (File file : files) {
			documents.add(deserializeDocument(file));
		}
		
		return documents;
	}

	private Document deserializeDocument(File file) {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
			return (Document) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] serializeDocument(Document document) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(document);
		return baos.toByteArray();
	}

	private Path getPushApplicationDocumentDirectoryPath(String pushApplicationID) {
		return getFullDirectoryPath(Paths.get(pushApplicationID));
	}
	
	private Path getAliasDocumentDirectoryPath(String pushApplicationID, String alias) {
		return getFullDirectoryPath(Paths.get(pushApplicationID, alias));
	}
	
	private Path getFullDirectoryPath(Path path) {
		// TODO: add to property file
		String pathRoot = configuration.getProperty("aerogear.config.document.path.root");
		return Paths.get(pathRoot, path.toString());
	}

	private String getDocumentFileName(DocumentMessage message) {
		return new StringBuilder("doc_").append(message.getType())
				.append(message.getSource()).append("_")
				.append(message.getDestination()).append("_")
				.append(message.getDocument().getQualifier() == null ? "null" : message.getDocument().getQualifier()).append("_")
				.append(System.currentTimeMillis()).toString();
	}
	
	private String sanitize(String s) {
		return s.trim().replace(" ", "");
	}
	
	private static interface DocumentNameFilter {
		boolean accept(String type, String source, String destination, String qualifier, String time);
	}
}
