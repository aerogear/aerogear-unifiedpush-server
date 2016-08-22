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
package org.jboss.aerogear.unifiedpush.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Holds device token information.
 *
 * May be extended if a variant requires additional field for sending push message.
 */
public class Token implements Serializable, Comparable<Token> {

    private static final long serialVersionUID = -3782844785133457996L;

    private final String endpoint;

    public Token(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint can not be null");
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public int compareTo(Token that) {
        return this.endpoint.compareTo(that.endpoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        return Objects.equals(endpoint, token.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint);
    }

    /**
     * Transforms collection of {@link Token} objects to collections of strings for backward compatibility.
     */
    public static List<String> toEndpoints(Collection<Token> tokens) {
        return tokens.stream()
                .map(Token::getEndpoint)
                .collect(Collectors.toList());
    }

    public static Set<Token> toTokens(Collection<String> endpoints) {
        return endpoints.stream()
                .map(Token::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
