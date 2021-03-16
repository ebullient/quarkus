package io.quarkus.micrometer.runtime.binder.vertx;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.interception.jaxrs.PostMatchContainerRequestContext;

import io.vertx.ext.web.RoutingContext;

public class VertxMeterBinderRestEasyContainerFilter implements ContainerRequestFilter {

    @Inject
    RoutingContext routingContext;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        HttpRequestMetric metric = HttpRequestMetric.getRequestMetric(routingContext);
        System.out.println("RESTEASY FILTER: " + metric + ", " + routingContext);

        ResourceMethodInvoker invoker = ((PostMatchContainerRequestContext) requestContext).getResourceMethod();
        // Lookup the templated full path for the method
        //        String path = fullPathIndex.getFullPath(invoker.getMethod());
        //        routingContext.put(RequestMetricInfo.HTTP_REQUEST_PATH, path);
    }
}
