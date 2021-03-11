package io.quarkus.micrometer.runtime.binder.vertx;

import java.io.IOException;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.quarkus.micrometer.runtime.binder.HttpRequestMetric;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * This needs to run before the Vert.x layer sees the "end" of the response.
 * HttpFilter meets that requirement.
 * Filter does not.
 */
public class VertxMeterBinderUndertowServletFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        try {
            chain.doFilter(req, res);
        } finally {
            RoutingContext routingContext = CDI.current().select(CurrentVertxRequest.class).get().getCurrent();
            if (routingContext != null
                    && routingContext.get(HttpRequestMetric.HTTP_REQUEST_PATH_MATCHED) == null
                    && routingContext.get(HttpRequestMetric.HTTP_REQUEST_PATH) == null) {

                routingContext.put(HttpRequestMetric.HTTP_REQUEST_PATH, req.getServletPath());
            }
        }
    }
}
