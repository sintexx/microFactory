package org.niels.master.serviceGraph;

import com.google.common.graph.MutableGraph;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.niels.master.generation.CodeGenUtils;
import org.niels.master.generation.ServiceRepresentation;
import org.niels.master.model.Service;
import org.niels.master.model.ServiceModelConfig;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
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

    public void generateArtifacts() throws IOException, URISyntaxException {

        var imageTag = CodeGenUtils.getTimestampTag();

        var outputFolder = this.getClearedOutputFolder();

        for (Service service : this.config.getServices()) {
            new ServiceRepresentation(service, outputFolder.toPath(), imageTag).generateServiceFromDefinition();
        }
    }

    private File getClearedOutputFolder() throws IOException {
        var folder = new File("./GeneratedCode");

        FileUtils.deleteDirectory(folder);

        folder.mkdirs();

        return folder;
    }
}
