package org.jboss.aerogear.unifiedpush.service.file;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface FileService {
	void writeForAlias(PushApplication pushApp, String alias, byte[] data);
	
	List<String> getFilesForAlias(PushApplication pushApp, String alias);
	
	byte[] readFileForAlias(PushApplication pushApp, String alias, String fileName);
}
