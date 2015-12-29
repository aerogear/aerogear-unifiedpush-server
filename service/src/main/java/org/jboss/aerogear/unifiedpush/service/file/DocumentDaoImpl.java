package org.jboss.aerogear.unifiedpush.service.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage.DocumentType;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
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
	public List<String> findPushDocumentsNewer(PushApplication pushApplication, Date date) {
		return getNewer(getPushApplicationDocumentDirectoryPath(pushApplication.getPushApplicationID()), date, null);
	}

	@Override
	public List<String> findAliasDocumentsNewer(PushApplication pushApplication, String alias, final String qualifier,
			Date date) {
		return getNewer(getAliasDocumentDirectoryPath(pushApplication.getPushApplicationID(), alias), date,
				new DocumentNameFilter() {
					@Override
					public boolean accept(DocumentType type, String source, String destination,
							String documentQualifier, String time) {
						return documentQualifier == null || documentQualifier.equals(qualifier);
					}
				});
	}

	private List<String> getNewer(Path directoryPath, final Date date, final DocumentNameFilter filter) {
		File directory = directoryPath.toFile();
		List<File> files;
		try {
			files = fileManager.list(directory.toPath(), new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String[] parts = pathname.getName().split(DOCUMENT_TOKEN);
					if (filter != null
							&& !filter.accept(DocumentType.valueOf(parts[0]), parts[1], parts[2], parts[3], parts[4])) {
						return false;
					}
					Long messageTime = Long.parseLong(parts[4]);
					return messageTime >= date.getTime();
				}
			});
		} catch (FileNotFoundException e) {
			logger.warning("Unable to find directory path, " + directoryPath);

			// Initialize an empty list.
			files = new ArrayList<>();
		}

		List<String> documents = new ArrayList<>(files.size());

		for (File file : files) {
			documents.add(new String(fileManager.read(file.toPath())));
		}

		return documents;
	}

	private Path getPushApplicationDocumentDirectoryPath(String pushApplicationID) {
		return getFullDirectoryPath(Paths.get(pushApplicationID));
	}

	private Path getAliasDocumentDirectoryPath(String pushApplicationID, String alias) {
		return getFullDirectoryPath(Paths.get(pushApplicationID, alias));
	}

	private Path getFullDirectoryPath(Path path) {
		String pathRoot = configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY);
		return Paths.get(pathRoot, path.toString());
	}

	private String getDocumentFileName(DocumentMessage message) {
		return new StringBuilder(message.getType().toString()).append(DOCUMENT_TOKEN).append(message.getSource())
				.append(DOCUMENT_TOKEN).append(message.getDestination()).append(DOCUMENT_TOKEN)
				.append(message.getQualifier() == null ? "NULL" : message.getQualifier().toUpperCase())
				.append(DOCUMENT_TOKEN).append(System.currentTimeMillis()).toString();
	}

	private static interface DocumentNameFilter {
		boolean accept(DocumentType type, String source, String destination, String qualifier, String time);
	}
}
