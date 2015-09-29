package org.jboss.aerogear.unifiedpush.service.impl.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.file.FileManager;
import org.jboss.aerogear.unifiedpush.service.file.FileService;

public class FileServiceImpl implements FileService {

	@Inject
	private FileManager fileManager;
	
	@Override
	public void writeForAlias(PushApplication pushApp, String alias, String fileName, byte[] data) {
		Path path = getPath(pushApp, alias);
		fileManager.save(Paths.get(path.toString(), fileName), data);
	}

	@Override
	public List<String> getFilesForAlias(PushApplication pushApp, String alias) {
		return fileManager.list(getPath(pushApp, alias));
	}

	@Override
	public byte[] readFileForAlias(PushApplication pushApp, String alias, String fileName) {
		Path path = Paths.get(getPath(pushApp, alias).toString(), fileName);
		return fileManager.read(path);
	}
	
	private Path getPath(PushApplication pushApp, String alias) {
		// TODO: keep a parameter for the root path
		return Paths.get(pushApp.getPushApplicationID(), alias);
	}
	
}
