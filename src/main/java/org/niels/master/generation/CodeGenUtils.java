package org.niels.master.generation;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;
import org.niels.master.model.interfaces.HttpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @NotNull
    public static Class getHttpVerb(HttpInterface endpoint) {
        Class httpVerb;

        switch (endpoint.getMethod()) {
            case POST:
                httpVerb = POST.class;
                break;
            default:
                httpVerb = GET.class;
        }
        return httpVerb;
    }

    public static String getTimestampTag() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return sdf.format(new Date());
    }
}
