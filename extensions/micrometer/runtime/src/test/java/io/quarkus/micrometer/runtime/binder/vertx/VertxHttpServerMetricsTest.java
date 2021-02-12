package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkus.micrometer.runtime.binder.HttpBinderConfiguration;
import io.quarkus.micrometer.runtime.config.runtime.HttpClientConfig;
import io.quarkus.micrometer.runtime.config.runtime.HttpServerConfig;
import io.quarkus.micrometer.runtime.config.runtime.VertxConfig;

public class VertxHttpServerMetricsTest {

    @Test
    public void testHttpServerMetricsIgnorePatterns() {
        HttpServerConfig serverConfig = new HttpServerConfig();
        serverConfig.ignorePatterns = Optional.of(new ArrayList<>(Arrays.asList(" /item/.* ", " /oranges/.* ")));

        HttpBinderConfiguration binderConfig = new HttpBinderConfiguration(
                true, false,
                serverConfig, new HttpClientConfig(), new VertxConfig());

        VertxHttpServerMetrics metrics = new VertxHttpServerMetrics(new SimpleMeterRegistry(), binderConfig);
        Assertions.assertEquals(2, metrics.ignorePatterns.size());

        Pattern p = metrics.ignorePatterns.get(0);
        Assertions.assertEquals("/item/.*", p.pattern());
        Assertions.assertTrue(p.matcher("/item/123").matches());

        p = metrics.ignorePatterns.get(1);
        Assertions.assertEquals("/oranges/.*", p.pattern());
        Assertions.assertTrue(p.matcher("/oranges/123").matches());
    }

    @Test
    public void testHttpServerMetricsMatchPatterns() {
        HttpServerConfig serverConfig = new HttpServerConfig();
        serverConfig.matchPatterns = Optional
                .of(new ArrayList<>(Arrays.asList(" /item/\\d+=/item/{id} ", "  /msg/\\d+=/msg/{other} ")));

        HttpBinderConfiguration binderConfig = new HttpBinderConfiguration(
                true, false,
                serverConfig, new HttpClientConfig(), new VertxConfig());

        VertxHttpServerMetrics metrics = new VertxHttpServerMetrics(new SimpleMeterRegistry(), binderConfig);

        Assertions.assertFalse(metrics.matchPatterns.isEmpty());
        Iterator<Map.Entry<Pattern, String>> i = metrics.matchPatterns.entrySet().iterator();
        Map.Entry<Pattern, String> entry = i.next();
        Assertions.assertEquals("/item/\\d+", entry.getKey().pattern());
        Assertions.assertEquals("/item/{id}", entry.getValue());
        Assertions.assertTrue(entry.getKey().matcher("/item/123").matches());

        entry = i.next();
        Assertions.assertEquals("/msg/\\d+", entry.getKey().pattern());
        Assertions.assertEquals("/msg/{other}", entry.getValue());
        Assertions.assertTrue(entry.getKey().matcher("/msg/789").matches());
    }
}
