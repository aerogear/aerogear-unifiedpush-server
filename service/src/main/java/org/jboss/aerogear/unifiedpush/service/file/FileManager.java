package org.jboss.aerogear.unifiedpush.service.file;

import java.nio.file.Path;
import java.util.List;

/**
 * Offers basic functionality for read, saving and listing files.
 */
public interface FileManager {
	
	/**
	 * Saves (overwrites, if exists) the byte array to the file denoted in the path.
	 * @param path path where to save
	 * @param data file data
	 */
	void save(Path path, byte[] data);
	
	/**
	 * Returns a byte array of the contents in the file denoted in the path. 
	 * @param path path of file
	 * @return file bytes
	 */
	byte[] read(Path path);

	/**
	 * Returns the list of files under the directory denoted in the path.
	 * @param path directory path
	 * @return list of files 
	 */
	List<String> list(Path path);
}
