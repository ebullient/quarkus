package io.quarkus.micrometer.runtime.binder.vertx;

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
    protected String currentRoutePath;

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

    public String initialPath() {
        return initialPath;
    }

    public void setTemplatePath(String path) {
        this.templatePath = path;
    }

    public void appendCurrentRoutePath(String path) {
        if (path != null && !path.isEmpty()) {
            this.currentRoutePath = path;
        }
    }

    public void getRoutingContext(RoutingContext context) {
        appendCurrentRoutePath(context.currentRoute().getPath());
    }

    @Override
    public String toString() {
        return "HttpRequestMetric [initialPath=" + initialPath + ", currentRoutePath=" + currentRoutePath
                + ", templatePath=" + templatePath + ", request=" + request + "]";
    }
}
