package io.quarkus.it.spring.boot;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tests {@code Map<String, ComplexObject>} binding —
 * each map value is a {@link RouteConfig} with scalar, int, and List fields.
 */
@ConfigurationProperties("app-routes")
public class AppRoutesProperties {

    private String version;

    private List<String> disabled;

    private Map<String, RouteConfig> routes;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getDisabled() {
        return disabled;
    }

    public void setDisabled(List<String> disabled) {
        this.disabled = disabled;
    }

    public Map<String, RouteConfig> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, RouteConfig> routes) {
        this.routes = routes;
    }
}
