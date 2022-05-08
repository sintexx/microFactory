package org.niels.master.generation;

import com.squareup.javapoet.ClassName;
import org.niels.master.generation.clients.RestClientGenerator;
import org.niels.master.generation.interfaces.HttpInterfaceGenerator;
import org.niels.master.model.Service;
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

    private RestClientGenerator restClientGenerator;

    public ServiceRepresentation(Service service, Path codeFolder, String imageTag) {
        this.service = service;
        this.imageTag = imageTag;

        this.servicePath = codeFolder.resolve(service.getName());

        this.javaFolder = servicePath.resolve("src/main/java").toFile();

    }

    public void generateServiceFromDefinition() throws IOException, URISyntaxException {
        this.createQuarkusFolder();
        this.generateSourceCode();
    }

    private void createQuarkusFolder() throws IOException, URISyntaxException {
        ServiceFileInitializer.copyQuarkus(servicePath, this.service.getName(), imageTag);
    }

    private void generateSourceCode() {

        var dataModelClass = MockDataModelGenerator.generateMockDataModel(this.javaFolder);

        this.restClientGenerator = new RestClientGenerator(dataModelClass, this.javaFolder);

        this.generateHttpInterfaces(dataModelClass);
        this.generateAmqpInterfaces();
        
    }

    private void generateHttpInterfaces(ClassName dataModelClass) {

        for (Interface anInterface : this.service.getInterfaces()) {
            if (anInterface instanceof HttpInterface httpInterface) {
                HttpInterfaceGenerator.generateHttpInterface(httpInterface, dataModelClass, this.javaFolder);
            }
        }
    }

    private void generateAmqpInterfaces() {
    }




}
