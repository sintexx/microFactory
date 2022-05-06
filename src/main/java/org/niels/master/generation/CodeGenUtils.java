package org.niels.master.generation;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CodeGenUtils {
    private static Logger logger = LoggerFactory.getLogger(CodeGenUtils.class);


    public static JavaFile writeToJavaFile(File outputFolder, TypeSpec typeSpec, String packageName) {
        var f= JavaFile.builder(packageName,
                        typeSpec)
                .build();

        try {
            f.writeTo(outputFolder);
        } catch (IOException e) {
            logger.error("Could not save javaFile", e);
        }

        return f;
    }
}
