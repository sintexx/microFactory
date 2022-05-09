package org.niels.master.generation.containers;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.niels.master.model.DatabaseServer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseResourceGenerator {

    private Path outputFolder;

    public DatabaseResourceGenerator(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void initAllDatabaseKubernetesFiles(List<DatabaseServer> dbServers) throws IOException {
        int nodePort = 30133;
        for (DatabaseServer dbServer : dbServers) {
            this.initDatabaseKubernetesFile(dbServer, nodePort);
            nodePort++;
        }
    }

    private void initDatabaseKubernetesFile(DatabaseServer dbServer, int nodePort) throws IOException {
        VelocityEngine velocityEngine = new VelocityEngine();

        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        Velocity.init();

        Template t = velocityEngine.getTemplate("/kubernetes/postgres.vm");

        VelocityContext context = new VelocityContext();

        var databaseEnv = dbServer.getDatabases().stream().collect(Collectors.joining(","));

        context.put("databaseEnv", databaseEnv);
        context.put("serverName", DatabaseServer.getKubernetesServiceName(dbServer.getName()));
        context.put("nodePort", nodePort);

        StringWriter writer = new StringWriter();
        t.merge( context, writer );

        File targetFile = outputFolder.resolve(dbServer.getName() + "Postgres.yml").toFile();

        FileUtils.write(targetFile, writer.toString(), StandardCharsets.UTF_8);
    }
}
