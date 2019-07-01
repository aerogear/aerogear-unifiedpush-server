package org.jboss.aerogear.unifiedpush.rest.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLUtils {
	private static final Logger logger = LoggerFactory.getLogger(URLUtils.class);

	public static String removeLastSlash(String url) {
		if (url.lastIndexOf("/") == url.length() - 1) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public static String getLastPart(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.error("unable to create URI from url: " + url, e);
			return null;
		}
		String path = removeLastSlash(uri.getPath());
		return path.substring(path.lastIndexOf('/') + 1);
	}
}
