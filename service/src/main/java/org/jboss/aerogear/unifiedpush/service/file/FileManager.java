package org.jboss.aerogear.unifiedpush.service.file;

import java.nio.file.Path;

public interface FileManager {
	void save(Path path, byte[] data);
	
	byte[] read(Path path);
}
