package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Property;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.service.CategoryDeploymentService;

@Stateless
public class CategoryDeploymentServiceImpl implements CategoryDeploymentService {

	@Inject
	private CategoryDao categoryDao;
	
	@Inject
	private InstallationDao installationDao;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deployCategories(PushApplication application, Map<String, List<String>> categoryData) {
		List<Category> categories = overwriteCategories(application, categoryData);
		
		List<String> variantIDs = new ArrayList<>(application.getVariants().size());
		
		for (Variant variant : application.getVariants()) {
			variantIDs.add(variant.getId());
		}
		
		for (Category category : categories) {
			for (Property property : category.getProperties()) {
				List<Installation> installations = installationDao.findInstallationsForVariantsByAlias(variantIDs, property.getName());
				for (Installation installation : installations) {
					if (!installation.getCategories().contains(category)) {
						installation.getCategories().add(category);
						installationDao.update(installation);
					}
				}
			}
		}
	}
	
	private List<Category> overwriteCategories(PushApplication application, Map<String, List<String>> categoryData) {
		categoryDao.deleteByPushApplicationID(application.getPushApplicationID());
		
		List<Category> categories = new ArrayList<>(categoryData.size());
		
		for (Map.Entry<String, List<String>> entry : categoryData.entrySet()) {
			final String name = entry.getKey();
			Category category = new Category(name);
			category.setApplicationId(application.getPushApplicationID());
			Set<Property> properties = new HashSet<>();
					
			for (String propertyName : entry.getValue()) {
				properties.add(new Property(propertyName));
			}
					
			category.setProperties(properties);
			categoryDao.create(category);
			categories.add(category);
		}
		
		return categories;
	}

}
