package org.aergear.ups.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.io.Writer;

@Path("/prometheus")
public class SimplePrometheusEndpoint {

    {
        // Initialize the default metrics for the hotspot VM
        DefaultExports.initialize();
    }

    @GET
    @Path("/metrics")
    @Produces(MediaType.TEXT_PLAIN)
    public StreamingOutput metrics() {

        return output -> {
            try (final Writer writer = new OutputStreamWriter(output)) {
                TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            }
        };
    }
}
