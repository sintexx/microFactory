package org.niels.master.generation.interfaces;

import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.niels.master.model.interfaces.HttpInterface;

import javax.lang.model.element.Modifier;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public class HttpInterfaceGenerator {
    public static void generateHttpInterface(HttpInterface endpoint, ClassName dataModelClass) {
        var resourceClassBuilder = TypeSpec.classBuilder(endpoint.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value", "$S", "/" + endpoint.getName()).build());

        Class httpVerb = getHttpVerb(endpoint);

        var endpointMethodBuilder = MethodSpec.methodBuilder("endpoint")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(httpVerb)
                .addAnnotation(AnnotationSpec.builder(Produces.class).addMember("value", "$S", MediaType.APPLICATION_JSON).build())
                .addAnnotation(AnnotationSpec.builder(Consumes.class).addMember("value", "$S", MediaType.APPLICATION_JSON).build())
                .addException(InterruptedException.class);

        switch (endpoint.getIn()) {

            case NONE -> {
                endpointMethodBuilder.returns(void.class);
            }
            case SINGLE -> {
                endpointMethodBuilder.returns(dataModelClass);
            }
            case LIST -> {
                endpointMethodBuilder.returns(ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass));

            }
        }


    }

    @NotNull
    private static Class getHttpVerb(HttpInterface endpoint) {
        Class httpVerb;

        switch (endpoint.getMethod()) {
            case "POST":
                httpVerb = POST.class;
                break;
            default:
                httpVerb = GET.class;
        }
        return httpVerb;
    }
}
