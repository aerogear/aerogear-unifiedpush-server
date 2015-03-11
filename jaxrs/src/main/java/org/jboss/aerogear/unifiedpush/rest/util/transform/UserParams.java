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
package org.jboss.aerogear.unifiedpush.rest.util.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;

/**
 * UserParams is a DynamicTransformer that moves all user parameters to a separate user-data section.
 */
public class UserParams implements DynamicTransformer {
    private static final List<String> KNOWN_KEYS = Arrays.asList("alert", "apns", "sound", "badge",
            "simple-push", "user-data");
    public static final String MOVE_OP = "['{'\"op\": \"move\",\"from\": \"/message/{0}\",\"path\": \"/message/user-data/{0}\"'}']";

    @Override
    public JsonNode transform(JsonNode patched) throws IOException {
        Iterator<Map.Entry<String, JsonNode>> nodeIterator = patched.get("message").fields();
        while (nodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodeIterator.next();

            if (!KNOWN_KEYS.contains(entry.getKey())) {
                String json = format(MOVE_OP, entry.getKey());
                try {
                    patched = JsonPatch.fromJson(JacksonUtils.getReader().readTree(json)).apply(patched);
                } catch (JsonPatchException e) {
                    throw new RuntimeException("move operation could not be applied", e);
                }
            }
        }

        return patched;
    }
}
