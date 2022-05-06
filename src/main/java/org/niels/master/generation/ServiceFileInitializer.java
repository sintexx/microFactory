package org.niels.master.generation;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class ServiceFileInitializer {
    private static Logger logger = LoggerFactory.getLogger(ServiceFileInitializer.class);


    public static void copyQuarkus(Path outputLocation, String serviceName, String tag) throws IOException, URISyntaxException {

        File quarkusTemplate = new File(ServiceFileInitializer.class.getClassLoader().getResource("code-with-quarkus").toURI());

        FileUtils.copyDirectory(quarkusTemplate, outputLocation.toFile());

        var buildGradle = outputLocation.resolve("build.gradle");

        var applicationProperties = outputLocation.resolve("src/main/resources/application.properties");

        FileUtils.writeStringToFile(applicationProperties.toFile(),
                System.lineSeparator() + "quarkus.container-image.name=" + serviceName,
                Charsets.toCharset("UTF8"), true);

        FileUtils.writeStringToFile(applicationProperties.toFile(),
                System.lineSeparator() + "quarkus.container-image.tag=" + tag,
                Charsets.toCharset("UTF8"), true);

        logger.info("Quarkus for service {} was created", serviceName);
    }
}
