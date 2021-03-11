package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.function.Consumer;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.micrometer.runtime.binder.HttpBinderConfiguration;
import io.quarkus.micrometer.runtime.config.runtime.VertxConfig;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.VertxOptions;

@Recorder
public class VertxMeterBinderRecorder {
    /* STATIC_INIT */
    public Consumer<VertxOptions> configureMetricsAdapter() {
        return new Consumer<VertxOptions>() {
            @Override
            public void accept(VertxOptions vertxOptions) {
                VertxMeterBinderAdapter binder = getInstance(VertxMeterBinderAdapter.class);
                if (binder != null) {
                    vertxOptions.setMetricsOptions(binder);
                }
            }
        };
    }

    /* RUNTIME_INIT */
    public void setVertxConfig(VertxConfig config) {
        VertxMeterBinderAdapter binder = getInstance(VertxMeterBinderAdapter.class);
        HttpBinderConfiguration httpConfig = getInstance(HttpBinderConfiguration.class);
        if (binder != null && httpConfig != null) {
            binder.setVertxConfig(config, httpConfig);
        }
    }

    static <T> T getInstance(Class<T> clazz) {
        // Handle degenerate bad dev env: don't make it worse.
        ArcContainer container = Arc.container();
        if (container != null) {
            InstanceHandle<T> handle = Arc.container().instance(clazz);
            if (handle != null) {
                return handle.get();
            }
        }
        return null;
    }
}
