package org.jboss.aerogear.unifiedpush.service.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata.DocumentType;
import org.jboss.aerogear.unifiedpush.dao.DocumentDao;
import org.jboss.aerogear.unifiedpush.service.Configuration;

@Stateless
public class DocumentDaoImpl implements DocumentDao {
	private static final Logger logger = Logger.getLogger(DocumentDao.class.getName());

	private static final String DOCUMENT_TOKEN = "__";
	private static final String NULL_PART = "NULL";
	
	@Inject
	private FileManager fileManager;

	@Inject
	private Configuration configuration;

	@Override
	public void create(DocumentMessage message, boolean overwrite) {
		Path directoryPath = getDocumentPath(message.getMetadata());
		fileManager.save(Paths.get(directoryPath.toString(), getDocumentFileName(message.getMetadata(), overwrite)), message.getContent()
				.getBytes(StandardCharsets.UTF_8));
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
	public List<DocumentMessage> findDocuments(DocumentMetadata message) {
		return getDocuments(getDocumentPath(message), message, null);
	}

	@Override
	public DocumentMessage findLatestDocumentForAlias(DocumentMetadata metadata) {
		return findLatestDocumentInDirectory(getDocumentPath(metadata), metadata);
	}
	
	@Override
	public List<DocumentMessage> findLatestDocumentsForApplication(DocumentMetadata message) {
		try {
			List<File> aliasDirectories = fileManager.list(
					getFullDirectoryPath(Paths.get(message.getPushApplication()
							.getPushApplicationID(), DocumentType.INSTALLATION
							.toString())), new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return pathname.isDirectory();
						}
					});
			List<DocumentMessage> documents = new LinkedList<>();
			
			for (File aliasDirectory : aliasDirectories) {
				documents.add(findLatestDocumentInDirectory(aliasDirectory.toPath(), message));
			}
			
			return documents;
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private DocumentMessage findLatestDocumentInDirectory(Path directoryPath, DocumentMetadata metadata) {
		List<DocumentMessage> documents = getDocuments(directoryPath, metadata, new DocumentNameFilterImpl(metadata));

		if (documents != null && documents.size() >= 1) {
			Collections.sort(documents, new Comparator<DocumentMessage>() {

				@Override
				public int compare(DocumentMessage o1, DocumentMessage o2) {
					return Long.compare(o1.getMetadata().getTimestamp(), o2.getMetadata().getTimestamp());
				}
			});

			return documents.get(documents.size() - 1);
		}

		return null;
	}

	private List<DocumentMessage> getDocuments(Path directoryPath, final DocumentMetadata metadata,
			final DocumentNameFilter filter) {
		File directory = directoryPath.toFile();
		List<File> files;
		try {
			files = fileManager.list(directory.toPath(), new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String[] parts = pathname.getName().split(DOCUMENT_TOKEN);
					if (filter != null
							&& !filter.accept(parts[0], DocumentType.valueOf(parts[1]), parts[2], parts[3], parts[4], parts[5])) {
						return false;
					}

					return metadata.getPushApplication().getPushApplicationID().equals(parts[0])
							&& metadata.getPublisher().name().equals(parts[1]);
				}
			});
		} catch (FileNotFoundException e) {
			logger.warning("Unable to find directory path, " + directoryPath);

			// Initialize an empty list.
			files = new ArrayList<>();
		}

		List<DocumentMessage> documents = new ArrayList<>(files.size());
		for (File file : files) {
			DocumentMetadata docMeta = new DocumentMetadata(metadata);
			docMeta.setTimestamp(file.lastModified());
			DocumentMessage document = new DocumentMessage(new String(fileManager.read(file.toPath()),
					StandardCharsets.UTF_8), docMeta);
			documents.add(document);
		}

		return documents;
	}

	private Path getDocumentPath(DocumentMetadata message) {
		// Application publisher allowed to create global alias documents.
		if (message.getPublisher() == DocumentType.APPLICATION && message.getAlias().equalsIgnoreCase(DocumentMetadata.NULL_ALIAS))
			return getFullDirectoryPath(Paths.get(message.getPushApplication().getPushApplicationID(),
					message.getPublisher().name()));
		
		return getFullDirectoryPath(Paths.get(message.getPushApplication().getPushApplicationID(),
				message.getPublisher().name(), message.getAlias()));
	}

	private Path getFullDirectoryPath(Path path) {
		String pathRoot = configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY);
		return Paths.get(pathRoot, path.toString());
	}

	private String getDocumentFileName(DocumentMetadata metadata, boolean overwrite) {
		return new StringBuilder(metadata.getPushApplication().getPushApplicationID()).append(DOCUMENT_TOKEN)
				.append(metadata.getPublisher().name()).append(DOCUMENT_TOKEN).append(metadata.getAlias())
				.append(DOCUMENT_TOKEN)
				.append(metadata.getQualifier() == null ? NULL_PART : metadata.getQualifier().toUpperCase())
				.append(DOCUMENT_TOKEN).append(overwrite ? NULL_PART : System.currentTimeMillis())
				.append(DOCUMENT_TOKEN).append(getFileNamePart(metadata.getId())).toString();
	}
	
	private String getFileNamePart(Object obj) {
		return obj == null ? NULL_PART : obj.toString();
	}
	
	private static interface DocumentNameFilter {
		boolean accept(String pushApp, DocumentType type, String alias, String qualifier, String time, String id);
	}
	
	private class DocumentNameFilterImpl implements DocumentNameFilter {
		private final DocumentMetadata documentMetadata;
		
		public DocumentNameFilterImpl(DocumentMetadata documentMetadata) {
			this.documentMetadata = documentMetadata;
		}

		@Override
		public boolean accept(String pushApp, DocumentType type,
				String alias, String qualifier, String time, String id) {
			System.out.println(documentMetadata.getAlias() + "," + alias);
			System.out.println(documentMetadata.getPushApplication() + "," + pushApp);
			System.out.println(documentMetadata.getPublisher() + "," + type);
			if (documentMetadata.getAlias() != null && !documentMetadata.getAlias().equals(alias)) return false;
			if (documentMetadata.getPushApplication() != null && !documentMetadata.getPushApplication().getPushApplicationID().equals(pushApp)) return false;
			
			return documentMetadata.getPublisher() == type 
					&& qualifier.equals(getFileNamePart(documentMetadata.getQualifier()))
					&& id.equals(getFileNamePart(documentMetadata.getId()));
		}
	}
}
