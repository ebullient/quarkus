package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import io.quarkus.micrometer.runtime.binder.RequestMetricInfo;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class HttpRequestMetric extends RequestMetricInfo {
    public static final Pattern VERTX_ROUTE_PARAM = Pattern.compile("^:(.*)$");

    /** Cache of vert.x resolved paths: /item/:id --> /item/{id} */
    final static ConcurrentHashMap<String, String> vertxWebToUriTemplate = new ConcurrentHashMap<>();

    protected HttpServerRequest request;
    protected String initialPath;
    protected String templatePath;
    protected RoutingContext routingContext;

    public HttpRequestMetric(String uri) {
        this.initialPath = uri;
    }

    public HttpRequestMetric(HttpServerRequest request) {
        this.request = request;
        this.initialPath = this.request.path();
    }

    public String getNormalizedUriPath(Map<Pattern, String> matchPatterns, List<Pattern> ignorePatterns) {
        return super.getNormalizedUriPath(matchPatterns, ignorePatterns, initialPath);
    }

    public String applyTemplateMatching(String path) {
        String currentRoutePath = getCurrentRoute();
        System.out.println("HERE, NOW WHAT: path=" + path + ", currentRoutePath=" + currentRoutePath);
        System.out.println(" ----:     routePath=" + currentRoutePath);

        // JAX-RS or Servlet container filter
        if (templatePath != null) {
            System.out.println(" ----: containerPath=" + templatePath);
            return normalizePath(templatePath);
        }

        // vertx-web or reactive route: is it templated?
        if (currentRoutePath != null && currentRoutePath.contains(":")) {
            System.out.println(" -: vertx template = " + currentRoutePath);
            // Convert /item/:id to /item/{id} and save it for next time
            return vertxWebToUriTemplate.computeIfAbsent(currentRoutePath, k -> {
                String segments[] = k.split("/");
                for (int i = 0; i < segments.length; i++) {
                    segments[i] = VERTX_ROUTE_PARAM.matcher(segments[i]).replaceAll("{$1}");
                }
                return normalizePath(String.join("/", segments));
            });
        }

        return path;
    }

    public HttpServerRequest request() {
        return request;
    }

    public void setTemplatePath(String path) {
        this.templatePath = path;
    }

    String getCurrentRoute() {
        return routingContext == null ? null : routingContext.currentRoute().getPath();
    }

    public static HttpRequestMetric getRequestMetric(RoutingContext context) {
        HttpRequestMetric metric = context.get(VertxHttpServerMetrics.METRICS_CONTEXT);
        metric.routingContext = context;
        return metric;
    }

    @Override
    public String toString() {
        return "HttpRequestMetric [initialPath=" + initialPath + ", currentRoutePath=" + getCurrentRoute()
                + ", templatePath=" + templatePath + ", request=" + request + "]";
    }
}
