package org.jboss.aerogear.unifiedpush.service.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage.DocumentType;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.service.Configuration;

@Stateless
public class DocumentDaoImpl implements DocumentDao {
	private static final Logger logger = Logger.getLogger(DocumentDao.class.getName());

	private static final String DOCUMENT_TOKEN = "__";
	@Inject
	private FileManager fileManager;

	@Inject
	private Configuration configuration;

	@Override
	public void create(DocumentMessage message) {
		Path directoryPath = getDocumentPath(message);

		fileManager.save(Paths.get(directoryPath.toString(), getDocumentFileName(message)), message.getContent()
				.getBytes());
	}

	@Override
	/**
	 * Return list of documents older then a given date.
	 * 
	 * @param pushApplication
	 * @param date
	 * 
	 * @return - List<String> of document content.
	 */
	public List<DocumentMessage> findDocuments(DocumentMessage message) {
		return getDocuments(getDocumentPath(message), message, null);
	}

	@Override
	public DocumentMessage findLatestDocument(DocumentMessage message) {
		List<DocumentMessage> documents = getDocuments(getDocumentPath(message), message, null);

		if (documents != null && documents.size() >= 1) {
			Collections.sort(documents, new Comparator<DocumentMessage>() {

				@Override
				public int compare(DocumentMessage o1, DocumentMessage o2) {
					return (int) (o1.getTimestamp() - o2.getTimestamp());
				}
			});

			return documents.get(documents.size() - 1);
		}

		return null;
	}

	private List<DocumentMessage> getDocuments(Path directoryPath, final DocumentMessage message,
			final DocumentNameFilter filter) {
		File directory = directoryPath.toFile();
		List<File> files;
		try {
			files = fileManager.list(directory.toPath(), new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String[] parts = pathname.getName().split(DOCUMENT_TOKEN);
					if (filter != null
							&& !filter.accept(parts[0], DocumentType.valueOf(parts[1]), parts[2], parts[3], parts[4])) {
						return false;
					}

					return message.getPushApplication().getPushApplicationID().equals(parts[0])
							&& message.getPublisher().name().equals(parts[1]);
				}
			});
		} catch (FileNotFoundException e) {
			logger.warning("Unable to find directory path, " + directoryPath);

			// Initialize an empty list.
			files = new ArrayList<>();
		}

		List<DocumentMessage> documents = new ArrayList<>(files.size());
		DocumentMessage document;
		for (File file : files) {
			document = new DocumentMessage(new String(fileManager.read(file.toPath())), message);
			document.setTimestamp(file.lastModified());
			documents.add(document);
		}

		return documents;
	}

	private Path getDocumentPath(DocumentMessage message) {
		// Application publisher allowed to create global alias documents.
		if (message.getPublisher() == DocumentType.APPLICATION && message.getAlias().equalsIgnoreCase(DocumentMessage.NULL_ALIAS))
			return getFullDirectoryPath(Paths.get(message.getPushApplication().getPushApplicationID(),
					message.getPublisher().name()));
		
		return getFullDirectoryPath(Paths.get(message.getPushApplication().getPushApplicationID(),
				message.getPublisher().name(), message.getAlias()));
	}

	private Path getFullDirectoryPath(Path path) {
		String pathRoot = configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY);
		return Paths.get(pathRoot, path.toString());
	}

	private String getDocumentFileName(DocumentMessage message) {
		return new StringBuilder(message.getPushApplication().getPushApplicationID()).append(DOCUMENT_TOKEN)
				.append(message.getPublisher().name()).append(DOCUMENT_TOKEN).append(message.getAlias())
				.append(DOCUMENT_TOKEN)
				.append(message.getQualifier() == null ? "NULL" : message.getQualifier().toUpperCase())
				.append(DOCUMENT_TOKEN).append(System.currentTimeMillis()).toString();
	}

	private static interface DocumentNameFilter {
		boolean accept(String publisher, DocumentType type, String alias, String qualifier, String time);
	}
}
