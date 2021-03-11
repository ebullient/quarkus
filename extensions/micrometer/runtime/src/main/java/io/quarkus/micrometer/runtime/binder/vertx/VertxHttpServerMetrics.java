package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.Outcome;
import io.quarkus.micrometer.runtime.binder.HttpBinderConfiguration;
import io.quarkus.micrometer.runtime.binder.HttpMeterFilterProvider;
import io.quarkus.micrometer.runtime.binder.HttpMetricsCommon;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.spi.metrics.HttpServerMetrics;

/**
 * HttpServerMetrics<R, W, S>
 * <ul>
 * <li>R for Request metric -- RequestMetricContext</li>
 * <li>W for Websocket metric -- LongTaskTimer sample</li>
 * <li>S for Socket metric -- Map<String, Object></li>
 * </ul>
 */
public class VertxHttpServerMetrics extends VertxTcpMetrics
        implements HttpServerMetrics<VertxServerRequestMetricInfo, LongTaskTimer.Sample, Map<String, Object>> {
    static final Logger log = Logger.getLogger(VertxHttpServerMetrics.class);
    static final String METRICS_CONTEXT = "HTTP_REQUEST_METRICS_CONTEXT";

    static final String HTTP_SERVER_WEBSOCKET_CONNECTIONS = HttpMeterFilterProvider.HTTP_SERVER_WEBSOCKET_CONNECTIONS;
    static final String HTTP_SERVER_PUSH = HttpMeterFilterProvider.HTTP_SERVER_PUSH;
    static final String HTTP_SERVER_REQUESTS = HttpMeterFilterProvider.HTTP_SERVER_REQUESTS;

    final List<Pattern> ignorePatterns;
    final Map<Pattern, String> matchPatterns;

    VertxHttpServerMetrics(MeterRegistry registry, HttpBinderConfiguration config) {
        super(registry, "http.server");

        ignorePatterns = config.getServerIgnorePatterns();
        matchPatterns = config.getServerMatchPatterns();
    }

    /**
     * Stash the RequestMetric in the Vertx Context
     *
     * @param context Vertx context to store RequestMetric in
     * @param requestMetric
     * @see VertxMeterFilter
     */
    public static void setRequestMetric(Context context, VertxServerRequestMetricInfo requestMetric) {
        if (context != null) {
            context.put(METRICS_CONTEXT, requestMetric);
        }
    }

    /**
     * Retrieve and remove the RequestMetric from the Vertx Context
     *
     * @param context
     * @return the RequestMetricContext stored in the Vertx Context, or null
     * @see VertxMeterFilter
     */
    public static VertxServerRequestMetricInfo retrieveRequestMetric(Context context) {
        if (context != null) {
            VertxServerRequestMetricInfo requestMetric = context.get(METRICS_CONTEXT);
            context.remove(METRICS_CONTEXT);
            return requestMetric;
        }
        return null;
    }

    /**
     * Called when an http server response is pushed.
     *
     * @param socketMetric a Map for socket metric context or null
     * @param method the pushed response method
     * @param uri the pushed response uri
     * @param response the http server response
     * @return a RequestMetricContext
     */
    @Override
    public VertxServerRequestMetricInfo responsePushed(Map<String, Object> socketMetric, HttpMethod method, String uri,
            HttpServerResponse response) {
        VertxServerRequestMetricInfo requestMetric = new VertxServerRequestMetricInfo(matchPatterns, ignorePatterns, uri);
        if (requestMetric.isMeasure()) {
            registry.counter(HTTP_SERVER_PUSH, Tags.of(
                    HttpMetricsCommon.uri(requestMetric.getPath(), response.getStatusCode()),
                    VertxMetricsTags.method(method),
                    VertxMetricsTags.outcome(response),
                    HttpMetricsCommon.status(response.getStatusCode())))
                    .increment();
        }
        log.debugf("responsePushed %s: %s, %s", uri, socketMetric, requestMetric);
        return requestMetric;
    }

    /**
     * Called when an http server request begins. Vert.x will invoke
     * {@link #responseEnd} when the response has ended or {@link #requestReset} if
     * the request/response has failed before.
     *
     * @param socketMetric a Map for socket metric context or null
     * @param request the http server request
     * @return a RequestMetricContext
     */
    @Override
    public VertxServerRequestMetricInfo requestBegin(Map<String, Object> socketMetric, HttpServerRequest request) {
        // evaluate and remember the path to monitor for use later (maybe a 404 or redirect..)
        VertxServerRequestMetricInfo requestMetric = new VertxServerRequestMetricInfo(matchPatterns, ignorePatterns,
                request.path());
        setRequestMetric(Vertx.currentContext(), requestMetric);

        if (requestMetric.isMeasure()) {
            // If we're measuring this request, create/remember the sample
            requestMetric.setSample(Timer.start(registry));
            requestMetric.setTags(Tags.of(VertxMetricsTags.method(request.method())));

            log.debugf("requestBegin %s: %s, %s", requestMetric.getPath(), socketMetric, requestMetric);
        }

        return requestMetric;
    }

    /**
     * Called when the http server request couldn't complete successfully, for
     * instance the connection was closed before the response was sent.
     *
     * @param requestMetric a RequestMetricContext or null
     */
    @Override
    public void requestReset(VertxServerRequestMetricInfo requestMetric) {
        log.debugf("requestReset: %s", requestMetric);
        Timer.Sample sample = getRequestSample(requestMetric);
        if (sample != null) {
            String requestPath = getServerRequestPath(requestMetric);
            Timer.Builder builder = Timer.builder(HTTP_SERVER_REQUESTS)
                    .tags(requestMetric.getTags())
                    .tags(Tags.of(
                            HttpMetricsCommon.uri(requestPath, 0),
                            Outcome.CLIENT_ERROR.asTag(),
                            HttpMetricsCommon.STATUS_RESET));
            sample.stop(builder.register(registry));
        }
    }

    /**
     * Called when an http server response has ended.
     *
     * @param requestMetric a RequestMetricContext or null
     * @param response the http server response
     */
    @Override
    public void responseEnd(VertxServerRequestMetricInfo requestMetric, HttpServerResponse response) {
        log.debugf("responseEnd: %s, %s", requestMetric, response);

        Timer.Sample sample = getRequestSample(requestMetric);
        if (sample != null) {
            String requestPath = getServerRequestPath(requestMetric);
            Timer.Builder builder = Timer.builder(HTTP_SERVER_REQUESTS)
                    .tags(requestMetric.getTags())
                    .tags(Tags.of(
                            HttpMetricsCommon.uri(requestPath, response.getStatusCode()),
                            VertxMetricsTags.outcome(response),
                            HttpMetricsCommon.status(response.getStatusCode())));

            sample.stop(builder.register(registry));
        }
    }

    /**
     * Called when a server web socket connects.
     *
     * @param socketMetric a Map for socket metric context or null
     * @param requestMetric a RequestMetricContext or null
     * @param serverWebSocket the server web socket
     * @return a LongTaskTimer.Sample or null
     */
    @Override
    public LongTaskTimer.Sample connected(Map<String, Object> socketMetric, VertxServerRequestMetricInfo requestMetric,
            ServerWebSocket serverWebSocket) {
        log.debugf("websocket connected: %s, %s, %s", socketMetric, requestMetric, serverWebSocket);
        String path = getServerRequestPath(requestMetric);
        if (path != null) {
            return LongTaskTimer.builder(HTTP_SERVER_WEBSOCKET_CONNECTIONS)
                    .tags(Tags.of(HttpMetricsCommon.uri(path, 0)))
                    .register(registry)
                    .start();
        }
        return null;
    }

    /**
     * Called when the server web socket has disconnected.
     *
     * @param websocketMetric a LongTaskTimer.Sample or null
     */
    @Override
    public void disconnected(LongTaskTimer.Sample websocketMetric) {
        log.debugf("websocket disconnected: %s", websocketMetric);
        if (websocketMetric != null) {
            websocketMetric.stop();
        }
    }

    private Timer.Sample getRequestSample(VertxServerRequestMetricInfo metricsContext) {
        if (metricsContext == null) {
            return null;
        }
        return metricsContext.getSample();
    }

    private String getServerRequestPath(VertxServerRequestMetricInfo metricsContext) {
        if (metricsContext == null) {
            return null;
        }
        return metricsContext.getHttpRequestPath();
    }
}
