package org.jboss.aerogear.unifiedpush.dao;

/**
 * Streams the batch of the results
 *
 * @author Lukas Fryc
 */
public interface ResultsStream<T> {

    /**
     * Advance to the next result
     * @return <tt>true</tt> if there is another result
     */
    boolean next() throws BatchException;

    /**
     * Get the current row of the result as an object
     * @return an object constructed from current row
     */
    T get() throws BatchException;

    /**
     * Allow to build new {@link ResultsStream} with customized fetching strategy.
     *
     * @author Lukas Fryc
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
