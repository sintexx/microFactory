package org.niels.master.generation.logic;

import com.squareup.javapoet.*;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.model.interfaces.HttpInterface;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.AmqpServiceCall;
import org.niels.master.model.logic.DatabaseAccess;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.HttpServiceCall;
import org.niels.master.serviceGraph.ServiceModel;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;


public class InterfaceCodeGenerator {

    public InterfaceCodeGenerator(ClassName dataModelClass, RestClientGenerator restClientGenerator, ServiceModel serviceModel) {
        this.dataModelClass = dataModelClass;
        this.restClientGenerator = restClientGenerator;
        this.serviceModel = serviceModel;
    }

    private ClassName dataModelClass;

    private RestClientGenerator restClientGenerator;

    private ServiceModel serviceModel;

    public void addLogicToMethod(Interface endpoint, MethodSpec.Builder endpointMethodBuilder, TypeSpec.Builder resourceClassBuilder) {

        switch (endpoint.getIn()) {

            case NONE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("$T " + CodeConstants.singleDataVariable + " = null", dataModelClass));
                endpointMethodBuilder.addStatement(CodeBlock.of("$T " + CodeConstants.listDataVariable + " = new $T()",
                                ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass),
                        ParameterizedTypeName.get(ClassName.bestGuess("java.util.ArrayList"), dataModelClass)));
            }
            case SINGLE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("$T " + CodeConstants.listDataVariable + " = new $T()",
                        ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass),
                        ParameterizedTypeName.get(ClassName.bestGuess("java.util.ArrayList"), dataModelClass)));
            }
            case LIST -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("$T " + CodeConstants.singleDataVariable + " = null", dataModelClass));
            }
        }

        endpointMethodBuilder.addStatement(CodeBlock.of("LOGGER.info($S);", "Method "+ endpoint.getName() + "was called"));


        for (CodeBlock codeBlock : generateStepsForInterfaceLogic(endpoint, resourceClassBuilder)) {
            endpointMethodBuilder.addStatement(codeBlock);
        }

        checkAndAddAdditionalSleep(endpoint, endpointMethodBuilder);

        addReturnStatement(endpoint, endpointMethodBuilder);

        addOutputParameters(endpoint, endpointMethodBuilder);

        addTransactionAnnotationOnDbWrite(endpoint, endpointMethodBuilder);
    }

    private void checkAndAddAdditionalSleep(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        if (endpoint.getTime() != null) {
            endpointMethodBuilder.addStatement(CodeBlock.of("$T.sleep($L)", Thread.class, endpoint.getTime()));
            endpointMethodBuilder.addException(InterruptedException.class);
        }
    }

    private void addReturnStatement(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        switch (endpoint.getOut()) {

            case SINGLE -> {
                endpointMethodBuilder.addStatement("return " + CodeConstants.singleDataVariable);
            }
            case LIST -> {
                endpointMethodBuilder.addStatement("return " + CodeConstants.listDataVariable);

            }
        }
    }

    private List<CodeBlock> generateStepsForInterfaceLogic(Interface method, TypeSpec.Builder resourceClassBuilder) {
        var codeSteps = new ArrayList<CodeBlock>();

        for (Logic logic : method.getLogic()) {
            if (logic instanceof DatabaseAccess dbAccess) {
                createDbAccessLogic(codeSteps, dbAccess);
            }

            if (logic instanceof HttpServiceCall serviceCall) {
                createServiceCallLogic(resourceClassBuilder, codeSteps, serviceCall);
            }

            if (logic instanceof AmqpServiceCall amqpServiceCall) {

                createAmqpServiceCallLogic(resourceClassBuilder, codeSteps, amqpServiceCall);

            }
        }


        return codeSteps;
    }

    private void createAmqpServiceCallLogic(TypeSpec.Builder resourceClassBuilder, ArrayList<CodeBlock> codeSteps, AmqpServiceCall amqpServiceCall) {
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
                codeSteps.add(CodeBlock.of(channelFieldName + ".send(" + CodeConstants.singleDataVariable + ")"));

            }
            case LIST -> {
                codeSteps.add(CodeBlock.of(channelFieldName + ".send(" + CodeConstants.listDataVariable + ")"));
            }
        }
    }

    private void createServiceCallLogic(TypeSpec.Builder resourceClassBuilder, ArrayList<CodeBlock> codeSteps, HttpServiceCall serviceCall) {
        var calledService = this.serviceModel.getServiceByName().get(serviceCall.getService());

        if (calledService.getInterfaceByName(serviceCall.getEndpoint()) instanceof HttpInterface calledHttpInterface) {
            var restClientClasses = this.restClientGenerator.generateRestClientIfNotExit(calledService.getName(), calledHttpInterface);

            var serviceFieldName = calledService.getName() + calledHttpInterface.getName() + "Client";


            ClassName restClientClass;
            switch (serviceCall.getFallback()) {

                case RETRY -> {
                    restClientClass = restClientClasses.getRetry();

                }
                case COMPLEX -> {
                    restClientClass = restClientClasses.getFailover();
                }

                default -> {
                    restClientClass = restClientClasses.getStandard();
                }
            }


            var serviceClientField = FieldSpec.builder(restClientClass, serviceFieldName)
                    .addAnnotation(Inject.class)
                    .addAnnotation(RestClient.class)
                    .build();

            resourceClassBuilder.addField(serviceClientField);


            String clientCall = getServiceCallCodeLine(calledHttpInterface, serviceFieldName);

            codeSteps.add(CodeBlock.of(clientCall));
        }
    }

    @NotNull
    private String getServiceCallCodeLine(HttpInterface calledHttpInterface, String serviceFieldName) {
        String clientCall = "";
        switch (calledHttpInterface.getIn()) {

            case NONE -> {
                clientCall = serviceFieldName + "." + calledHttpInterface.getClientMethodName() + "()";
            }
            case SINGLE -> {
                clientCall = serviceFieldName + "." + calledHttpInterface.getClientMethodName() + "(" + CodeConstants.singleDataVariable + ")";
            }
            case LIST -> {
                clientCall = serviceFieldName + "." + calledHttpInterface.getClientMethodName() + "(" + CodeConstants.listDataVariable + ")";
            }
        }

        switch (calledHttpInterface.getOut()) {
            case SINGLE -> {
                clientCall = CodeConstants.singleDataVariable + " = " + clientCall;
            }
            case LIST -> {
                clientCall = CodeConstants.listDataVariable + " = " + clientCall;
            }
        }
        return clientCall;
    }

    private void createDbAccessLogic(ArrayList<CodeBlock> codeSteps, DatabaseAccess dbAccess) {
            switch (dbAccess.getMethod()) {

                case GET_SINGLE -> {
                    codeSteps.add(CodeBlock.of(CodeConstants.singleDataVariable + " = $T.findById((long)$L)", dataModelClass, (long)1));
                }
                case GET_LIST -> {
                    codeSteps.add(CodeBlock.of(CodeConstants.listDataVariable + " = $T.listAll()", dataModelClass));
                }
                case SAVE_SINGLE -> {
                    codeSteps.add(CodeBlock.of(CodeConstants.singleDataVariable + ".id = null"));
                    codeSteps.add(CodeBlock.of(CodeConstants.singleDataVariable + ".persist()"));
                }
                case SAVE_LIST -> {
                    codeSteps.add(CodeBlock.of(CodeConstants.listDataVariable + ".stream().forEach(d -> {d.id = null;d.persist();})"));
                }
            }
    }



    private void addOutputParameters(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        switch (endpoint.getOut()) {
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

    private void addTransactionAnnotationOnDbWrite(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        if (endpoint.getLogic().stream().filter(l -> {
            if (l instanceof DatabaseAccess dbAccess) {
                if (dbAccess.getMethod().equals(DatabaseAccess.DatabaseMethod.SAVE_SINGLE) || dbAccess.getMethod().equals(DatabaseAccess.DatabaseMethod.SAVE_LIST)) {
                    return true;
                }
            }
            return false;
        }).count() > 0) {
            endpointMethodBuilder.addAnnotation(Transactional.class);
        }
    }
}
