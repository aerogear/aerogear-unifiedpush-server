package org.jboss.aerogear.unifiedpush.service.file;

import java.nio.file.Path;
import java.util.List;

public interface FileManager {
	void save(Path path, byte[] data);
	
	byte[] read(Path path);

	List<String> list(Path path);
}
