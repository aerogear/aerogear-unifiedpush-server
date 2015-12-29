package org.jboss.aerogear.unifiedpush.service.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
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
	 * Returns the list of files under the directory denoted in the path and match the given file filter.
	 * @param path directory path
	 * @param filter predicate for files
	 * @return list of files 
	 */
	List<File> list(Path path, FileFilter filter) throws FileNotFoundException;
}
