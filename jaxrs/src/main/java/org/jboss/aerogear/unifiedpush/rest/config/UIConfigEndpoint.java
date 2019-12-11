package org.jboss.aerogear.unifiedpush.rest.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;

/**
 * Provides the following ui options * Documentation Links * Disabled Services
 */
@Path("/ui/config")
public class UIConfigEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> uiConfig() throws Exception {

        String disabled = ConfigurationUtils.tryGetGlobalProperty("UPS_DISABLED");
        String docsPath = ConfigurationUtils.tryGetGlobalProperty("DOCS_PATH", "/doc-links.json");
        Map<String, String> docLinks = getDocsLinks(docsPath);
        Map<String, Object> toReturn = new HashMap<>();

        toReturn.put("UPS_DISABLED", disabled);
        toReturn.put("DOCS_LINKS", docLinks);

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDocsLinks(String docsPath) throws Exception {
        InputStream stream;

        stream = getClass().getClassLoader().getResourceAsStream(docsPath);
        if (stream == null) {
            File file = new File(docsPath);
            stream = new FileInputStream(file);
        }

        String json = readAsSting(stream);
        Gson gson = new Gson();
        return gson.fromJson(json, Map.class);
    }

    private String readAsSting(InputStream stream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try {

            int data = stream.read();
            while (data != -1) {
                builder.append((char) data);
                data = stream.read();
            }
        } finally {
            stream.close();
        }
        return builder.toString();
    }

}
