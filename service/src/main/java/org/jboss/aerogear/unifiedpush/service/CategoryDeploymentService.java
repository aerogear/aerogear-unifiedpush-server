package org.jboss.aerogear.unifiedpush.service;

import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.PushApplication;

public interface CategoryDeploymentService {
	
	/**
	 * Creates categories (and properties) mirroring the data received in {@code categoryData},
	 * and associates them to the given application. If some installation's alias matches one
	 * of the properties, the respective category will also be added to that installation. Note that
	 * this application's existing categories will be overwritten by the newly created categories.
	 * 
	 * @param application application to associate the categories to
	 * @param categoryData a map representing categories and properties. Each key in the map
	 * 	is a category's name, and a list represents that category's properties.
	 */
	void deployCategories(PushApplication application, Map<String, List<String>> categoryData);

}
