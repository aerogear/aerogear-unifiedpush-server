package org.jboss.aerogear.unifiedpush.service.impl.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;

import org.jboss.aerogear.unifiedpush.service.file.FileManager;

/**
 * A basic, non-synchronous and blocking implementation of FileManager.
 */
@Stateless
public class FileManagerImpl implements FileManager {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(Path path, byte[] data) {
		try {
			File directory = path.getParent().toFile();
			if (!directory.exists()) {
				directory.mkdirs();
			}
			Files.write(path, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] read(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> list(Path path) {
		File directory = path.toFile();
		if (!directory.exists()) {
			throw new RuntimeException(path + " does not exist");
		}
		
		if (!directory.isDirectory()) {
			throw new RuntimeException(path + " is not a directory");
		}
		
		return Arrays.asList(directory.list());
	}

}