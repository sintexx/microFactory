package org.niels.master.generation.logic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import lombok.AllArgsConstructor;
import org.niels.master.generation.CodeConstants;

import java.util.ArrayList;

@AllArgsConstructor
public class DatabaseAccess {

    private ClassName dataModelClass;

    public void createDbAccessLogic(MethodSpec.Builder endpointMethodBuilder, org.niels.master.model.logic.DatabaseAccess dbAccess) {
        switch (dbAccess.getMethod()) {

            case GET_SINGLE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of(CodeConstants.singleDataVariable + " = $T.findById((long)$L)", dataModelClass, (long)1));
            }
            case GET_LIST -> {
                endpointMethodBuilder.addStatement(CodeBlock.of(CodeConstants.listDataVariable + " = $T.listAll()", dataModelClass));
            }
            case SAVE_SINGLE -> {
                endpointMethodBuilder.addStatement(CodeBlock.of(CodeConstants.singleDataVariable + ".id = null"));
                endpointMethodBuilder.addStatement(CodeBlock.of(CodeConstants.singleDataVariable + ".persistAndFlush()"));
            }
            case SAVE_LIST -> {
                endpointMethodBuilder.addStatement(CodeBlock.of(CodeConstants.listDataVariable + ".stream().forEach(d -> {d.id = null;d.persistAndFlush();})"));
            }
        }
    }
}
