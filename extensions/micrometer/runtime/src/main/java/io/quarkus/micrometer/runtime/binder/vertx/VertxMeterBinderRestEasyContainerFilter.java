package io.quarkus.micrometer.runtime.binder.vertx;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.jboss.logging.Logger;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.interception.jaxrs.PostMatchContainerRequestContext;

import io.quarkus.micrometer.runtime.binder.HttpRequestMetric;
import io.quarkus.resteasy.common.runtime.MethodFullPathIndex;
import io.vertx.ext.web.RoutingContext;

public class VertxMeterBinderRestEasyContainerFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(VertxMeterBinderRestEasyContainerFilter.class);

    @Inject
    MethodFullPathIndex fullPathIndex;

    @Inject
    RoutingContext routingContext;

    @Override
    public void filter(final ContainerRequestContext crc) {
        // bail early if we have no routing context, or
        // if path munging isn't necessary (see VertxMeterFilter)
        if (routingContext == null || routingContext.get(HttpRequestMetric.HTTP_REQUEST_PATH_MATCHED) != null) {
            return;
        }

        ResourceMethodInvoker invoker = ((PostMatchContainerRequestContext) crc).getResourceMethod();
        // Lookup the templated full path for the method
        String path = fullPathIndex.getFullPath(invoker.getMethod());
        routingContext.put(HttpRequestMetric.HTTP_REQUEST_PATH, path);
    }
}
