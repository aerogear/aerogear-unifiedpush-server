package org.jboss.aerogear.unifiedpush.dao;

/**
 * Generic base interface for different DAO layers
 * @param <O> Object type
 * @param <K> primary key
 */
public interface GenericBaseDao<O, K> {

    O find(K id);

    O create(O o);

    O update(O o);

    void delete(O o);

}
