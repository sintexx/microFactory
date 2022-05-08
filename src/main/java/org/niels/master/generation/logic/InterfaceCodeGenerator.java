package org.niels.master.generation.logic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import org.jetbrains.annotations.NotNull;
import org.niels.master.generation.CodeConstants;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.HttpInterface;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.DatabaseAccess;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.ServiceCall;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.niels.master.model.logic.DatabaseAccess.DatabaseMethod.SAVE_SINGLE;

public class InterfaceCodeGenerator {

    public static void addLogicToMethod(HttpInterface endpoint, ClassName dataModelClass, MethodSpec.Builder endpointMethodBuilder) {

        switch (endpoint.getIn()) {

            case NONE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("$T " + CodeConstants.singleDataVariable + " = null", dataModelClass));
                endpointMethodBuilder.addStatement(CodeBlock.of("var " + CodeConstants.listDataVariable + " = new $T()",
                        ParameterizedTypeName.get(ClassName.bestGuess("java.util.ArrayList"), dataModelClass)));
            }
            case SINGLE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("var " + CodeConstants.listDataVariable + " = new $T()",
                        ParameterizedTypeName.get(ClassName.bestGuess("java.util.ArrayList"), dataModelClass)));
            }
            case LIST -> {
                endpointMethodBuilder.addStatement(CodeBlock.of("$T " + CodeConstants.singleDataVariable + " = null", dataModelClass));
            }
        }


        for (CodeBlock codeBlock : InterfaceCodeGenerator.generateStepsForInterfaceLogic(endpoint, dataModelClass)) {
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

        addOutputParameters(endpoint, dataModelClass, endpointMethodBuilder);

        addInputParameters(endpoint, dataModelClass, endpointMethodBuilder);

        addTransactionAnnotationOnDbWrite(endpoint, endpointMethodBuilder);
    }

    private static List<CodeBlock> generateStepsForInterfaceLogic(Interface method, ClassName dataModelClass) {
        var codeSteps = new ArrayList<CodeBlock>();

        Iterator<Logic> iter = method.getLogic().iterator();
        while (iter.hasNext()) {
            Logic logicStep = iter.next();


            if (logicStep instanceof DatabaseAccess dbAccess) {

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

            if (logicStep instanceof ServiceCall serviceCall) {

            }

            if (!iter.hasNext()) {

            }
        }

        return codeSteps;
    }

    private static void addInputParameters(HttpInterface endpoint, ClassName dataModelClass, MethodSpec.Builder endpointMethodBuilder) {
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

    private static void addOutputParameters(HttpInterface endpoint, ClassName dataModelClass, MethodSpec.Builder endpointMethodBuilder) {
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

    private static void addTransactionAnnotationOnDbWrite(HttpInterface endpoint, MethodSpec.Builder endpointMethodBuilder) {
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
