package org.jboss.aerogear.unifiedpush.service.metrics;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;

public interface IPushMessageMetricsService {

	FlatPushMessageInformation storeNewRequestFrom(String pushAppId, String json, String ipAddress,
			String clientIdentifier);

	void updatePushMessageInformation(FlatPushMessageInformation pushMessageInformation);

	void appendError(FlatPushMessageInformation pushMessageInformation, Variant variant, String errorMessage);

	PageResult<FlatPushMessageInformation, MessageMetrics> findAllFlatsForPushApplication(String pushApplicationID,
			String search, boolean sorting, Integer page, Integer pageSize);

	/**
	 * Returns number of push messages for given push application ID
	 *
	 * @param pushApplicationId the push app ID
	 *
	 * @return the number of message for the given push application
	 */
	long countMessagesForPushApplication(String pushApplicationId);

	/**
	 *  We trigger a delete of all {@link org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation} objects that are
	 *  <i>older</i> than 30 days!
	 */
	void deleteOutdatedFlatPushInformationData();

	void updateAnalytics(String aerogearPushId);

	FlatPushMessageInformation getPushMessageInformation(String id);

}