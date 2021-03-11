package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import io.quarkus.micrometer.runtime.binder.HttpRequestMetricInfo;
import io.vertx.ext.web.RoutingContext;

public class VertxServerRequestMetricInfo extends HttpRequestMetricInfo {

    /** Cache of vert.x resolved paths: /item/:id --> /item/{id} */
    final static ConcurrentHashMap<String, String> templatePath = new ConcurrentHashMap<>();

    volatile RoutingContext routingContext;

    /**
     * Extract the path out of the uri. Return null if the path should be
     * ignored.
     *
     * @param matchPattern
     * @param ignorePatterns
     * @param uri
     */
    public VertxServerRequestMetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns, String uri) {
        super(matchPattern, ignorePatterns, uri);
    }

    public String getHttpRequestPath() {
        // Vertx binder configuration, see VertxMetricsTags
        if (super.pathMatched) {
            return path;
        }
        if (routingContext != null) {
            // JAX-RS or Servlet container filter
            String rcPath = routingContext.get(HTTP_REQUEST_PATH);
            if (rcPath != null) {
                return rcPath;
            }

            String matchedPath = routingContext.currentRoute().getPath();
            if (matchedPath != null) {
                // vertx-web or reactive route: is it templated?
                if (matchedPath.contains(":")) {
                    // Convert /item/:id to /item/{id} and save it for next time
                    return templatePath.computeIfAbsent(matchedPath, k -> {
                        String segments[] = k.split("/");
                        for (int i = 0; i < segments.length; i++) {
                            segments[i] = VERTX_ROUTE_PARAM.matcher(segments[i]).replaceAll("{$1}");
                        }
                        return String.join("/", segments);
                    });
                } else {
                    // With no template vars to deal with, use the normalized version of the path
                    return routingContext.normalisedPath();
                }
            }
        }
        return path;
    }

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    public void setRoutingContext(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

}
