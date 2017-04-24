package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.Alias;

/**
 * Public Service which wraps Spring DAO. TODO - As soon as we can mixup Spring
 * and EJB IOC, merge this service into AliasService.
 */
public interface AliasCrudService {
	Alias find(UUID pushApplicationId, String alias);

	Alias find(UUID pushApplicationId, UUID userId);

	void remove(String pushApplicationId, String alias);

	void remove(UUID pushApplicationId, String alias);

	/**
	 * Remove all aliases according to given parameters. All aliases are
	 * manually evicted from cache.
	 *
	 * @param pushApplicationId
	 *            Selected push application
	 * @param userId
	 *            Time based UUID
	 */
	void remove(UUID pushApplicationId, UUID userId);

	void removeAll(UUID pushApplicationId);

	void create(Alias alias);
}
