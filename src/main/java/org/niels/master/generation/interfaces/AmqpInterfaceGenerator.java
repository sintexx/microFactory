package org.niels.master.generation.interfaces;

import com.squareup.javapoet.*;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.niels.master.generation.ApplicationProperties;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.generation.logic.InterfaceCodeGenerator;
import org.niels.master.model.interfaces.AmqpInterface;

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
                .addParameter(String.class, "incoming")
                .addAnnotation(AnnotationSpec.builder(Incoming.class)
                        .addMember("value", "$S", endpoint.getQuery()).build())
                .returns(void.class);

        this.applicationProperties.addLine("mp.messaging.incoming." + endpoint.getQuery() + ".connector=smallrye-rabbitmq");
        this.applicationProperties.addLine("mp.messaging.incoming." + endpoint.getQuery() + ".address="+ endpoint.getQuery());
        this.applicationProperties.addLine("mp.messaging.incoming." + endpoint.getQuery() + ".queue.name="+ endpoint.getQuery());



        processorClassBuilder.addMethod(endpointMethodBuilder.build());

        CodeGenUtils.writeToJavaFile(outputFolder, processorClassBuilder.build(), "org.niels.master.generated.amqp");


    }


}
