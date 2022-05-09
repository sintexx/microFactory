package org.niels.master;

import org.niels.master.generation.ServiceRepresentation;
import org.niels.master.generation.containers.KubernetesRunner;
import org.niels.master.model.ModelReader;
import org.niels.master.serviceGraph.ServiceModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        var modelConfig = ModelReader.readModel(new File(Main.class.getClassLoader().getResource("./models/sampleModel.json").toURI()));

        var model = new ServiceModel(modelConfig);

        var createdServices = model.generateArtifacts();

        for (ServiceRepresentation createdService : createdServices) {
            createdService.build();
            createdService.copyKubernetesYaml();
        }

         KubernetesRunner.run(new File("./GeneratedCode").toPath().resolve("kubernetes"));

    }
}