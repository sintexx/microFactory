package org.niels.master.generation.clients;

import com.squareup.javapoet.*;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.model.interfaces.HttpInterface;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestClientGenerator {

    private ClassName dataModelClass;
    private File outputFolder;

    public RestClientGenerator(ClassName dataModelClass, File outputFolder) {
        this.dataModelClass = dataModelClass;
        this.outputFolder = outputFolder;
    }

    private Map<HttpInterface, RestClient> existingClients = new HashMap<>();

    public RestClient generateRestClientIfNotExit(String url, HttpInterface httpInterface) {

        if (existingClients.containsKey(httpInterface)) {
            return existingClients.get(httpInterface);
        }

        var generatedClient = this.generateRestClient(url, httpInterface);

        existingClients.put(httpInterface, generatedClient);

        return generatedClient;
    }

    private RestClient generateRestClient(String url, HttpInterface httpInterface) {
        var typeBuilder = TypeSpec.interfaceBuilder(httpInterface.getName() + "Service")
                .addAnnotation(AnnotationSpec.builder(RegisterRestClient.class)
                        .addMember("baseUri", "$S", "http://" + url + "/" + httpInterface.getName()).build())
                .addModifiers(Modifier.PUBLIC);

        var endpointMethodBuilder = MethodSpec.methodBuilder(httpInterface.getClientMethodName())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        this.addParameterToRestClient(httpInterface, endpointMethodBuilder);

        ClassName fallbackClass = addReturnTypeToRestClient(httpInterface, endpointMethodBuilder);

        // NONE
        typeBuilder.addMethod(endpointMethodBuilder.build());

        var standardFile = CodeGenUtils.writeToJavaFile(outputFolder, typeBuilder.build(), "org.niels.master.generated.restClients");

        var standard = ClassName.get(standardFile.packageName,
                standardFile.typeSpec.name);


        //RETRY
        typeBuilder.methodSpecs.clear();
        endpointMethodBuilder.addAnnotation(AnnotationSpec.builder(Retry.class).addMember("maxRetries", "$L",4).build());

        typeBuilder.addMethod(endpointMethodBuilder.build());
        var retryFile = CodeGenUtils.writeToJavaFile(outputFolder, typeBuilder.build(), "org.niels.master.generated.restClients");


        var retry = ClassName.get(retryFile.packageName,
                retryFile.typeSpec.name);


        // COMPLEX
        typeBuilder.methodSpecs.clear();
        addFallbackToRestClient(endpointMethodBuilder, fallbackClass);

        typeBuilder.addMethod(endpointMethodBuilder.build());

        var fallbackFile = CodeGenUtils.writeToJavaFile(outputFolder, typeBuilder.build(), "org.niels.master.generated.restClients");


        var fallback = ClassName.get(fallbackFile.packageName,
                fallbackFile.typeSpec.name);

        return new RestClient(standard, retry, fallback);
    }

    private void addParameterToRestClient(HttpInterface httpInterface, MethodSpec.Builder endpointMethodBuilder) {
        switch (httpInterface.getIn()) {
            case SINGLE -> {
                endpointMethodBuilder.addParameter(ParameterSpec.builder(this.dataModelClass, CodeConstants.singleDataVariable).build());
            }
            case LIST -> {
                var param = ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass);

                endpointMethodBuilder.addParameter(ParameterSpec.builder(param, CodeConstants.listDataVariable).build());

            }
        }
    }

    @Nullable
    private ClassName addReturnTypeToRestClient(HttpInterface httpInterface, MethodSpec.Builder endpointMethodBuilder) {
        ClassName fallbackClass = null;

        switch (httpInterface.getOut()) {

            case NONE -> {
                endpointMethodBuilder.addAnnotation(CodeGenUtils.getHttpVerb(httpInterface))
                        .returns(void.class);

            }
            case SINGLE -> {
                var returns = dataModelClass;

                endpointMethodBuilder.addAnnotation(CodeGenUtils.getHttpVerb(httpInterface))
                        .returns(returns);

                fallbackClass = generateFallBackClass(httpInterface, returns, CodeBlock.of("return new $T($S);", dataModelClass, "fallback"));

            }
            case LIST -> {
                var returns = ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass);

                endpointMethodBuilder.addAnnotation(CodeGenUtils.getHttpVerb(httpInterface))
                        .returns(returns);

                fallbackClass = generateFallBackClass(httpInterface, returns,
                        CodeBlock.of("return $T.of(new $T($S));", List.class, dataModelClass, "fallback"));
            }
        }
        return fallbackClass;
    }

    private void addFallbackToRestClient(MethodSpec.Builder endpointMethodBuilder, ClassName fallbackClass) {
        if (fallbackClass != null) {
            endpointMethodBuilder.addAnnotation(AnnotationSpec.builder(Fallback.class)
                    .addMember("value", "$T.class", fallbackClass)
                    .build());
        }
    }

    private ClassName generateFallBackClass(HttpInterface httpInterface, TypeName returnType, CodeBlock returnCode) {

        var fallBackMethod = MethodSpec.methodBuilder("handle")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ExecutionContext.class, "context")
                .addCode(returnCode)
                .returns(returnType).build();


        var fallBackClass = TypeSpec.classBuilder(httpInterface.getClientMethodName() + "Fallback")
                .addMethod(fallBackMethod)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.bestGuess("org.eclipse.microprofile.faulttolerance.FallbackHandler"), returnType))
                .build();


        var f = CodeGenUtils.writeToJavaFile(outputFolder, fallBackClass, "org.niels.master.generated.restServices");

        return ClassName.get(f.packageName,
                f.typeSpec.name);
    }
}
