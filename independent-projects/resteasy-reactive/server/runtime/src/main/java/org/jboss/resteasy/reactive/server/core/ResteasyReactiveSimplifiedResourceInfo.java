package org.jboss.resteasy.reactive.server.core;

import org.jboss.resteasy.reactive.server.SimpleResourceInfo;

public final class ResteasyReactiveSimplifiedResourceInfo implements SimpleResourceInfo {

    private final String methodName;
    private final Class<?> resourceClass;
    private final Class<?>[] parameterTypes;
    private final String templateUriPath;

    public ResteasyReactiveSimplifiedResourceInfo(String methodName, Class<?> resourceClass, Class<?>[] parameterTypes,
            String templateUriPath) {
        this.methodName = methodName;
        this.resourceClass = resourceClass;
        this.parameterTypes = parameterTypes;
        this.templateUriPath = templateUriPath;
    }

    @Override
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Class<?>[] parameterTypes() {
        return parameterTypes;
    }

    @Override
    public String getTemplateUriPath() {
        return templateUriPath;
    }
}
