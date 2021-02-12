package io.quarkus.micrometer.runtime.binder;

import javax.inject.Singleton;
import javax.ws.rs.Produces;

import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.internal.OnlyOnceLoggingDenyMeterFilter;
import io.quarkus.micrometer.runtime.config.runtime.HttpClientConfig;
import io.quarkus.micrometer.runtime.config.runtime.HttpServerConfig;

@Singleton
public class HttpMeterFilterProvider {
    public static final String HTTP_CLIENT_REQUESTS_NAME = "http.client.requests";
    public static final String HTTP_SERVER_REQUESTS_NAME = "http.server.requests";
    public static final String HTTP_SERVER_PUSH_NAME = "http.server.push";
    public static final String HTTP_SERVER_WEBSOCKETS_NAME = "http.server.websocket.connections";

    @Singleton
    @Produces
    public MeterFilter metricsHttpClientUriTagFilter(HttpClientConfig httpClientConfig) {
        return maximumAllowableUriTagsFilter(HTTP_CLIENT_REQUESTS_NAME, httpClientConfig.maxUriTags);
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpServerUriTagFilter(HttpServerConfig httpServerConfig) {
        return maximumAllowableUriTagsFilter(HTTP_SERVER_REQUESTS_NAME, httpServerConfig.maxUriTags);
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpPushUriTagFilter(HttpServerConfig httpServerConfig) {
        return maximumAllowableUriTagsFilter(HTTP_SERVER_PUSH_NAME, httpServerConfig.maxUriTags);
    }

    @Singleton
    @Produces
    public MeterFilter metricsHttpWebSocketsUriTagFilter(HttpServerConfig httpServerConfig) {
        return maximumAllowableUriTagsFilter(HTTP_SERVER_WEBSOCKETS_NAME, httpServerConfig.maxUriTags);
    }

    MeterFilter maximumAllowableUriTagsFilter(final String metricName, final int maximumTagValues) {
        MeterFilter denyFilter = new OnlyOnceLoggingDenyMeterFilter(() -> String
                .format("Reached the maximum number (%s) of URI tags for '%s'. Are you using path parameters?",
                        maximumTagValues, metricName));

        return MeterFilter.maximumAllowableTags(metricName, "uri", maximumTagValues,
                denyFilter);
    }
}
