package org.niels.master.serviceGraph.metrics;

import com.google.common.graph.MutableGraph;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.AmqpServiceCall;
import org.niels.master.model.logic.HttpServiceCall;
import org.niels.master.serviceGraph.ServiceModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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


        StandardDeviation sd = new StandardDeviation(false);

        var t = this.serviceModel.getConfig().getServices().stream().map(s -> {
            return s.getInterfaces().stream().filter(i -> i.getPartOfHandling().contains(handling)).map(i -> {
                if (i.getWorkload() != null) {
                    return (double)i.getWorkload();
                }
                return (double)0;
            } ).collect(Collectors.toList());
        }).flatMap(Collection::stream).mapToDouble(d ->d).toArray();

        metrics.put(HandlingMetric.DISTRIBUTION_OF_WORKLOAD, sd.evaluate(t));


        metrics.put(HandlingMetric.NUMBER_OF_AFFECTED_SERVICES, this.serviceModel.getGraphPerHandling().get(handling).nodes().size());

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
