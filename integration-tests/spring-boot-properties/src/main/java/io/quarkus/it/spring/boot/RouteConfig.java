package io.quarkus.it.spring.boot;

import java.util.List;

/**
 * Nested complex object used as a Map value in {@link AppRoutesProperties}.
 * Tests that {@code Map<String, ComplexObject>} binding populates fields correctly.
 */
public class RouteConfig {

    private String path;

    private int timeout;

    private List<String> methods;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }
}
