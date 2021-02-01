package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;
import io.quarkus.micrometer.runtime.config.HttpServerFixedConfig;
import io.quarkus.micrometer.runtime.config.runtime.HttpServerConfig;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.VertxOptions;

@Recorder
public class VertxMeterBinderRecorder {
    private static final Logger log = Logger.getLogger(VertxMeterBinderRecorder.class);

    /* STATIC_INIT */
    public Consumer<VertxOptions> configureMetricsAdapter() {
        return new Consumer<VertxOptions>() {
            @Override
            public void accept(VertxOptions vertxOptions) {
                log.debug("Adding Micrometer MeterBinder to VertxOptions");
                VertxMeterBinderAdapter binder = Arc.container().instance(VertxMeterBinderAdapter.class).get();
                vertxOptions.setMetricsOptions(binder);
            }
        };
    }

    /* RUNTIME_INIT */
    public void setVertxConfig(HttpServerConfig config) {
        // Look for old config values
        vertxHttpServerConfig(config);

        VertxMeterBinderAdapter binder = Arc.container().instance(VertxMeterBinderAdapter.class).get();
        binder.setVertxConfig(config);
    }

    @Deprecated
    protected void vertxHttpServerConfig(HttpServerConfig config) {
        Config generalConfig = ConfigProvider.getConfig();
        if (!config.ignorePatterns.isPresent()) {
            Optional<String[]> vertxIgnore = generalConfig.getOptionalValue(
                    HttpServerFixedConfig.VERTX_BINDER_IGNORE_PATTERNS_PROPERTY,
                    String[].class);
            if (vertxIgnore.isPresent()) {
                config.ignorePatterns = Optional.of(Arrays.asList(vertxIgnore.get()));
            }
        }
        if (!config.matchPatterns.isPresent()) {
            Optional<String[]> vertxMatch = generalConfig.getOptionalValue(
                    HttpServerFixedConfig.VERTX_BINDER_MATCH_PATTERNS_PROPERTY,
                    String[].class);
            if (vertxMatch.isPresent()) {
                config.ignorePatterns = Optional.of(Arrays.asList(vertxMatch.get()));
            }
        }
    }
}
