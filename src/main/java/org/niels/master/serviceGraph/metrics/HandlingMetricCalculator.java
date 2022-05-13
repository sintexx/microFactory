package org.niels.master.serviceGraph.metrics;

import lombok.AllArgsConstructor;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.serviceGraph.ServiceModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HandlingMetricCalculator {
    private List<ServiceWithMetrics> servicesWithMetrics;

    public Map<Metric, Object> getAveragesOfMetricsPerHandling(String handling) {
        var metrics = new HashMap<Metric, Object>();

        var servicesOfHandling = this.servicesWithMetrics.stream()
                .filter(s -> s.getService().getInterfaces().stream().filter(i -> i.getPartOfHandling().contains(handling)).count() > 0).collect(Collectors.toList());

        for (Metric m : Arrays.asList(Metric.values())) {
            var avg = servicesOfHandling.stream().map(s -> s.getMetrics().get(m)).filter(metric -> {
                        return metric instanceof Integer || metric instanceof Double;
                    })
                    .map(metric -> {
                        if (metric instanceof Integer mI) {
                            return mI.doubleValue();
                        }
                        return (double)metric;
                    }).mapToDouble(Double::doubleValue).average();

            if (avg.isPresent()) {
                metrics.put(m, avg.getAsDouble());
            }
        }

        return metrics;
    }
}
