package org.jboss.aerogear.unifiedpush.service.file;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface FileService {
	void writeForAlias(PushApplication pushApp, String alias, byte[] data);
}
