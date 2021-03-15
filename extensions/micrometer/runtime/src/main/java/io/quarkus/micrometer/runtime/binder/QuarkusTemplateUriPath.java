package io.quarkus.micrometer.runtime.binder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface QuarkusTemplateUriPath {
    String value();
}
