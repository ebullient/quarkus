package io.quarkus.micrometer.runtime.binder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.client.ClientRequestContext;

import org.jboss.resteasy.microprofile.client.utils.ClientRequestContextUtils;

import io.quarkus.arc.Arc;
import io.quarkus.resteasy.common.runtime.MethodFullPathIndex;

public class RestEasyClientRequestMetricInfoFactory implements RestClientMetricsListener.RestClientMetricInfoFactory {

    // Created by the Quarkus RestEasy extension:
    // stores a mapping between a Method, and the full path used to reach it
    MethodFullPathIndex methodFullPathIndex;

    RestEasyClientRequestMetricInfoFactory() {
        this.methodFullPathIndex = Arc.container().instance(MethodFullPathIndex.class).get();
    }

    @Override
    public HttpRequestMetricInfo getHttpRequestMetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns,
            ClientRequestContext requestContext) {
        return new RestEasyClientRequestMetricInfo(matchPattern, ignorePatterns, requestContext);
    }

    class RestEasyClientRequestMetricInfo extends HttpRequestMetricInfo {
        ClientRequestContext requestContext;

        public RestEasyClientRequestMetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns,
                ClientRequestContext requestContext) {
            super(matchPattern, ignorePatterns, requestContext.getUri().getPath());
            this.requestContext = requestContext;
        }

        @Override
        public String getHttpRequestPath() {
            // find and then cache/store the path annotation for this class
            Method method = ClientRequestContextUtils.getMethod(requestContext);
            String path = methodFullPathIndex.getFullPath(method);
            if (path == null) {
                Path p = method.getAnnotation(Path.class);
                if (p != null) {
                    path = p.value();
                } else {
                    path = requestContext.getUri().getPath();
                }
                path = methodFullPathIndex.registerFullPath(method, path);
            }
            return path;
        }
    }
}
