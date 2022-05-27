package org.niels.master.generation.logic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import lombok.AllArgsConstructor;
import org.niels.master.generation.CodeConstants;

import java.util.ArrayList;

@AllArgsConstructor
public class DatabaseAccess {

    private ClassName dataModelClass;

    public void createDbAccessLogic(ArrayList<CodeBlock> codeSteps, org.niels.master.model.logic.DatabaseAccess dbAccess) {
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
}
