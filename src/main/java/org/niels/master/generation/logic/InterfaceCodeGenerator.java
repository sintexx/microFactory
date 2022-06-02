package org.niels.master.generation.logic;

import com.squareup.javapoet.*;
import org.apache.commons.math3.primes.Primes;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.*;
import org.niels.master.model.logic.DatabaseAccess;
import org.niels.master.serviceGraph.ServiceModel;

import javax.transaction.Transactional;


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

        addInitialVariable(endpoint, endpointMethodBuilder);

        endpointMethodBuilder.addStatement(CodeBlock.of("LOGGER.info($S);", "Method "+ endpoint.getName() + "was called"));

        generateStepsForInterfaceLogic(endpoint, resourceClassBuilder, endpointMethodBuilder);


        checkAndAddAdditionalSleep(endpoint, endpointMethodBuilder);

        addReturnStatement(endpoint, endpointMethodBuilder);

        addOutputParameters(endpoint, endpointMethodBuilder);

        addTransactionAnnotationOnDbWrite(endpoint, endpointMethodBuilder);
    }

    private void addInitialVariable(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {
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
    }

    private void checkAndAddAdditionalSleep(Interface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        if (endpoint.getTime() != null) {
            endpointMethodBuilder.addStatement(CodeBlock.of("$T.sleep($L)", Thread.class, endpoint.getTime()));
            endpointMethodBuilder.addException(InterruptedException.class);
        }

        if (endpoint.getWorkload() != null) {
            endpointMethodBuilder.beginControlFlow("for (int i = 0; i <= " + endpoint.getWorkload() +"; i++)")
                .beginControlFlow("for (int y = 100; y <= 100000; y++)")
                        .addStatement("$T.primeFactors(y)", Primes.class)
                        .endControlFlow()
            .endControlFlow();
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

    private void generateStepsForInterfaceLogic(Interface method, TypeSpec.Builder resourceClassBuilder, MethodSpec.Builder endpointMethodBuilder) {

        for (Logic logic : method.getLogic()) {
            if (logic instanceof DatabaseAccess dbAccess) {
                new org.niels.master.generation.logic.DatabaseAccess(this.dataModelClass)
                        .createDbAccessLogic(endpointMethodBuilder, dbAccess);
            }

            if (logic instanceof HttpServiceCall serviceCall) {
                new HttpServiceCallCode(this.serviceModel, this.restClientGenerator)
                        .createHttpServiceCallLogic(resourceClassBuilder, endpointMethodBuilder, serviceCall);
            }

            if (logic instanceof AmqpServiceCall amqpServiceCall) {
                new AmqpServiceCallCode(this.dataModelClass)
                        .createAmqpServiceCallLogic(resourceClassBuilder, endpointMethodBuilder, amqpServiceCall);
            }

            if (logic instanceof InsertMock insertMock) {
                if (insertMock.getTargetVariable() == InsertMock.TargetVariable.SINGLE) {
                    endpointMethodBuilder.addStatement(CodeBlock.of(CodeConstants.singleDataVariable + " = new $T($S)", this.dataModelClass, method.getName()));
                }

                if (insertMock.getTargetVariable() == InsertMock.TargetVariable.LIST) {
                    endpointMethodBuilder.beginControlFlow("for (int i = 0; i < " + insertMock.getSize() + "; i++)")
                            .addStatement(CodeConstants.listDataVariable + ".add(new $T($S))", this.dataModelClass, method.getName())
                            .endControlFlow().build();
                }


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
