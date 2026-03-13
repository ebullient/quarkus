package io.quarkus.it.spring.boot;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/server-pool")
public class ServerPoolPropertiesResource {

    @Inject
    ServerPoolProperties properties;

    @Path("/default-server")
    @GET
    public String getDefaultServer() {
        return properties.getDefaultServer();
    }

    @Path("/servers/{key}")
    @GET
    public String getServer(@PathParam("key") String key) {
        return properties.getServers().get(key);
    }

    @Path("/servers/size")
    @GET
    public int getServersSize() {
        return properties.getServers().size();
    }

    @Path("/labels/{key}")
    @GET
    public String getLabel(@PathParam("key") String key) {
        return properties.getLabels().get(key);
    }
}
