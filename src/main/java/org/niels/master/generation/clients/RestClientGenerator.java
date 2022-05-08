package org.niels.master.generation.clients;

import com.squareup.javapoet.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.model.interfaces.HttpInterface;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RestClientGenerator {

    private ClassName dataModelClass;
    private File outputFolder;

    public RestClientGenerator(ClassName dataModelClass, File outputFolder) {
        this.dataModelClass = dataModelClass;
        this.outputFolder = outputFolder;
    }

    private Map<HttpInterface, ClassName> existingClients = new HashMap<>();

    public ClassName generateRestClientIfNotExit(String url, HttpInterface httpInterface) {

        if (existingClients.containsKey(httpInterface)) {
            return existingClients.get(httpInterface);
        }

        var generatedClient = this.generateRestClient(url, httpInterface);

        existingClients.put(httpInterface, generatedClient);

        return generatedClient;
    }

    private ClassName generateRestClient(String url, HttpInterface httpInterface) {
        var typeBuilder = TypeSpec.interfaceBuilder(httpInterface.getName() + "Service")
                .addAnnotation(AnnotationSpec.builder(RegisterRestClient.class)
                        .addMember("baseUri", "$S", "http://" + url + "/" + httpInterface.getName()).build())
                .addModifiers(Modifier.PUBLIC);

        var endpointMethodBuilder = MethodSpec.methodBuilder(httpInterface.getMethod().toString().toLowerCase() + httpInterface.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);


        switch (httpInterface.getOut()) {

            case NONE -> {
                endpointMethodBuilder.addAnnotation(CodeGenUtils.getHttpVerb(httpInterface))
                        .returns(void.class);
            }
            case SINGLE -> {
                endpointMethodBuilder.addAnnotation(CodeGenUtils.getHttpVerb(httpInterface))
                        .returns(dataModelClass);
            }
            case LIST -> {
                endpointMethodBuilder.addAnnotation(CodeGenUtils.getHttpVerb(httpInterface))
                        .returns(ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass));
            }
        }

        typeBuilder.addMethod(endpointMethodBuilder.build());

        var f = CodeGenUtils.writeToJavaFile(outputFolder, typeBuilder.build(), "org.niels.master.generated.restClients");


        return ClassName.get(f.packageName,
                f.typeSpec.name);

    }
}
