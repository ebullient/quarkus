package io.quarkus.it.spring.boot;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tests {@code Map<String, String>} binding alongside scalar fields.
 */
@ConfigurationProperties("server-pool")
public class ServerPoolProperties {

    private String defaultServer;

    private Map<String, String> servers;

    private Map<String, String> labels;

    public String getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(String defaultServer) {
        this.defaultServer = defaultServer;
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
