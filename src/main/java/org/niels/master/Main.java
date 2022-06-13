package org.niels.master;

import org.apache.commons.io.FileUtils;
import org.niels.master.generation.ServiceRepresentation;
import org.niels.master.generation.containers.KubernetesRunner;
import org.niels.master.model.ModelReader;
import org.niels.master.serviceGraph.GraphVisualizer;
import org.niels.master.serviceGraph.ServiceModel;
import org.niels.master.serviceGraph.metrics.MetricWriter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        var configName = args[0];

        var configFile = new File(Main.class.getClassLoader().getResource("models/" + configName).toURI());

        var content = FileUtils.readFileToString(configFile);

        var modelConfig = ModelReader.readModel(configFile);

        var model = new ServiceModel(modelConfig);

        var writer = new MetricWriter(model);

        writer.createWorkbookWithMetrics();

        var name = configFile.getName().replace(".json", "");

        var output =  new File("stats" + name).toPath();

        FileUtils.deleteDirectory(output.toFile());
       output.toFile().mkdir();

        writer.writeToFile(output.resolve("metrics" + name + ".xlsx").toFile());

        new GraphVisualizer(model).writeAllHandlingGraphs(output.resolve("handlingGraphs").toFile());

        var createdServices = model.generateArtifacts();

        if (args.length > 1 && args[1].equals("build")) {
            for (ServiceRepresentation createdService : createdServices) {
                createdService.build();
                createdService.copyKubernetesYaml();
            }

            KubernetesRunner.run(new File("./GeneratedCode").toPath().resolve("kubernetes"));
        }
    }
}