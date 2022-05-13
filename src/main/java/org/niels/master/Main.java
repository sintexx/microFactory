package org.niels.master;

import org.niels.master.generation.ServiceRepresentation;
import org.niels.master.generation.containers.KubernetesRunner;
import org.niels.master.model.ModelReader;
import org.niels.master.serviceGraph.GraphVisualizer;
import org.niels.master.serviceGraph.ServiceModel;
import org.niels.master.serviceGraph.metrics.MetricWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        var modelConfig = ModelReader.readModel(new File(Main.class.getClassLoader().getResource("./models/ftgo.json").toURI()));

        var model = new ServiceModel(modelConfig);

        var writer = new MetricWriter(model);

        writer.createWorkbookWithMetrics();

        writer.writeToFile(new File("metrics.xlsx"));

        GraphVisualizer.writeGraphAsSvg(model, new File("graph.svg"));

        var createdServices = model.generateArtifacts();


        if (args.length > 0 && args[0].equals("build")) {
            for (ServiceRepresentation createdService : createdServices) {
                createdService.build();
                createdService.copyKubernetesYaml();
            }

            KubernetesRunner.run(new File("./GeneratedCode").toPath().resolve("kubernetes"));
        }
    }
}