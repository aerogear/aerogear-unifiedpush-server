package org.jboss.aerogear.unifiedpush.kafka.streams;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import net.wessendorf.kafka.serialization.CafdiSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.kafka.KafkaClusterConfig;
import org.jboss.aerogear.unifiedpush.kafka.serdes.InternalUnifiedPushMessageSerde;
import org.jboss.aerogear.unifiedpush.kafka.serdes.PushApplicationSerde;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.jboss.aerogear.unifiedpush.api.VariantType.*;


@Singleton
@Startup
public class NotificationRouterStreamsHook {

    private final static Logger logger = LoggerFactory.getLogger(NotificationRouterStreamsHook.class);

    private KafkaStreams streams;

    private final String STREAMS_INPUT_TOPIC = KafkaClusterConfig.NOTIFICATION_ROUTER_STREAMS_INPUT_TOPIC;

    public final String ADM_TOPIC = "agpush_admPushMessageTopic";

    public final String ANDROID_TOPIC = "agpush_gcmPushMessageTopic";

    public final String IOS_TOPIC = "agpush_apnsPushMessageTopic";

    public final String SIMPLE_PUSH_TOPIC = "agpush_simplePushMessageTopic";

    public final String WINDOWS_MPNS_TOPIC = "agpush_mpnsPushMessageTopic";

    public final String WINDOWS_WNS_TOPIC = "agpush_wnsPushMessageTopic";


    @Resource(name = "DefaultManagedExecutorService")
    private ManagedExecutorService executor;

    @Inject
    private Instance<GenericVariantService> genericVariantService;

    @PostConstruct
    private void startup() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "notification-router-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, net.wessendorf.kafka.cdi.extension.VerySimpleEnvironmentResolver.simpleBootstrapServerResolver("#{KAFKA_HOST}:#{KAFKA_PORT}"));
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, PushApplicationSerde.class);
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, InternalUnifiedPushMessageSerde.class);

        // Initialize specific serdes to be used later
        final Serde<InternalUnifiedPushMessage> pushMessageSerde = CafdiSerdes.serdeFrom(InternalUnifiedPushMessage.class);
        final Serde<Variant> variantSerde = CafdiSerdes.serdeFrom(Variant.class);

        KStreamBuilder builder = new KStreamBuilder();

        // Read from the source stream
        KStream<PushApplication, InternalUnifiedPushMessage> source = builder.stream(STREAMS_INPUT_TOPIC);

        // For each push message, get its variants and create records for each one with
        // key/value pair (InternalUnifiedPushMessage, Variant)
        KStream<InternalUnifiedPushMessage, Variant> getVariants = source.flatMap(
                (app, message) -> {
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
                        variants.addAll(app.getVariants());
                    }

                    logger.warn("got variants.." + variants.toString());

                    List<KeyValue<InternalUnifiedPushMessage, Variant>> result = new LinkedList<>();
                    variants.forEach((variant) -> {
                        result.add(KeyValue.pair(message, variant));
                    });

                    return result;
                }
        );

        // Branch based on variant types
        KStream<InternalUnifiedPushMessage, Variant>[] branches = getVariants.branch(
                (message, variant) -> variant.getType().equals(ADM),
                (message, variant) -> variant.getType().equals(ANDROID),
                (message, variant) -> variant.getType().equals(IOS),
                (message, variant) -> variant.getType().equals(SIMPLE_PUSH),
                (message, variant) -> variant.getType().equals(WINDOWS_MPNS),
                (message, variant) -> variant.getType().equals(WINDOWS_WNS)
        );

        // Stream each branch to respective topic
        branches[0].to(pushMessageSerde, variantSerde, ADM_TOPIC);
        branches[1].to(pushMessageSerde, variantSerde, ANDROID_TOPIC);
        branches[2].to(pushMessageSerde, variantSerde, IOS_TOPIC);
        branches[3].to(pushMessageSerde, variantSerde, SIMPLE_PUSH_TOPIC);
        branches[4].to(pushMessageSerde, variantSerde, WINDOWS_MPNS_TOPIC);
        branches[5].to(pushMessageSerde, variantSerde, WINDOWS_WNS_TOPIC);

        streams = new KafkaStreams(builder, props);
        streams.start();
    }


    @PreDestroy
    private void shutdown() {
        logger.debug("Shutting down the streams.");
        streams.close();
    }

}