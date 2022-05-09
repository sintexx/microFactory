package org.niels.master.generation.logic;

import com.squareup.javapoet.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.model.interfaces.HttpInterface;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.DatabaseAccess;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.ServiceCall;
import org.niels.master.serviceGraph.ServiceModel;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.niels.master.model.logic.DatabaseAccess.DatabaseMethod.SAVE_SINGLE;

public class InterfaceCodeGenerator {

    public InterfaceCodeGenerator(ClassName dataModelClass, RestClientGenerator restClientGenerator, ServiceModel serviceModel) {
        this.dataModelClass = dataModelClass;
        this.restClientGenerator = restClientGenerator;
        this.serviceModel = serviceModel;
    }

    private ClassName dataModelClass;

    private RestClientGenerator restClientGenerator;

    private ServiceModel serviceModel;

    public void addLogicToMethod(HttpInterface endpoint, MethodSpec.Builder endpointMethodBuilder, TypeSpec.Builder resourceClassBuilder) {

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


        for (CodeBlock codeBlock : generateStepsForInterfaceLogic(endpoint, resourceClassBuilder)) {
            endpointMethodBuilder.addStatement(codeBlock);
        }

        switch (endpoint.getOut()) {

            case SINGLE -> {
                endpointMethodBuilder.addStatement("return " + CodeConstants.singleDataVariable);
            }
            case LIST -> {
                endpointMethodBuilder.addStatement("return " + CodeConstants.listDataVariable);

            }
        }

        addOutputParameters(endpoint, endpointMethodBuilder);

        addInputParameters(endpoint, endpointMethodBuilder);

        addTransactionAnnotationOnDbWrite(endpoint, endpointMethodBuilder);
    }

    private List<CodeBlock> generateStepsForInterfaceLogic(Interface method, TypeSpec.Builder resourceClassBuilder) {
        var codeSteps = new ArrayList<CodeBlock>();

        for (Logic logic : method.getLogic()) {
            if (logic instanceof DatabaseAccess dbAccess) {
                createDbAccessLogic(codeSteps, dbAccess);
            }

            if (logic instanceof ServiceCall serviceCall) {
                createServiceCallLogic(resourceClassBuilder, codeSteps, serviceCall);
            }
        }


        return codeSteps;
    }

    private void createServiceCallLogic(TypeSpec.Builder resourceClassBuilder, ArrayList<CodeBlock> codeSteps, ServiceCall serviceCall) {
        var calledService = this.serviceModel.getServiceByName().get(serviceCall.getService());

        if (calledService.getInterfaceByName(serviceCall.getMethod()) instanceof HttpInterface calledHttpInterface) {
            var restClientClass = this.restClientGenerator.generateRestClientIfNotExit(calledService.getName(), calledHttpInterface);

            var serviceFieldName = calledService.getName() + calledHttpInterface.getName() + "Client";

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
                }
                case GET_LIST -> {
                    codeSteps.add(CodeBlock.of(CodeConstants.listDataVariable + " = $T.listAll()", dataModelClass));
                }
                case SAVE_SINGLE -> {
                    codeSteps.add(CodeBlock.of(CodeConstants.singleDataVariable + ".persist()"));
                }
            }
    }

    private void addInputParameters(HttpInterface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        switch (endpoint.getIn()) {
            case NONE -> {
            }
            case SINGLE -> {
                endpointMethodBuilder.addParameter(dataModelClass, CodeConstants.singleDataVariable);
            }
            case LIST -> {
                endpointMethodBuilder.addParameter(ParameterizedTypeName.get(ClassName.bestGuess("java.util.List"), dataModelClass), CodeConstants.listDataVariable);
            }
        }
    }

    private void addOutputParameters(HttpInterface endpoint, MethodSpec.Builder endpointMethodBuilder) {
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

    private void addTransactionAnnotationOnDbWrite(HttpInterface endpoint, MethodSpec.Builder endpointMethodBuilder) {
        if (endpoint.getLogic().stream().filter(l -> {
            if (l instanceof DatabaseAccess dbAccess) {
                if (dbAccess.getMethod().equals(SAVE_SINGLE)) {
                    return true;
                }
            }
            return false;
        }).count() > 0) {
            endpointMethodBuilder.addAnnotation(Transactional.class);
        }
    }
}
