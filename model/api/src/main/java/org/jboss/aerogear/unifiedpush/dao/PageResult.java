package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

/**
 * Holds the result of a query plus the total number of rows
 * @param <T> the type of the result
 */
public class PageResult<T> {

    private final long count;
    private final List<T> resultList;

    public PageResult(List<T> resultList, long count) {
        this.count = count;
        this.resultList = resultList;
    }

    public long getCount() {
        return count;
    }

    public List<T> getResultList() {
        return resultList;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "count=" + count +
                ", resultList=" + resultList +
                '}';
    }
}
