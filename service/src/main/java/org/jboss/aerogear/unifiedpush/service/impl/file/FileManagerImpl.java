package org.jboss.aerogear.unifiedpush.service.impl.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.aerogear.unifiedpush.service.file.FileManager;

public class FileManagerImpl implements FileManager {

	@Override
	public void save(Path path, byte[] data) {
		try {
			Files.write(path, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] read(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
