package org.niels.master.generation.logic;

import com.squareup.javapoet.*;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.niels.master.generation.CodeConstants;
import org.niels.master.model.logic.AmqpServiceCall;

import java.util.ArrayList;

@AllArgsConstructor
public class AmqpServiceCallCode {
    private ClassName dataModelClass;

    public void createAmqpServiceCallLogic(TypeSpec.Builder resourceClassBuilder, MethodSpec.Builder endpointMethodBuilder, AmqpServiceCall amqpServiceCall) {
        var channelFieldName = amqpServiceCall.getQuery();

        if (resourceClassBuilder.fieldSpecs.stream().filter(s -> s.name.equals(channelFieldName)).count() == 0) {

            TypeName outputType;

            switch (amqpServiceCall.getOut()) {
                case LIST -> {
                    outputType = ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass);
                }
                default -> {
                    outputType= this.dataModelClass;
                }
            }


            var channelField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Emitter.class), outputType),
                            channelFieldName)
                    .addAnnotation(AnnotationSpec.builder(Channel.class).addMember("value", "$S", amqpServiceCall.getQuery()).build())
                    .build();

            resourceClassBuilder.addField(channelField);
        }

        switch (amqpServiceCall.getOut()) {

            case SINGLE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of(channelFieldName + ".send(" + CodeConstants.singleDataVariable + ")"));

            }
            case LIST -> {
                endpointMethodBuilder.addStatement(CodeBlock.of(channelFieldName + ".send(" + CodeConstants.listDataVariable + ")"));
            }
        }
    }


}
