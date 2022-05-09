package org.niels.master.serviceGraph;

import com.google.common.graph.MutableGraph;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.generation.ServiceRepresentation;
import org.niels.master.generation.containers.DatabaseResourceGenerator;
import org.niels.master.model.Service;
import org.niels.master.model.ServiceModelConfig;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ServiceModel {

    private ServiceModelConfig config;
    private Map<String, Service> serviceByName = new HashMap<>();

    private MutableGraph<Service> serviceGraph;


    public ServiceModel(ServiceModelConfig config) {
        this.config = config;

        for (Service service : config.getServices()) {
            serviceByName.put(service.getName(), service);
        }

        this.serviceGraph = ServiceGraphCalculator.generateGraphModel(this);
    }

    public List<ServiceRepresentation> generateArtifacts() throws IOException, URISyntaxException {

        var imageTag = CodeGenUtils.getTimestampTag();

        var outputFolder = this.getClearedOutputFolder();

        Path kubernetesOutput = createCleanKubernetesFolder(outputFolder);

        new DatabaseResourceGenerator(kubernetesOutput).initAllDatabaseKubernetesFiles(this.config.getDatabaseServers());

        var createdServices = new ArrayList<ServiceRepresentation>();
        for (Service service : this.config.getServices()) {
            var rep = new ServiceRepresentation(service, outputFolder.toPath(), imageTag, this, kubernetesOutput);
            rep.generateServiceFromDefinition();
            createdServices.add(rep);
        }

        return createdServices;


    }

    @NotNull
    private Path createCleanKubernetesFolder(File outputFolder) throws IOException {
        var kubernetesOutput = outputFolder.toPath().resolve("kubernetes");

        FileUtils.deleteDirectory(kubernetesOutput.toFile());
        kubernetesOutput.toFile().mkdirs();
        return kubernetesOutput;
    }

    private File getClearedOutputFolder() throws IOException {
        var folder = new File("./GeneratedCode");

        FileUtils.deleteDirectory(folder);

        folder.mkdirs();

        return folder;
    }
}
