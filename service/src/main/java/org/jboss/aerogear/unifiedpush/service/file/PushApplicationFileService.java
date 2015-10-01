package org.jboss.aerogear.unifiedpush.service.file;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface PushApplicationFileService {
	void save(PushApplication pushApp, String fileName, byte[] data);
	
	List<String> getFiles(PushApplication pushApp);
	
	byte[] readFile(PushApplication pushApp, String fileName);
}
