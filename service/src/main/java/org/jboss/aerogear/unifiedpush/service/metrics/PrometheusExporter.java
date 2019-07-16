package org.jboss.aerogear.unifiedpush.service.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;

import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class PrometheusExporter {

    private final static PrometheusExporter INSTANCE = new PrometheusExporter();

    private final Counter totalDeviceRegisterRequests = Counter.build()
            .name("aerogear_ups_device_register_requests_total")
            .help("Total number of Device register requests.")
            .register();

    private static final Counter totalPushRequests = Counter.build()
            .name("aerogear_ups_push_requests_total")
            .help("Total number of push requests.")
            .register();

    private static final Counter totalPushRequestsFail = Counter.build()
            .name("aerogear_ups_push_requests_fail_total")
            .help("Total number of push requests fail.")
            .register();

    private static final Counter totalPushAndroidRequests = Counter.build()
            .name("aerogear_ups_push_requests_android")
            .help("Total number of Android push batch requests.")
            .register();

    private static final Counter totalPushIosRequests = Counter.build()
            .name("aerogear_ups_push_requests_ios")
            .help("Total number of iOS push batch requests.")
            .register();

    public static PrometheusExporter instance() {
        return INSTANCE;
    }

    private PrometheusExporter() {
        DefaultExports.initialize();
    }

    public void increaseTotalDeviceRegisterRequests() {
        totalDeviceRegisterRequests.inc();
    }

    public void increaseTotalPushRequests() {
        totalPushRequests.inc();
    }

    public void increaseTotalPushRequestsFail() {
        totalPushRequestsFail.inc();
    }

    public void increaseTotalPushIosRequests() {
        totalPushIosRequests.inc();
    }

    public void increasetotalPushAndroidRequests() {
        totalPushAndroidRequests.inc();
    }

    public StreamingOutput metrics() {

        return output -> {
            try (final Writer writer = new OutputStreamWriter(output)) {
                TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            }
        };
    }


}
