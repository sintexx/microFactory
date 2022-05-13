package org.niels.master.serviceGraph.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.niels.master.model.Service;

import java.util.Map;

@Data
@AllArgsConstructor
public class ServiceWithMetrics {
    private Service service;
    private Map<Metric, Object> metrics;
}
