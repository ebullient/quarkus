package io.quarkus.it.spring.boot;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/app-routes")
public class AppRoutesPropertiesResource {

    @Inject
    AppRoutesProperties properties;

    @Path("/version")
    @GET
    public String getVersion() {
        return properties.getVersion();
    }

    @Path("/disabled/size")
    @GET
    public int getDisabledSize() {
        return properties.getDisabled().size();
    }

    @Path("/disabled/{index}")
    @GET
    public String getDisabled(@PathParam("index") int index) {
        return properties.getDisabled().get(index);
    }

    @Path("/routes/{name}/path")
    @GET
    public String getRoutePath(@PathParam("name") String name) {
        return properties.getRoutes().get(name).getPath();
    }

    @Path("/routes/{name}/timeout")
    @GET
    public int getRouteTimeout(@PathParam("name") String name) {
        return properties.getRoutes().get(name).getTimeout();
    }

    @Path("/routes/{name}/methods/size")
    @GET
    public int getRouteMethodsSize(@PathParam("name") String name) {
        return properties.getRoutes().get(name).getMethods().size();
    }

    @Path("/routes/{name}/methods/{index}")
    @GET
    public String getRouteMethod(@PathParam("name") String name, @PathParam("index") int index) {
        return properties.getRoutes().get(name).getMethods().get(index);
    }
}
