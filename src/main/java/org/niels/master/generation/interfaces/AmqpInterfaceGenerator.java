package org.niels.master.generation.interfaces;

import com.squareup.javapoet.*;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.niels.master.generation.ApplicationProperties;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.generation.logic.InterfaceCodeGenerator;
import org.niels.master.model.interfaces.AmqpInterface;
import org.niels.master.model.interfaces.Interface;

import javax.enterprise.context.ApplicationScoped;
import javax.lang.model.element.Modifier;
import java.io.File;

public class AmqpInterfaceGenerator {

    private ClassName dataModelClass;

    private InterfaceCodeGenerator interfaceCodeGenerator;

    private File outputFolder;

    private ApplicationProperties applicationProperties;

    public AmqpInterfaceGenerator(ClassName dataModelClass, InterfaceCodeGenerator interfaceCodeGenerator,
                                  File outputFolder,
                                  ApplicationProperties applicationProperties) {
        this.dataModelClass = dataModelClass;
        this.interfaceCodeGenerator = interfaceCodeGenerator;
        this.outputFolder = outputFolder;
        this.applicationProperties = applicationProperties;
    }

    public void generateInterface(AmqpInterface endpoint) {

        var className = endpoint.getName() + "Processor";


        var processorClassBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(ApplicationScoped.class)
                .addAnnotation(Traced.class);

        var endpointMethodBuilder = MethodSpec.methodBuilder("process")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Blocking.class)
                .addAnnotation(Traced.class)
                .addAnnotation(AnnotationSpec.builder(Incoming.class)
                        .addMember("value", "$S", endpoint.getQuery()).build())
                .returns(void.class);

        this.applicationProperties.addLine("mp.messaging.incoming." + endpoint.getQuery() + ".connector=smallrye-rabbitmq");
        this.applicationProperties.addLine("mp.messaging.incoming." + endpoint.getQuery() + ".address="+ endpoint.getQuery());
        this.applicationProperties.addLine("mp.messaging.incoming." + endpoint.getQuery() + ".queue.name="+ endpoint.getQuery());


        var logger = FieldSpec.builder(Logger.class, "LOGGER")
                .addModifiers( Modifier.PRIVATE, Modifier.STATIC)
                .initializer(CodeBlock.of("Logger.getLogger($L)", className + ".class")).build();

        processorClassBuilder.addField(logger);

        this.addInputParameters(endpoint, endpointMethodBuilder);

        this.interfaceCodeGenerator.addLogicToMethod(endpoint, endpointMethodBuilder, processorClassBuilder);

        processorClassBuilder.addMethod(endpointMethodBuilder.build());

        CodeGenUtils.writeToJavaFile(outputFolder, processorClassBuilder.build(), "org.niels.master.generated.amqp");


    }

    private void addInputParameters(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {

        endpointMethodBuilder.addParameter(JsonObject.class, "p");
        switch (endpoint.getIn()) {
            case SINGLE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("var " + CodeConstants.singleDataVariable + " = p.mapTo($T.class)", dataModelClass));
            }
            case LIST -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("var " + CodeConstants.singleDataVariable + " = p.mapTo($T)",
                        ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass)));
            }
        }
    }


}
