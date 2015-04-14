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
package org.jboss.aerogear.unifiedpush.dao;

/**
 * Streams the batch of the results
 */
public interface ResultsStream<T> {

    /**
     * Advance to the next result
     * @return <tt>true</tt> if there is another result
     */
    boolean next() throws ResultStreamException;

    /**
     * Get the current row of the result as an object
     * @return an object constructed from current row
     */
    T get() throws ResultStreamException;

    /**
     * Allow to build new {@link ResultsStream} with customized fetching strategy.
     */
    public static interface QueryBuilder<T> {

        /**
         * Set a fetch size for the underlying JDBC query. See org.hibernate.Query.setFetchSize(int).
         */
        QueryBuilder<T> fetchSize(int fetchSize);

        /**
         * Builds the query and constructs the {@link ResultsStream} from underlying org.hibernate.ScrollableResults object.
         */
        ResultsStream<T> executeQuery();

    }
}
