package org.niels.master.generation.interfaces;

import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.generation.logic.InterfaceCodeGenerator;
import org.niels.master.model.interfaces.HttpInterface;

import javax.lang.model.element.Modifier;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.niels.master.generation.CodeGenUtils.getHttpVerb;

public class HttpInterfaceGenerator {

    private ClassName dataModelClass;

    private InterfaceCodeGenerator interfaceCodeGenerator;

    private File outputFolder;


    public HttpInterfaceGenerator(ClassName dataModelClass, File outputFolder, InterfaceCodeGenerator interfaceCodeGenerator) {
        this.dataModelClass = dataModelClass;
        this.outputFolder = outputFolder;
        this.interfaceCodeGenerator = interfaceCodeGenerator;
    }





    public void generateInterface(HttpInterface endpoint) {
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

        this.interfaceCodeGenerator.addLogicToMethod(endpoint, endpointMethodBuilder, resourceClassBuilder);

        resourceClassBuilder.addMethod(endpointMethodBuilder.build());

        CodeGenUtils.writeToJavaFile(outputFolder, resourceClassBuilder.build(), "org.niels.master.generated.http");
    }


}
