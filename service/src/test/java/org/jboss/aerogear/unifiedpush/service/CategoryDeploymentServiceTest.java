package org.jboss.aerogear.unifiedpush.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.CategoryDao;
import org.junit.Test;


public class CategoryDeploymentServiceTest extends AbstractBaseServiceTest {

	@Inject
    private PushApplicationService applicationService;
	
	@Inject
	private GenericVariantService variantService;
	
	@Inject
	private CategoryDeploymentService deploymentService;
	
	@Inject
	private CategoryDao categoryDao;
	
	private PushApplication application;
	
	@Override
	protected void specificSetup() {
		application = new PushApplication();
		application.setName("my application");
		
		AndroidVariant androidVariant = new AndroidVariant();
		androidVariant.setGoogleKey("Key");
        androidVariant.setName("Android");
        androidVariant.setDeveloper("me");
        
        variantService.addVariant(androidVariant);
        applicationService.addPushApplication(application);
	}
	
	@Test
	public void testDeployCategories() {
		Map<String, List<String>> categoryData = new HashMap<>();
		categoryData.put("cat1", Arrays.asList("a", "b", "c"));
		categoryData.put("cat2", Arrays.asList("b", "c", "d"));
		
		deploymentService.deployCategories(application, categoryData);
				
		assertThat(categoryDao.findByPushApplicationID(application.getPushApplicationID())).
			containsExactly(new Category("cat1"), new Category("cat2"));
		
		// check they are overwritten
		categoryData.clear();
		categoryData.put("cat3", Arrays.asList("a", "b", "c"));
		
		deploymentService.deployCategories(application, categoryData);
		
		assertThat(categoryDao.findByPushApplicationID(application.getPushApplicationID())).
			containsExactly(new Category("cat3"));
	}

}
