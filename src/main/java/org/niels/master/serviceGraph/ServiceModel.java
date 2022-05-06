package org.niels.master.serviceGraph;

import com.google.common.graph.MutableGraph;
import lombok.Data;
import org.niels.master.model.Service;
import org.niels.master.model.ServiceModelConfig;

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
}
