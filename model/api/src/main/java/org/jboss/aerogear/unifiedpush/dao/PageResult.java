package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

/**
 * Holds the result of a query plus the total number of rows
 * @param <T> the type of the result
 */
public class PageResult<T, A> {

    private final List<T> resultList;
    private final A aggregate;

    public PageResult(List<T> resultList, A aggregate) {
        this.resultList = resultList;
        this.aggregate = aggregate;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public A getAggregate() {
        return aggregate;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "aggregate=" + aggregate +
                ", resultList=" + resultList +
                '}';
    }
}
