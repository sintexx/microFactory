package org.niels.master.serviceGraph.metrics;

import com.google.common.graph.MutableGraph;
import org.niels.master.model.Service;
import org.niels.master.serviceGraph.ServiceModel;

import java.util.*;

public class MetricCalculator {


    private ServiceModel serviceModel;

    public MetricCalculator(ServiceModel serviceModel) {

        this.serviceModel = serviceModel;
    }

    public Map<Metric, Object> calculateMetricForService(Service service) {
        var metrics = new HashMap<Metric, Object>();

        metrics.put(Metric.PRODUCED_ENDPOINTS, service.getInterfaces().size());

        return metrics;
    }

}
