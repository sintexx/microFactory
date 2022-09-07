package org.niels.master;

import org.apache.commons.io.FileUtils;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.generation.ServiceRepresentation;
import org.niels.master.generation.containers.KubernetesRunner;
import org.niels.master.model.ModelReader;
import org.niels.master.serviceGraph.GraphVisualizer;
import org.niels.master.serviceGraph.ServiceModel;
import org.niels.master.serviceGraph.metrics.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {



        if (args.length == 0) {
            for (File models : FileUtils.listFiles(new File(Main.class.getClassLoader().getResource("models").toURI()), new String[]{"json"}, false)) {
                runForName(args, models.getName());
            }



        } else {
            var configName = args[0];

            runForName(args, configName);
        }


    }

    private static void runForName(String[] args, String configName) throws URISyntaxException, IOException, InterruptedException {

        System.out.println("create for model " + configName);

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

        new GraphVisualizer(model).writeAllHandlingGraphs(output.resolve("handlingGraphs").toFile(), name);


        if (args.length > 1 && args[1].equals("build")) {

            var codeOut = getClearedOutputFolder(name);

            var createdServices = model.generateArtifacts(codeOut);


            for (ServiceRepresentation createdService : createdServices) {
                createdService.build();
                createdService.copyKubernetesYaml();
            }

            KubernetesRunner.run(codeOut.toPath().resolve("kubernetes"));
        }
    }

    private static File getClearedOutputFolder(String name) throws IOException {
        var folder = new File("./GeneratedCode/" + name);

        FileUtils.deleteDirectory(folder);

        folder.mkdirs();

        return folder;
    }
}