package org.jboss.aerogear.unifiedpush.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
        return DocLinks.getInstance().getDocsLinks();
    }
}
