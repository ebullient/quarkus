package io.quarkus.micrometer.deployment.binder;

import java.util.function.BooleanSupplier;

import javax.interceptor.Interceptor;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.micrometer.deployment.MicrometerProcessor;
import io.quarkus.micrometer.runtime.MicrometerRecorder;
import io.quarkus.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter;
import io.quarkus.micrometer.runtime.binder.vertx.VertxMeterBinderRecorder;
import io.quarkus.micrometer.runtime.binder.vertx.VertxMeterFilter;
import io.quarkus.micrometer.runtime.config.HttpServerFixedConfig;
import io.quarkus.micrometer.runtime.config.MicrometerConfig;
import io.quarkus.micrometer.runtime.config.runtime.HttpServerConfig;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.reactive.spi.CustomContainerRequestFilterBuildItem;
import io.quarkus.vertx.core.deployment.VertxOptionsConsumerBuildItem;
import io.quarkus.vertx.http.deployment.FilterBuildItem;

/**
 * Add support for Vert.x and other http instrumentation.
 * Note that various bits of support may not be present at deploy time,
 * e.g. Vert.x can be present while resteasy is not.
 * 
 * Avoid referencing classes that in turn import optional dependencies.
 */
public class VertxBinderProcessor {
    static final String METRIC_OPTIONS_CLASS_NAME = "io.vertx.core.metrics.MetricsOptions";
    static final Class<?> METRIC_OPTIONS_CLASS = MicrometerRecorder.getClassForName(METRIC_OPTIONS_CLASS_NAME);

    static class VertxBinderEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            if (!mConfig.binder.httpServer.enabled.isPresent()) {
                // Look for old configuration attribute if the new attribute is not present
                mConfig.binder.httpServer.enabled = ConfigProvider.getConfig().getOptionalValue(
                        HttpServerFixedConfig.VERTX_BINDER_ENABLED_PROPERTY,
                        boolean.class);
                ;
            }
            return METRIC_OPTIONS_CLASS != null && mConfig.checkBinderEnabledWithDefault(mConfig.binder.httpServer);
        }
    }

    // avoid imports due to related deps not being there
    static final String RESTEASY_CONTAINER_FILTER_CLASS_NAME = "io.quarkus.micrometer.runtime.binder.vertx.VertxMeterBinderRestEasyContainerFilter";
    static final String QUARKUS_REST_CONTAINER_FILTER_CLASS_NAME = "io.quarkus.micrometer.runtime.binder.vertx.VertxMeterBinderQuarkusRestContainerFilter";

    @BuildStep(onlyIf = { VertxBinderEnabled.class })
    void enableJaxRsSupport(Capabilities capabilities,
            BuildProducer<ResteasyJaxrsProviderBuildItem> resteasyJaxrsProviders,
            BuildProducer<CustomContainerRequestFilterBuildItem> customContainerRequestFilter,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        if (capabilities.isPresent(Capability.RESTEASY)) {
            resteasyJaxrsProviders.produce(new ResteasyJaxrsProviderBuildItem(RESTEASY_CONTAINER_FILTER_CLASS_NAME));
            turnVertxBinderFilterIntoBean(additionalBeans, RESTEASY_CONTAINER_FILTER_CLASS_NAME);
        } else if (capabilities.isPresent(Capability.RESTEASY_REACTIVE)) {
            customContainerRequestFilter
                    .produce(new CustomContainerRequestFilterBuildItem(QUARKUS_REST_CONTAINER_FILTER_CLASS_NAME));
            turnVertxBinderFilterIntoBean(additionalBeans, QUARKUS_REST_CONTAINER_FILTER_CLASS_NAME);
        }
    }

    private void turnVertxBinderFilterIntoBean(BuildProducer<AdditionalBeanBuildItem> additionalBeans, String className) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(className)
                .setUnremovable().build());
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    AdditionalBeanBuildItem createVertxAdapters() {
        // Add Vertx meter adapters
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(VertxMeterBinderAdapter.class)
                .setUnremovable().build();
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    FilterBuildItem addVertxMeterFilter() {
        return new FilterBuildItem(new VertxMeterFilter(), Integer.MAX_VALUE);
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    @Record(value = ExecutionTime.STATIC_INIT)
    VertxOptionsConsumerBuildItem build(VertxMeterBinderRecorder recorder) {
        return new VertxOptionsConsumerBuildItem(recorder.configureMetricsAdapter(), Interceptor.Priority.LIBRARY_AFTER);
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    @Record(value = ExecutionTime.RUNTIME_INIT)
    void setVertxConfig(VertxMeterBinderRecorder recorder, HttpServerConfig config) {
        recorder.setVertxConfig(config);
    }

    private void warnIfDeprecatedPropertiesPresent() {
        if (ConfigProvider.getConfig().getOptionalValue(HttpServerFixedConfig.VERTX_BINDER_ENABLED_PROPERTY, boolean.class)
                .isPresent()) {
            MicrometerProcessor.LOG.warn(
                    "`" + HttpServerFixedConfig.VERTX_BINDER_ENABLED_PROPERTY
                            + "` is deprecated and will be removed in a future version. "
                            + "Use `quarkus.micrometer.binder.http-server.enabled` to enable metrics for inbound Http traffic "
                            + "using the micrometer extension");
        }

        if (ConfigProvider.getConfig()
                .getOptionalValue(HttpServerFixedConfig.VERTX_BINDER_IGNORE_PATTERNS_PROPERTY, boolean.class).isPresent()) {
            MicrometerProcessor.LOG.warn(
                    "`" + HttpServerFixedConfig.VERTX_BINDER_IGNORE_PATTERNS_PROPERTY
                            + "` is deprecated and will be removed in a future version. "
                            + "Use `quarkus.micrometer.binder.http-server.ignore-patterns` instead.");
        }

        if (ConfigProvider.getConfig()
                .getOptionalValue(HttpServerFixedConfig.VERTX_BINDER_MATCH_PATTERNS_PROPERTY, boolean.class).isPresent()) {
            MicrometerProcessor.LOG.warn(
                    "`" + HttpServerFixedConfig.VERTX_BINDER_MATCH_PATTERNS_PROPERTY
                            + "` is deprecated and will be removed in a future version. "
                            + "Use `quarkus.micrometer.binder.http-server.match-patterns` instead.");
        }
    }
}
