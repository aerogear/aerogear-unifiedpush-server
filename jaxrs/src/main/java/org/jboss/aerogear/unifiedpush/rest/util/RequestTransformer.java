/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import org.jboss.aerogear.unifiedpush.rest.util.transform.DynamicTransformer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;

/**
 * Basic request transformer
 */
@RequestScoped
public class RequestTransformer {
    private final Logger logger = Logger.getLogger(RequestTransformer.class.getName());

    private static final String OPERATIONS = "operations";
    private static final String TRANSFORMER = "dynamicTransformer";

    public StringBuilder transform(String path, String version, StringBuilder json) {
        try {
            final JsonNode node = locateJsonPatch(path, version);
            final JsonNode operations = node.findPath(OPERATIONS);
            final JsonNode patch;
            if (patchHasDynamicTransformer(operations)) {
                patch = operations;
            } else {
                patch = node;
            }

            final JsonNode patched = transformJson(patch, json);
            return convertToStringBuilder(applyDynamicTransformer(patched, node));
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("could not find/load path file for version '%s' and path '%s'", version, path), e);
            return json;
        } catch (JsonPatchException e) {
            throw new RuntimeException("Error in json patch file", e);
        }
    }

    private boolean patchHasDynamicTransformer(JsonNode operations) {
        return !operations.isMissingNode();
    }

    private JsonNode locateJsonPatch(String path, String version) throws IOException {
        return JacksonUtils.getReader().readTree(getClass().getResourceAsStream(path + "/" + version + ".json"));
    }

    private JsonNode transformJson(JsonNode patch, StringBuilder json) throws IOException, JsonPatchException {
        JsonNode jsonNode = convertToJsonNode(json);
        for (JsonNode operation : patch) {
            try {
                final ArrayNode nodes = JsonNodeFactory.instance.arrayNode();
                nodes.add(operation);
                JsonPatch patchOperation = JsonPatch.fromJson(nodes);
                jsonNode = patchOperation.apply(jsonNode);
            } catch (JsonPatchException e) {
                logger.log(Level.FINEST, "ignore field not found");
            }
        }
        return jsonNode;
    }

    private JsonNode convertToJsonNode(StringBuilder json) throws IOException {
        return JacksonUtils.getReader().readTree(json.toString());
    }

    private StringBuilder convertToStringBuilder(JsonNode dynamicTransformedNode) {
        return new StringBuilder(dynamicTransformedNode.toString());
    }

    JsonNode applyDynamicTransformer(JsonNode json, JsonNode patch) throws IOException {
        String clazz = patch.findPath(TRANSFORMER).textValue();
        if (clazz != null) {
            try {
                final DynamicTransformer dynamicTransformer = (DynamicTransformer) Class.forName(getClass().getPackage().getName() + ".transform." + clazz).newInstance();
                return dynamicTransformer.transform(json);
            } catch (Exception e) {
                throw new RuntimeException("error in json patch could not instantiate / find dynamic transformer", e);
            }
        }
        return json;
    }
}
