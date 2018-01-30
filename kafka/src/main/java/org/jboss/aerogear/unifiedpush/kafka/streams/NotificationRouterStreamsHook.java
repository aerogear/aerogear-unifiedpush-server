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
package org.jboss.aerogear.unifiedpush.kafka.streams;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.aerogear.kafka.cdi.extension.VerySimpleEnvironmentResolver;
import org.aerogear.kafka.serialization.CafdiSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.kafka.KafkaClusterConfig;
import org.jboss.aerogear.unifiedpush.kafka.serdes.InternalUnifiedPushMessageSerde;
import org.jboss.aerogear.unifiedpush.kafka.serdes.PushApplicationSerde;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.jboss.aerogear.unifiedpush.api.VariantType.ADM;
import static org.jboss.aerogear.unifiedpush.api.VariantType.ANDROID;
import static org.jboss.aerogear.unifiedpush.api.VariantType.IOS;
import static org.jboss.aerogear.unifiedpush.api.VariantType.WINDOWS_WNS;

/**
 * Class that will be initialized on startup, which reads messages from the input topic
 * {@link KafkaClusterConfig#NOTIFICATION_ROUTER_STREAMS_INPUT_TOPIC} containing key/value pair (PushApplication, InternalUnifiedPushMessage).
 *
 * These records are split into subrecords, based on the message's variants. This allows messages to be processed separately,
 * giving attention to limitations and requirements of specific push networks.
 *
 * Once processing is performed, records are streamed to output topics based on their variant type for further processing
 * in {@link org.jboss.aerogear.unifiedpush.message.token.TokenLoader}.
 */
@ApplicationScoped
public class NotificationRouterStreamsHook {

    private final static Logger logger = LoggerFactory.getLogger(NotificationRouterStreamsHook.class);

    private KafkaStreams streams;

    private final String STREAMS_INPUT_TOPIC = KafkaClusterConfig.NOTIFICATION_ROUTER_STREAMS_INPUT_TOPIC;

    private final String ADM_TOPIC = "agpush_admPushMessageTopic";

    private final String ANDROID_TOPIC = "agpush_gcmPushMessageTopic";

    private final String IOS_TOPIC = "agpush_apnsPushMessageTopic";

    private final String WINDOWS_WNS_TOPIC = "agpush_wnsPushMessageTopic";


    @Inject
    private Instance<GenericVariantService> genericVariantService;

    @Inject
    private PushMessageMetricsService metricsService;

    /**
     * Records of type (PushApplication, InternalUnifiedPushMessage) are split into subrecords based on the message's
     * variants.
     *
     * Each subrecord is transformed into a record with key/value pair (InternalUnifiedPushMessage, Variant)
     * and streamed to an output topic based on its variant type.
     */
    private void startup(@Observes @Initialized(ApplicationScoped.class) Object init) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "agpush_notificationRouterStreams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, VerySimpleEnvironmentResolver.simpleBootstrapServerResolver("#{KAFKA_SERVICE_HOST}:#{KAFKA_SERVICE_PORT}"));
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, PushApplicationSerde.class);
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, InternalUnifiedPushMessageSerde.class);

        // Initialize specific serdes to be used later
        final Serde<MessageHolderWithVariants> messageHolderSerde = CafdiSerdes.serdeFrom(MessageHolderWithVariants.class);
        final Serde<VariantType> variantTypeSerde = CafdiSerdes.serdeFrom(VariantType.class);

        final KStreamBuilder builder = new KStreamBuilder();

        // Read from the source stream
        final KStream<PushApplication, InternalUnifiedPushMessage> source = builder.stream(STREAMS_INPUT_TOPIC);

        // For each push message, get its variants and create records for each one with
        // key/value pair (InternalUnifiedPushMessage, Variant)
        final KStream<VariantType, MessageHolderWithVariants> getVariants = source.flatMap(
                (pushApplication, message) -> {
                    // collections for all the different variants:
                    final List<Variant> variants = new ArrayList<>();
                    final List<String> variantIDs = message.getCriteria().getVariants();

                    if (variantIDs != null) {
                        variantIDs.forEach(variantID -> {
                            Variant variant = genericVariantService.get().findByVariantID(variantID);

                            // does the variant exist ?
                            if (variant != null) {
                                variants.add(variant);
                            }
                        });
                    } else {
                        // No specific variants have been requested,
                        // we get all the variants, from the given PushApplicationEntity:
                        variants.addAll(pushApplication.getVariants());
                    }

                    logger.warn("got variants.." + variants.toString());

                    // TODO: Not sure the transformation should be done here...
                    // There are likely better places to check if the metadata is way to long
                    String jsonMessageContent = message.toStrippedJsonString() ;
                    if (jsonMessageContent != null && jsonMessageContent.length() >= 4500) {
                        jsonMessageContent = message.toMinimizedJsonString();
                    }

                    final FlatPushMessageInformation pushMessageInformation =
                            metricsService.storeNewRequestFrom(
                                    pushApplication.getPushApplicationID(),
                                    jsonMessageContent,
                                    message.getIpAddress(),
                                    message.getClientIdentifier()
                            );

                    final List<KeyValue<VariantType, MessageHolderWithVariants>> result = new LinkedList<>();
                    variants.forEach((variant) -> {
                        result.add(KeyValue.pair(variant.getType(), new MessageHolderWithVariants(pushMessageInformation, message, variant.getType(), Arrays.asList(variant))));
                    });

                    return result;
                }
        );

        // Branch based on variant types
        final KStream<VariantType, MessageHolderWithVariants>[] branches = getVariants.branch(
                (variantType, holder) -> variantType.equals(ADM),
                (variantType, holder) -> variantType.equals(ANDROID),
                (variantType, holder) -> variantType.equals(IOS),
                (variantType, holder) -> variantType.equals(WINDOWS_WNS)
        );

        // Stream each branch to respective topic
        branches[0].to(variantTypeSerde, messageHolderSerde, ADM_TOPIC);
        branches[1].to(variantTypeSerde, messageHolderSerde, ANDROID_TOPIC);
        branches[2].to(variantTypeSerde, messageHolderSerde, IOS_TOPIC);
        branches[3].to(variantTypeSerde, messageHolderSerde, WINDOWS_WNS_TOPIC);

        streams = new KafkaStreams(builder, props);
        streams.start();
    }


    @PreDestroy
    private void shutdown() {
        logger.debug("Shutting down the streams.");
        streams.close();
    }

}