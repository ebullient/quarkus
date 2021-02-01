package io.quarkus.micrometer.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * Build / static runtime config for inbound HTTP traffic
 */
@ConfigGroup
public class HttpServerFixedConfig implements MicrometerConfig.CapabilityEnabled {

    /** @deprecated in favor of "quarkus.micrometer.binder.http-server.enabled" */
    public static final String VERTX_BINDER_ENABLED_PROPERTY = "quarkus.micrometer.binder.vertx.enabled";

    /** @deprecated in favor of "quarkus.micrometer.binder.http-server.ignore-patterns" */
    public static final String VERTX_BINDER_IGNORE_PATTERNS_PROPERTY = "quarkus.micrometer.binder.vertx.ignore-patterns";

    /** @deprecated in favor of "quarkus.micrometer.binder.http-server.match-patterns" */
    public static final String VERTX_BINDER_MATCH_PATTERNS_PROPERTY = "quarkus.micrometer.binder.vertx.match-patterns";

    /**
     * Inbound HTTP server metrics support.
     * <p>
     * Support for Http Server metrics will be enabled if Micrometer
     * support is enabled, an extension serving HTTP requests is present
     * and either this value is true, or this value is unset and
     * {@code quarkus.micrometer.binder-enabled-default is true.
     */
    @ConfigItem
    public Optional<Boolean> enabled;

    @Override
    public Optional<Boolean> getEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{enabled=" + enabled
                + '}';
    }
}
