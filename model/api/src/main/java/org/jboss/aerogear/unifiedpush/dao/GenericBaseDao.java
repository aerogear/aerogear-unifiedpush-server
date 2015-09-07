package org.jboss.aerogear.unifiedpush.dao;

import java.util.List;

/**
 * Generic base interface for different DAO layers
 * @param <O> Object type
 * @param <K> primary key
 */
public interface GenericBaseDao<O, K> {

    O find(K id);

    List<O> findAll();
    	
    void create(O o);

    void update(O o);

    void delete(O o);

    void flushAndClear();

    void lock(O entity);
}
