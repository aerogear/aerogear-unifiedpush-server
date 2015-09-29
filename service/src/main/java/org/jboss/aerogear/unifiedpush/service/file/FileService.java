package org.jboss.aerogear.unifiedpush.service.file;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

/**
 *	A service for saving and retrieving files saved for an alias.
 */
public interface FileService {
	
	/**
	 * Saves a file for the given alias.
	 * 
	 * @param pushApp push application of the alias
	 * @param alias alias to save the file for
	 * @param fileName name of the file
	 * @param data file bytes
	 */
	void writeForAlias(PushApplication pushApp, String alias, String fileName, byte[] data);
	
	/**
	 * Returns all the files saved for the given alias.
	 * 
	 * @param pushApp push application of the alias
	 * @param alias 
	 * @return list of files
	 */
	List<String> getFilesForAlias(PushApplication pushApp, String alias);
	
	/**
	 * Returns a byte array of the requested file for the alias.
	 * @param pushApp push application of the alias
	 * @param alias alias 
	 * @param fileName name of the file
	 * @return byte array of the file
	 */
	byte[] readFileForAlias(PushApplication pushApp, String alias, String fileName);
}
