package org.niels.master.serviceGraph.metrics;

import com.google.common.graph.MutableGraph;
import lombok.AllArgsConstructor;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.AmqpServiceCall;
import org.niels.master.model.logic.HttpServiceCall;
import org.niels.master.serviceGraph.ServiceModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HandlingMetricCalculator {
    private List<ServiceWithMetrics> servicesWithMetrics;
    
    private ServiceModel serviceModel;

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

    public Map<HandlingMetric, Object> getHandlingSpecificMetrics(String handling) {
        var metrics = new HashMap<HandlingMetric, Object>();
        
        var graph = this.serviceModel.getGraphPerHandling().get(handling);

        metrics.put(HandlingMetric.SYNC_HANDLING_DEPENDENCIES, countSyncHandlingDependencies(graph, HttpServiceCall.class, handling));

        metrics.put(HandlingMetric.ASYNC_HANDLING_DEPENDENCIES, countSyncHandlingDependencies(graph, AmqpServiceCall.class, handling));


        var max = this.serviceModel.getConfig().getServices().stream().map(s -> {
            if (ServiceMetricCalculator.checkIfPartOfCycle(s, graph)) {
                return -1;
            }

            return ServiceMetricCalculator.calculateMayAffectedServiceChain(s, graph);
        }).mapToInt(Integer::intValue).max();


        metrics.put(HandlingMetric.MAX_AFFECTED_SERVICES_CHAIN, max.getAsInt());


        return metrics;
    }
    
    private int countSyncHandlingDependencies(MutableGraph<Service> graph, Class<?> type, String handling) {
        int count = 0;
        for (Service node : graph.nodes()) {
            count += countDependenciesByType(node, type, handling);
        }
        return count;
    }

    private int countDependenciesByType(Service service, Class<?> type, String handling) {
        var res = service.getInterfaces().stream().filter(i -> i.getPartOfHandling().contains(handling)).map(i -> i.getLogic())
                .map(l -> l.stream().filter(logicStep -> type.isInstance(logicStep)).count())
                .mapToLong(Long::longValue).max();

        if (res.isPresent()) {
            return (int)res.getAsLong();
        }

        return 0;
    }
}
