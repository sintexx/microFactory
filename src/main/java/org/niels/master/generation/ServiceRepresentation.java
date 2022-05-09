package org.niels.master.generation;

import com.squareup.javapoet.ClassName;
import org.apache.commons.io.FileUtils;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.generation.interfaces.AmqpInterfaceGenerator;
import org.niels.master.generation.interfaces.HttpInterfaceGenerator;
import org.niels.master.generation.logic.InterfaceCodeGenerator;
import org.niels.master.model.DatabaseServer;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.AmqpInterface;
import org.niels.master.model.interfaces.HttpInterface;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.serviceGraph.ServiceModel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class ServiceRepresentation {
    private Service service;

    private Path servicePath;
    private String imageTag;

    private File javaFolder;

    private ServiceModel serviceModel;

    private Path kubernetesOutput;

    private ApplicationProperties applicationProperties;


    public ServiceRepresentation(Service service, Path codeFolder, String imageTag, ServiceModel serviceModel, Path kubernetesOutput) {
        this.service = service;
        this.imageTag = imageTag;

        this.servicePath = codeFolder.resolve(service.getName());

        this.javaFolder = servicePath.resolve("src/main/java").toFile();

        this.serviceModel = serviceModel;

        this.kubernetesOutput = kubernetesOutput;

    }

    public void generateServiceFromDefinition() throws IOException, URISyntaxException {
        this.createQuarkusFolder();
        this.generateSourceCode();
    }

    public void build() throws IOException, InterruptedException {
        var builder = new ProcessBuilder("gradle", "build").directory(servicePath.toFile()).inheritIO();

        builder.start().waitFor();
    }

    public void copyKubernetesYaml() throws IOException {
        var kubernetesYml = servicePath.resolve("build/kubernetes/minikube.yml").toFile();

        FileUtils.copyFile(kubernetesYml, this.kubernetesOutput.resolve(this.service.getName() + ".yml").toFile());
    }

    private void createQuarkusFolder() throws IOException, URISyntaxException {
        ServiceFileInitializer.copyQuarkus(servicePath, this.service.getName(), imageTag);

        this.applicationProperties = new ApplicationProperties(servicePath.resolve("src/main/resources/application.properties").toFile());

        addServiceSpecificsToApplicationProperties();
    }

    private void addServiceSpecificsToApplicationProperties() {

        this.applicationProperties.addLine("quarkus.container-image.name=" + service.getName());
        this.applicationProperties.addLine("quarkus.container-image.tag=" + this.imageTag);

        this.applicationProperties.addLine("quarkus.jaeger.service-name=" + service.getName() );

        if (this.service.getDbms() != null) {
            var jdbcString = "jdbc:tracing:postgresql://" + DatabaseServer.getKubernetesServiceName(this.service.getDbms()) + ":5432/" + this.service.getDatabase();

            this.applicationProperties.addLine("quarkus.datasource.jdbc.url=" + jdbcString);
        }
    }

    private void generateSourceCode() {

        var dataModelClass = MockDataModelGenerator.generateMockDataModel(this.javaFolder);


        generateInterfaces(dataModelClass);

    }

    private void generateInterfaces(ClassName dataModelClass) {

        var restClientGenerator = new RestClientGenerator(dataModelClass, this.javaFolder);

        var interfaceCodeGenerator = new InterfaceCodeGenerator(dataModelClass, restClientGenerator, serviceModel);

        var httpInterfaceGenerator = new HttpInterfaceGenerator(dataModelClass, this.javaFolder, interfaceCodeGenerator);
        var amqpInterfaceGenerator = new AmqpInterfaceGenerator(dataModelClass, interfaceCodeGenerator, javaFolder, applicationProperties);

        for (Interface anInterface : this.service.getInterfaces()) {
            if (anInterface instanceof HttpInterface httpInterface) {
                httpInterfaceGenerator.generateInterface(httpInterface);
            }

            if (anInterface instanceof AmqpInterface amqpInterface) {
                amqpInterfaceGenerator.generateInterface(amqpInterface);
            }
        }
    }
}
