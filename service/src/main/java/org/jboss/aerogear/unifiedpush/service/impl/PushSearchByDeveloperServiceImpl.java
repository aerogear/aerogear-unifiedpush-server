/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.service.dashboard.Application;
import org.jboss.aerogear.unifiedpush.service.dashboard.ApplicationVariant;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the <code>PushSearchService</code> internally used for
 * 'developer' role, to query for a restricted set of data, that is tied to a
 * specific user/login name.
 */
@Service
@Qualifier("PushSearchByDeveloperServiceImpl")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class PushSearchByDeveloperServiceImpl implements PushSearchService {

	@Inject
	private PushApplicationDao pushApplicationDao;

	@Inject
	private VariantDao variantDao;

	@Inject
	private InstallationDao installationDao;

	@Inject
	private FlatPushMessageInformationDao flatPushMessageInformationDao;

	private LoggedInUser loginName;

	public void setLoginName(LoggedInUser loginName) {
		this.loginName = loginName;
	}

	@Override
	public PageResult<PushApplication, Count> findAllPushApplicationsForDeveloper(Integer page, Integer pageSize) {
		return pushApplicationDao.findAllForDeveloper(loginName.get(), page, pageSize);
	}

	@Override
	public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID) {
		return pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());
	}

	@Override
	public boolean existsVariantIDForDeveloper(String variantID) {
		return variantDao.existsVariantIDForDeveloper(variantID, loginName.get());
	}

	/**
	 * Receives the dashboard data for the given user
	 */
	@Override
	public DashboardData loadDashboardData() {

		long totalApps = totalApplicationNumber();
		long totalDevices = totalDeviceNumber();
		long totalMessages = totalMessages();

		final DashboardData data = new DashboardData();
		data.setApplications(totalApps);
		data.setDevices(totalDevices);
		data.setMessages(totalMessages);

		return data;
	}

	/**
	 * Loads all the Variant objects where we did notice some failures on
	 * sending for the given user
	 */
	@Override
	public List<ApplicationVariant> getVariantsWithWarnings() {
		final List<String> warningIDs = flatPushMessageInformationDao.findVariantIDsWithWarnings(loginName.get());
		if (warningIDs.isEmpty()) {
			return Collections.emptyList();
		}

		return wrapApplicationVariant(pushApplicationDao.findByVariantIds(warningIDs));
	}

	/**
	 * Loads all the Variant objects with the most received messages
	 */
	@Override
	public List<Application> getLatestActivity(int maxResults) {
		return wrapApplication(flatPushMessageInformationDao.findLatestActivity(loginName.get(), maxResults));
	}

	@Override
	public PageResult<Installation, Count> findAllInstallationsByVariantForDeveloper(String variantID, Integer page,
			Integer pageSize, String search) {
		return installationDao.findInstallationsByVariantForDeveloper(variantID, loginName.get(), page, pageSize,
				search);
	}

	private long totalMessages() {
		return flatPushMessageInformationDao.getNumberOfPushMessagesForLoginName(loginName.get());
	}

	private long totalDeviceNumber() {
		return installationDao.getNumberOfDevicesForLoginName(loginName.get());
	}

	private long totalApplicationNumber() {
		return pushApplicationDao.getNumberOfPushApplicationsForDeveloper(loginName.get());
	}

	private List<ApplicationVariant> wrapApplicationVariant(List<PushApplication> applications) {
		final List<ApplicationVariant> applicationVariants = new ArrayList<>(applications.size());

		applications.forEach(application -> {
			application.getVariants().forEach(variant -> {
				final ApplicationVariant applicationVariant = new ApplicationVariant(application, variant);
				applicationVariants.add(applicationVariant);
			});
		});

		return applicationVariants;
	}

	private List<Application> wrapApplication(List<FlatPushMessageInformation> pushMessageInformations) {
		final List<Application> applications = new ArrayList<>(pushMessageInformations.size());

		pushMessageInformations.forEach(pushMessageInformation -> {
			final String applicationName = pushApplicationDao
					.findByPushApplicationID(pushMessageInformation.getPushApplicationId()).getName();
			final Application application = new Application(applicationName,
					pushMessageInformation.getPushApplicationId(), pushMessageInformation.getSubmitDate());
			applications.add(application);
		});
		return applications;
	}
}
