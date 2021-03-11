package io.quarkus.micrometer.runtime.binder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.client.ClientRequestContext;

public class RestEasyReactiveClientRequestMetricInfoFactory implements RestClientMetricsListener.RestClientMetricInfoFactory {
    @Override
    public HttpRequestMetricInfo getHttpRequestMetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns,
            ClientRequestContext requestContext) {
        return new RestEasyReactiveClientRequestMetricInfo(matchPattern, ignorePatterns, requestContext);
    }

    class RestEasyReactiveClientRequestMetricInfo extends HttpRequestMetricInfo {
        ClientRequestContext requestContext;

        public RestEasyReactiveClientRequestMetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns,
                ClientRequestContext requestContext) {
            super(matchPattern, ignorePatterns, requestContext.getUri().getPath());
            this.requestContext = requestContext;
        }

        @Override
        public String getHttpRequestPath() {

            return "/";
        }
    }
}
