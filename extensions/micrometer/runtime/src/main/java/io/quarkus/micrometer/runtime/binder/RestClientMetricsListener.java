package io.quarkus.micrometer.runtime.binder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.quarkus.arc.Arc;

/**
 * This is initialized via ServiceFactory (static/non-CDI initialization)
 */
public class RestClientMetricsListener implements RestClientListener {

    private final static String REQUEST_METRIC_PROPERTY = "restClientMetrics";

    final MeterRegistry registry = Metrics.globalRegistry;
    boolean initialized = false;
    boolean clientMetricsEnabled = true;

    MetricsClientRequestFilter clientRequestFilter;
    MetricsClientResponseFilter clientResponseFilter;
    RestClientMetricInfoFactory restClientMetricInfoFactory;

    @Override
    public void onNewClient(Class<?> serviceInterface, RestClientBuilder builder) {
        if (prepClientMetrics()) {
            builder.register(this.clientRequestFilter);
            builder.register(this.clientResponseFilter);
        }
    }

    boolean prepClientMetrics() {
        boolean doInit = !this.initialized;
        boolean clientMetricsEnabled = this.clientMetricsEnabled;
        if (doInit) {
            HttpBinderConfiguration httpMetricsConfig = Arc.container().instance(HttpBinderConfiguration.class).get();
            clientMetricsEnabled = this.clientMetricsEnabled = httpMetricsConfig.isClientEnabled();
            if (clientMetricsEnabled) {
                this.clientRequestFilter = new MetricsClientRequestFilter(httpMetricsConfig);
                this.clientResponseFilter = new MetricsClientResponseFilter();

                // Isolate the processing of path information, as it relies on different mechanisms
                if (httpMetricsConfig.isResteasyReactive()) {
                    restClientMetricInfoFactory = new RestEasyReactiveClientRequestMetricInfoFactory();
                } else {
                    restClientMetricInfoFactory = new RestEasyClientRequestMetricInfoFactory();
                }
            }
            this.initialized = true;
        }
        return clientMetricsEnabled;
    }

    class MetricsClientRequestFilter implements ClientRequestFilter {
        HttpBinderConfiguration binderConfiguration;

        MetricsClientRequestFilter(HttpBinderConfiguration binderConfiguration) {
            this.binderConfiguration = binderConfiguration;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            HttpRequestMetricInfo requestMetric = restClientMetricInfoFactory.getHttpRequestMetricInfo(
                    binderConfiguration.getClientMatchPatterns(),
                    binderConfiguration.getClientIgnorePatterns(),
                    requestContext);

            if (requestMetric.isMeasure()) {
                requestMetric.setSample(Timer.start(registry));
                requestContext.setProperty(REQUEST_METRIC_PROPERTY, requestMetric);
            }
        }
    }

    class MetricsClientResponseFilter implements ClientResponseFilter {
        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            HttpRequestMetricInfo requestMetric = getRequestMetric(requestContext);

            if (requestMetric != null) {
                Timer.Sample sample = requestMetric.sample;
                String requestPath = requestMetric.getHttpRequestPath();
                int statusCode = responseContext.getStatus();
                Timer.Builder builder = Timer.builder(HttpMeterFilterProvider.HTTP_CLIENT_REQUESTS)
                        .tags(Tags.of(
                                HttpMetricsCommon.method(requestContext.getMethod()),
                                HttpMetricsCommon.uri(requestPath, statusCode),
                                HttpMetricsCommon.outcome(statusCode),
                                HttpMetricsCommon.status(statusCode),
                                clientName(requestContext)));

                sample.stop(builder.register(registry));
            }
        }

        private HttpRequestMetricInfo getRequestMetric(ClientRequestContext requestContext) {
            return (HttpRequestMetricInfo) requestContext.getProperty(REQUEST_METRIC_PROPERTY);
        }

        private Tag clientName(ClientRequestContext requestContext) {
            String host = requestContext.getUri().getHost();
            if (host == null) {
                host = "none";
            }
            return Tag.of("clientName", host);
        }
    }

    public static interface RestClientMetricInfoFactory {
        HttpRequestMetricInfo getHttpRequestMetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns,
                ClientRequestContext requestContext);
    }
}
