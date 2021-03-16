package io.quarkus.micrometer.deployment.binder;

import java.util.Collection;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.micrometer.deployment.MicrometerProcessor;
import io.quarkus.resteasy.server.common.spi.ResteasyJaxrsConfigBuildItem;

public class RestPathAnnotationHandler {

    static final DotName PATH_ANNOTATION = DotName.createSimple("javax.ws.rs.Path");
    static final DotName PATH_TEMPLATE_ANNOTATION = DotName
            .createSimple("io.quarkus.micrometer.runtime.binder.QuarkusTemplateUriPath");

    static AnnotationsTransformerBuildItem transformAnnotations(final IndexView index,
            Optional<ResteasyJaxrsConfigBuildItem> resteasyJaxrsConfig) {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
            @Override
            public void transform(TransformationContext ctx) {
                final Collection<AnnotationInstance> annotations = ctx.getAnnotations();
                AnnotationInstance annotation = MicrometerProcessor.findAnnotation(annotations, PATH_ANNOTATION);
                if (annotation == null || !ctx.isMethod()) {
                    return;
                }
                AnnotationTarget target = ctx.getTarget();
                MethodInfo methodInfo = target.asMethod();
                ClassInfo classInfo = methodInfo.declaringClass();

            }
        });
    }
}
