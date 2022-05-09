package org.niels.master.generation.containers;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class KubernetesRunner {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesRunner.class);


    private static final String scriptName = "recreateEnvironment.sh";

    public static void run(Path kubernetesFolder) throws URISyntaxException, IOException, InterruptedException {

        logger.info("Create Kubernetes environment");

        var script = new File(
                KubernetesRunner.class.getClassLoader()
                        .getResource("./kubernetes/" + scriptName)
                        .toURI());

        FileUtils.copyFile(script, kubernetesFolder.resolve(scriptName).toFile());

        var builder = new ProcessBuilder("./" + scriptName)
                .directory(kubernetesFolder.toFile()).inheritIO();



        builder.start().waitFor();

    }
}
