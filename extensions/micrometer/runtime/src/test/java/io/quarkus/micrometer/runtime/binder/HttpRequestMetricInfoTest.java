package io.quarkus.micrometer.runtime.binder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import io.quarkus.micrometer.runtime.config.runtime.HttpClientConfig;
import io.quarkus.micrometer.runtime.config.runtime.HttpServerConfig;
import io.quarkus.micrometer.runtime.config.runtime.VertxConfig;

/**
 * Disabled on Java 8 because of Mocks
 */
@DisabledOnJre(JRE.JAVA_8)
public class HttpRequestMetricInfoTest {

    final List<Pattern> NO_IGNORE_PATTERNS = Collections.emptyList();
    final List<Pattern> ignorePatterns = Arrays.asList(Pattern.compile("/ignore.*"));

    final Map<Pattern, String> NO_MATCH_PATTERNS = Collections.emptyMap();

    @Test
    public void testHttpServerMetricsIgnorePatterns() {
        HttpServerConfig serverConfig = new HttpServerConfig();
        serverConfig.ignorePatterns = Optional.of(new ArrayList<>(Arrays.asList(" /item/.* ", " /oranges/.* ")));

        HttpBinderConfiguration binderConfig = new HttpBinderConfiguration(
                true, false, false,
                serverConfig, new HttpClientConfig(), new VertxConfig());

        Assertions.assertEquals(2, binderConfig.serverIgnorePatterns.size());

        Pattern p = binderConfig.serverIgnorePatterns.get(0);
        Assertions.assertEquals("/item/.*", p.pattern());
        Assertions.assertTrue(p.matcher("/item/123").matches());

        p = binderConfig.serverIgnorePatterns.get(1);
        Assertions.assertEquals("/oranges/.*", p.pattern());
        Assertions.assertTrue(p.matcher("/oranges/123").matches());
    }

    @Test
    public void testHttpServerMetricsMatchPatterns() {
        HttpServerConfig serverConfig = new HttpServerConfig();
        serverConfig.matchPatterns = Optional
                .of(new ArrayList<>(Arrays.asList(" /item/\\d+=/item/{id} ", "  /msg/\\d+=/msg/{other} ")));

        HttpBinderConfiguration binderConfig = new HttpBinderConfiguration(
                true, false, false,
                serverConfig, new HttpClientConfig(), new VertxConfig());

        Assertions.assertFalse(binderConfig.serverMatchPatterns.isEmpty());
        Iterator<Map.Entry<Pattern, String>> i = binderConfig.serverMatchPatterns.entrySet().iterator();
        Map.Entry<Pattern, String> entry = i.next();
        Assertions.assertEquals("/item/\\d+", entry.getKey().pattern());
        Assertions.assertEquals("/item/{id}", entry.getValue());
        Assertions.assertTrue(entry.getKey().matcher("/item/123").matches());

        entry = i.next();
        Assertions.assertEquals("/msg/\\d+", entry.getKey().pattern());
        Assertions.assertEquals("/msg/{other}", entry.getValue());
        Assertions.assertTrue(entry.getKey().matcher("/msg/789").matches());
    }

    @Test
    public void testParsePathDoubleSlash() {
        HttpRequestMetricInfo requestMetric = new MetricInfo(NO_MATCH_PATTERNS, NO_IGNORE_PATTERNS, "//");
        Assertions.assertEquals("/", requestMetric.path);
        Assertions.assertTrue(requestMetric.measure, "Path should be measured");
        Assertions.assertFalse(requestMetric.pathMatched, "Path should not be marked as matched");
    }

    @Test
    public void testParseEmptyPath() {
        HttpRequestMetricInfo requestMetric = new MetricInfo(NO_MATCH_PATTERNS, NO_IGNORE_PATTERNS, "");
        Assertions.assertEquals("/", requestMetric.path);
        Assertions.assertTrue(requestMetric.measure, "Path should be measured");
        Assertions.assertFalse(requestMetric.pathMatched, "Path should not be marked as matched");
    }

    @Test
    public void testParsePathNoLeadingSlash() {
        HttpRequestMetricInfo requestMetric = new MetricInfo(NO_MATCH_PATTERNS, NO_IGNORE_PATTERNS,
                "path/with/no/leading/slash");
        Assertions.assertEquals("/path/with/no/leading/slash", requestMetric.path);
        Assertions.assertTrue(requestMetric.measure, "Path should be measured");
        Assertions.assertFalse(requestMetric.pathMatched, "Path should not be marked as matched");
    }

    @Test
    public void testParsePathWithQueryString() {
        HttpRequestMetricInfo requestMetric = new MetricInfo(NO_MATCH_PATTERNS, NO_IGNORE_PATTERNS,
                "/path/with/query/string?stuff");
        Assertions.assertEquals("/path/with/query/string", requestMetric.path);
        Assertions.assertTrue(requestMetric.measure, "Path should be measured");
        Assertions.assertFalse(requestMetric.pathMatched, "Path should not be marked as matched");
    }

    @Test
    public void testParsePathIgnoreNoLeadingSlash() {
        HttpRequestMetricInfo requestMetric = new MetricInfo(NO_MATCH_PATTERNS, ignorePatterns,
                "ignore/me/with/no/leading/slash");
        Assertions.assertEquals("/ignore/me/with/no/leading/slash", requestMetric.path);
        Assertions.assertFalse(requestMetric.measure, "Path should be measured");
        Assertions.assertFalse(requestMetric.pathMatched, "Path should not be marked as matched");
    }

    @Test
    public void testParsePathIgnoreWithQueryString() {
        HttpRequestMetricInfo requestMetric = new MetricInfo(NO_MATCH_PATTERNS, ignorePatterns,
                "/ignore/me/with/query/string?stuff");
        Assertions.assertEquals("/ignore/me/with/query/string", requestMetric.path);
        Assertions.assertFalse(requestMetric.measure, "Path should be measured");
        Assertions.assertFalse(requestMetric.pathMatched, "Path should not be marked as matched");
    }

    @Test
    public void testParsePathMatchReplaceNoLeadingSlash() {
        final Map<Pattern, String> matchPatterns = new HashMap<>();
        matchPatterns.put(Pattern.compile("/item/\\d+"), "/item/{id}");

        HttpRequestMetricInfo requestMetric = new MetricInfo(matchPatterns, NO_IGNORE_PATTERNS, "item/123");
        Assertions.assertEquals("/item/{id}", requestMetric.path);
        Assertions.assertTrue(requestMetric.measure, "Path should be measured");
        Assertions.assertTrue(requestMetric.pathMatched, "Path should be marked as matched");
    }

    @Test
    public void testParsePathMatchReplaceLeadingSlash() {
        final Map<Pattern, String> matchPatterns = new HashMap<>();
        matchPatterns.put(Pattern.compile("/item/\\d+"), "/item/{id}");

        HttpRequestMetricInfo requestMetric = new MetricInfo(matchPatterns, NO_IGNORE_PATTERNS, "/item/123");
        Assertions.assertEquals("/item/{id}", requestMetric.path);
        Assertions.assertTrue(requestMetric.measure, "Path should be measured");
        Assertions.assertTrue(requestMetric.pathMatched, "Path should be marked as matched");
    }

    class MetricInfo extends HttpRequestMetricInfo {

        /**
         * Extract the path out of the uri. Return null if the path should be
         * ignored.
         *
         * @param matchPattern
         * @param ignorePatterns
         * @param uri
         */
        public MetricInfo(Map<Pattern, String> matchPattern, List<Pattern> ignorePatterns, String uri) {
            super(matchPattern, ignorePatterns, uri);
        }

        @Override
        public String getHttpRequestPath() {
            return null;
        }
    }
}
