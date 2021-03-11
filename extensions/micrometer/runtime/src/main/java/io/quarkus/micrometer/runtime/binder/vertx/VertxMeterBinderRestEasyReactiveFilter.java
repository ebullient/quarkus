package io.quarkus.micrometer.runtime.binder.vertx;

import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.SimpleResourceInfo;

import io.quarkus.micrometer.runtime.binder.HttpRequestMetric;
import io.vertx.ext.web.RoutingContext;

public class VertxMeterBinderRestEasyReactiveFilter {

    @ServerRequestFilter
    public void filter(SimpleResourceInfo simpleResourceInfo, RoutingContext routingContext) {
        if (routingContext == null || routingContext.get(HttpRequestMetric.HTTP_REQUEST_PATH_MATCHED) != null) {
            return;
        }
        routingContext.put(HttpRequestMetric.HTTP_REQUEST_PATH, simpleResourceInfo.getTemplateUriPath());
    }
}
