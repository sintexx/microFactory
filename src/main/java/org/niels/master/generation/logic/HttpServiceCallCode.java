package org.niels.master.generation.logic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;
import org.niels.master.generation.CodeConstants;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.model.interfaces.HttpInterface;
import org.niels.master.model.logic.HttpServiceCall;
import org.niels.master.serviceGraph.ServiceModel;

import javax.inject.Inject;
import java.util.ArrayList;

@AllArgsConstructor
public class HttpServiceCallCode {

    private ServiceModel serviceModel;
    private RestClientGenerator restClientGenerator;


    public void createHttpServiceCallLogic(TypeSpec.Builder resourceClassBuilder, ArrayList<CodeBlock> codeSteps, HttpServiceCall serviceCall) {
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


            String clientCall = getHttpServiceCallCodeLine(calledHttpInterface, serviceFieldName);

            codeSteps.add(CodeBlock.of(clientCall));
        }
    }

    @NotNull
    private String getHttpServiceCallCodeLine(HttpInterface calledHttpInterface, String serviceFieldName) {
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

}
