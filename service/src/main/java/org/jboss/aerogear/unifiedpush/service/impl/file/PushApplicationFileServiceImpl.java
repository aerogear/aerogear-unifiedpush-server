package org.jboss.aerogear.unifiedpush.service.impl.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.file.FileManager;
import org.jboss.aerogear.unifiedpush.service.file.PushApplicationFileService;

@Stateless
public class PushApplicationFileServiceImpl implements PushApplicationFileService {
	
	@Inject
	private FileManager fileManager;

	@Override
	public void save(PushApplication pushApp, String fileName, byte[] data) {
		fileManager.save(getPath(pushApp, fileName), data);
	}

	@Override
	public List<String> getFiles(PushApplication pushApp) {
		return fileManager.list(getPath(pushApp));
	}

	@Override
	public byte[] readFile(PushApplication pushApp, String fileName) {
		return fileManager.read(getPath(pushApp, fileName));
	}
	
	private Path getPath(PushApplication pushApplication, String fileName) {
		return Paths.get(getPath(pushApplication).toString(), fileName);
	}
	
	private Path getPath(PushApplication pushApplication) {
		return Paths.get(pushApplication.getPushApplicationID());
	}
}
