package org.jboss.aerogear.unifiedpush.rest.config;

import com.google.gson.Gson;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

public final class DocLinks {
    private final static String DEFAULT_LINKS = "/doc-links.json";
    private static DocLinks INSTANCE = null;

    private static final Logger logger = LoggerFactory.getLogger(DocLinks.class);

    private DocLinks() {
    }

    private static class StreamUtils {
        public static String streamToString(InputStream in) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            BufferedInputStream bin = new BufferedInputStream(in);

            int count;
            byte[] buff = new byte[512];

            do {
                count = bin.read(buff);
                if (count > 0) {
                    bout.write(buff, 0, count);
                }
            } while(count > 0);

            return new String(bout.toByteArray());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDocsLinks(InputStream jsonStream) throws IOException {
        String json = StreamUtils.streamToString(jsonStream);
        Gson gson = new Gson();
        return gson.fromJson(json, Map.class);
    }

    public Map<String, String> getDocsLinks() {
        final Map<String, String> links;

        // Load the default links
        try(InputStream defaultDocLink = getClass().getClassLoader().getResourceAsStream(DEFAULT_LINKS)) {
            links = getDocsLinks(defaultDocLink);
        } catch (Exception e) {
            throw new IllegalStateException("Error parsing default doc links", e);
        }

        // Load custom links
        final String customLinksFilePath = ConfigurationUtils.tryGetGlobalProperty("DOCS_PATH");

        if (customLinksFilePath != null) {
            try(InputStream customDocLinks = new FileInputStream(customLinksFilePath)) {
                links.putAll(getDocsLinks(customDocLinks));
            } catch (Exception e) {
                logger.error("An error has occurred parsing the custom doc link file at '" + customLinksFilePath + "': " + e.getMessage(), e);
            }
        }

        return links;
    }

    public static DocLinks getInstance() {
        if (INSTANCE == null) {
            synchronized (DocLinks.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DocLinks();
                }
            }
        }

        return INSTANCE;
    }
}
