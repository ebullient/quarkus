package io.quarkus.resteasy.common.runtime;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class MethodFullPathIndex {
    private ConcurrentHashMap<Method, String> methodToFullPath = new ConcurrentHashMap<>();

    public String registerFullPath(Method method, String fullPath) {
        if (fullPath.charAt(0) != '/') {
            fullPath = '/' + fullPath;
        }
        methodToFullPath.put(method, fullPath);
        return fullPath;
    }

    public String getFullPath(Method method) {
        return methodToFullPath.get(method);
    }
}
