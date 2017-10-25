/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.token;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.configuration.SenderConfiguration;
import org.jboss.aerogear.unifiedpush.message.event.AllBatchesLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.TopicProcessor;
import reactor.core.publisher.WorkQueueProcessor;

/**
 * Receives a request for sending a push message to given variants from
 * {@link NotificationRouter}.
 *
 * Loads device token batches from a database and queues them for processing
 * inside a message holder.
 *
 * {@link TokenLoader} uses result stream with configured fetch size so that it
 * can split database results into several batches.
 */
@Service
@Transactional
public class TokenLoader {

	private final Logger logger = LoggerFactory.getLogger(TokenLoader.class);

	@Inject
	private ClientInstallationService clientInstallationService;

	@Inject
	private WorkQueueProcessor<MessageHolderWithTokens> dispatchTokensEvent;

	@Inject
	private TopicProcessor<MessageHolderWithVariants> nextBatchEvent;

	@Inject
	private ApplicationContext context;
	@Inject
	private TokenLoaderWrapper wrapper;

	@PostConstruct
	public void subscribe() {
		nextBatchEvent.repeat().subscribe(s -> wrapper.loadAndQueueTokenBatch(s));
	}

	/**
	 * Receives request for processing a {@link UnifiedPushMessage} and loads
	 * tokens for devices that match requested parameters from database.
	 *
	 * Device tokens are loaded in a stream and split to batches of configured
	 * size (see {@link SenderConfiguration#batchSize()}). Once the
	 * pre-configured number of batches (see
	 * {@link SenderConfiguration#batchesToLoad()}) is reached, this method
	 * resends message to the same queue it took the request from, so that the
	 * transaction it worked in is split and further processing may continue in
	 * next transaction.
	 *
	 * Additionally it fires {@link BatchLoadedEvent} as CDI event (that is
	 * translated to JMS event). When all batches were loaded for the given
	 * variant, it fires {@link AllBatchesLoadedEvent}.
	 *
	 * @param msg
	 *            holder object containing the payload and info about the
	 */

	@Transactional(readOnly = true)
	public void loadAndQueueTokenBatch(MessageHolderWithVariants msg) throws IllegalStateException {
		final UnifiedPushMessage message = msg.getUnifiedPushMessage();
		final VariantType variantType = msg.getVariantType();
		final Collection<Variant> variants = msg.getVariants();
		final String lastTokenFromPreviousBatch = msg.getLastTokenFromPreviousBatch();

		final SenderConfiguration configuration = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
				context.getAutowireCapableBeanFactory(), SenderConfiguration.class, variantType.name());

		int serialId = msg.getLastSerialId();

		logger.debug("Received message from queue: {}", message.getMessage().getAlert());

		final Criteria criteria = message.getCriteria();
		final List<String> categories = criteria.getCategories();
		final List<String> aliases = criteria.getAliases();
		final List<String> deviceTypes = criteria.getDeviceTypes();

		logger.info(String.format(
				"Preparing message delivery and loading tokens for the %s 3rd-party Push Network (for %d variants)",
				variantType, variants.size()));

		for (Variant variant : variants) {

			try {

				ResultsStream<String> tokenStream;
				final Set<String> topics = new TreeSet<>();
				final boolean isAndroid = variantType == VariantType.ANDROID;

				// the entire batch size
				int batchesToLoad = configuration.batchesToLoad();

				// Some checks for GCM, because of GCM-3 topics
				boolean gcmTopicRequest = (isAndroid && TokenLoaderUtils.isGCMTopicRequest(criteria));
				if (gcmTopicRequest) {

					// If we are able to do push for GCM topics...

					// 1)
					// find all topics, BUT only on the very first round of
					// batches
					// otherwise after 10 (or what ever the max. is) another
					// request would be sent to that topic
					if (serialId == 0) {
						topics.addAll(TokenLoaderUtils.extractGCMTopics(criteria, variant.getVariantID()));

						// topics are handled as a first extra batch,
						// therefore we have to adjust the number by adding this
						// extra batch
						batchesToLoad += 1;
					}

					// 2) always load the legacy tokens, for all number of batch
					// iterations
					tokenStream = clientInstallationService
							.findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(variant.getVariantID(),
									categories, aliases, deviceTypes, configuration.tokensToLoad(),
									lastTokenFromPreviousBatch)
							.fetchSize(configuration.batchSize()).executeQuery();
				} else {
					tokenStream = clientInstallationService
							.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases,
									deviceTypes, configuration.tokensToLoad(), lastTokenFromPreviousBatch)
							.fetchSize(configuration.batchSize()).executeQuery();
				}

				String lastTokenInBatch = null;
				int tokensLoaded = 0;
				for (int batchNumber = 0; batchNumber < batchesToLoad; batchNumber++) {

					// increasing the serial ID,
					// to make sure it's properly read from all block
					++serialId;

					final Set<String> tokens = new TreeSet<>();

					// On Android, the first batch is for GCM3 topics
					// legacy tokens are submitted in the batch #2 and later
					if (isAndroid && batchNumber == 0 && !topics.isEmpty()) {
						tokens.addAll(topics);
					} else {
						for (int i = 0; i < configuration.batchSize() && tokenStream.next(); i++) {
							lastTokenInBatch = tokenStream.get();
							tokens.add(lastTokenInBatch);
							tokensLoaded += 1;
						}
					}

					if (tokens.size() > 0) {
						if (tryToDispatchTokens(new MessageHolderWithTokens(msg.getPushMessageInformation(), message,
								variant, tokens, serialId))) {
							logger.info(String.format("Loaded batch #%s, containing %d tokens, for %s variant (%s)",
									serialId, tokens.size(), variant.getType().getTypeName(), variant.getVariantID()));
						} else {
							logger.debug(String.format(
									"Failing token loading transaction for batch token #%s for %s variant (%s), since previous batch failed",
									serialId, variant.getType().getTypeName(), variant.getVariantID()));
							return;
						}
					} else {
						logger.debug("Ending batch processing: No more tokens for batch #{} available", serialId);
						break;
					}
				}

				// should we trigger next transaction batch ?
				if (tokensLoaded >= configuration.tokensToLoad()) {
					logger.debug(String.format("Ending token loading transaction for %s variant (%s)",
							variant.getType().getTypeName(), variant.getVariantID()));
					nextBatchEvent.onNext(new MessageHolderWithVariants(msg.getPushMessageInformation(), message,
							msg.getVariantType(), variants, serialId, lastTokenInBatch));
				} else {
					logger.debug("All batches for {} variant were loaded ({})", variant.getType().getTypeName(),
							variant.getVariantID());

					// using combined key of variant and PMI (AGPUSH-1585):
					// allBatchesLoaded.fire(new
					// AllBatchesLoadedEvent(variant.getVariantID()+":"+msg.getPushMessageInformation().getId()));

					if (tokensLoaded == 0 && lastTokenFromPreviousBatch == null) {
						// no tokens were loaded at all!
						if (gcmTopicRequest) {
							logger.debug("No legacy(non-InstanceID) tokens found. Just pure GCM topic requests");
						} else {
							logger.warn(
									"Check your push query: 0 tokens were loaded from the DB. Variant type {} name {}",
									variant.getType().name(), variant.getName());
						}
					}
				}
			} catch (Throwable e) {
				logger.info("Failed to load batch of tokens for message! {}", msg);
				logger.error("Failed to load batch of tokens", e);
				// TODO - Implement a recovery mechanism, message request is
				// lost.
			}
		}
	}

	/**
	 * Tries to dispatch tokens; returns true if tokens were successfully
	 * queued.
	 *
	 * @return returns true if tokens were successfully queued; returns false if
	 *         failed.
	 */
	private boolean tryToDispatchTokens(MessageHolderWithTokens msg) {
		try {
			if (!dispatchTokensEvent.alive())
				return false;

			dispatchTokensEvent.onNext(msg);
			return true;
		} catch (Exception e) {
			logger.error("Failed to submit MessageHolderWithTokens to Flux", e);
			return false;
		}
	}

	public static class TokenLoaderWrapperConfig {
		@Bean
		public TokenLoaderWrapper getTokenLoaderWrapper() {
			return new TokenLoaderWrapper();
		}
	}

	public static class TokenLoaderWrapper {
		@Autowired
		private TokenLoader tokenLoader;

		public TokenLoader getTokenLoader() {
			return tokenLoader;
		}

		public void setTokenLoader(TokenLoader tokenLoader) {
			this.tokenLoader = tokenLoader;
		}

		public void loadAndQueueTokenBatch(MessageHolderWithVariants msg) throws IllegalStateException {
			tokenLoader.loadAndQueueTokenBatch(msg);
		}
	}

}
