package io.quarkus.micrometer.runtime.binder;

import javax.inject.Singleton;
import javax.ws.rs.Produces;

import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.internal.OnlyOnceLoggingDenyMeterFilter;
import io.quarkus.micrometer.runtime.config.runtime.HttpClientConfig;
import io.quarkus.micrometer.runtime.config.runtime.HttpServerConfig;

@Singleton
public class HttpMeterFilterProvider {
    public static final String HTTP_CLIENT_REQUESTS = "http.client.requests";
    public static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    public static final String HTTP_SERVER_PUSH = "http.server.push";
    public static final String HTTP_SERVER_WEBSOCKET_CONNECTIONS = "http.server.websocket.connections";

    HttpBinderConfiguration binderConfiguration;

    HttpMeterFilterProvider(HttpBinderConfiguration binderConfiguration) {
        this.binderConfiguration = binderConfiguration;
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpClientUriTagFilter(HttpClientConfig httpClientConfig) {
        if (binderConfiguration.isClientEnabled()) {
            return maximumAllowableUriTagsFilter(HTTP_CLIENT_REQUESTS, httpClientConfig.maxUriTags);
        }
        return null;
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpServerUriTagFilter(HttpServerConfig httpServerConfig) {
        if (binderConfiguration.isServerEnabled()) {
            return maximumAllowableUriTagsFilter(HTTP_SERVER_REQUESTS, httpServerConfig.maxUriTags);
        }
        return null;
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpPushUriTagFilter(HttpServerConfig httpServerConfig) {
        if (binderConfiguration.isServerEnabled()) {
            return maximumAllowableUriTagsFilter(HTTP_SERVER_PUSH, httpServerConfig.maxUriTags);
        }
        return null;
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpWebSocketsUriTagFilter(HttpServerConfig httpServerConfig) {
        if (binderConfiguration.isServerEnabled()) {
            return maximumAllowableUriTagsFilter(HTTP_SERVER_WEBSOCKET_CONNECTIONS, httpServerConfig.maxUriTags);
        }
        return null;
    }

    MeterFilter maximumAllowableUriTagsFilter(final String metricName, final int maximumTagValues) {
        MeterFilter denyFilter = new OnlyOnceLoggingDenyMeterFilter(() -> String
                .format("Reached the maximum number (%s) of URI tags for '%s'. Are you using path parameters?",
                        maximumTagValues, metricName));

        return MeterFilter.maximumAllowableTags(metricName, "uri", maximumTagValues,
                denyFilter);
    }
}
