package org.niels.master.generation;

import org.niels.master.model.Service;
import org.niels.master.serviceGraph.ServiceModel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class ServiceRepresentation {
    private Service service;

    private ServiceModel serviceModel;
    private Path codeFolder;
    private Path servicePath;
    private String imageTag;

    private File javaFolder;

    public ServiceRepresentation(Service service, ServiceModel serviceModel, Path codeFolder, String imageTag) {
        this.service = service;
        this.serviceModel = serviceModel;
        this.codeFolder = codeFolder;
        this.imageTag = imageTag;

        this.servicePath = codeFolder.resolve(service.getName());

        this.javaFolder = servicePath.resolve("src/main/java").toFile();
    }

    private void createQuarkusFolder() throws IOException, URISyntaxException {
        ServiceFileInitializer.copyQuarkus(servicePath, this.service.getName(), imageTag);
    }

    private void generateSourceCode() {

        var dataModelClass = MockDataModelGenerator.generateMockDataModel(this.javaFolder);

        this.generateHttpInterfaces();
        this.generateAmqpInterfaces();
        
    }

    private void generateHttpInterfaces() {

    }

    private void generateAmqpInterfaces() {
    }




}
