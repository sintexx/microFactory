package org.niels.master.serviceGraph.metrics;

import org.niels.master.model.Service;
import org.niels.master.model.logic.HttpServiceCall;
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

        var totalConsumedEndpoints = service.getInterfaces().stream().map(i -> i.getLogic())
                .flatMap(List::stream).filter(l -> {
                    return l instanceof HttpServiceCall;
                }).count();

        metrics.put(Metric.CONSUMED_ENDPOINTS, (int)totalConsumedEndpoints);

        metrics.put(Metric.MAX_AFFECTED_SERVICES_CHAIN_PER_HANDLING, calculateMayAffectedServiceChain(service));

        var maxDependencies = service.getInterfaces().stream().map(i -> i.getLogic())
                .map(l -> l.stream().filter(logicStep -> logicStep instanceof HttpServiceCall).count())
                .mapToLong(Long::longValue).max().getAsLong();

        metrics.put(Metric.MAX_AFFECTED_SERVICES_PER_HANDLING, (int)maxDependencies);


        metrics.put(Metric.DEPENDENCIES_SIMPLE_FAILOVER, countDependenciesByFailover(service, HttpServiceCall.Fallback.RETRY));

        metrics.put(Metric.DEPENDENCIES_COMPLEX_FAILOVER, countDependenciesByFailover(service, HttpServiceCall.Fallback.COMPLEX));

        metrics.put(Metric.IS_PART_OF_CYCLE, checkIfPartOfCycle(service));

        return metrics;
    }

    private int countDependenciesByFailover(Service service, HttpServiceCall.Fallback fallbackType) {
        return (int)service.getInterfaces().stream().map(i -> i.getLogic())
                .flatMap(List::stream).filter(l -> {
                    if (l instanceof HttpServiceCall serviceCall) {
                        return serviceCall.getFallback().equals(fallbackType);
                    }
                    return false;
                }).count();
    }

    private int calculateMayAffectedServiceChain(Service service) {


        if (this.serviceModel.getServiceGraph().successors(service).size() == 0) {
            return 1;
        }

        return 1 + this.serviceModel.getServiceGraph().successors(service).stream().map(a -> {
            return calculateMayAffectedServiceChain(a);
        }).mapToInt(Integer::intValue).max().getAsInt();
    }

    private boolean checkIfPartOfCycle(Service service) {
        return getAllReachable(service, new HashSet<>()).contains(service);
    }

    private Set<Service> getAllReachable(Service service, Set<Service> res) {
        if (this.serviceModel.getServiceGraph().successors(service).size() == 0) {
            return res;
        }

        for (Service successor : this.serviceModel.getServiceGraph().successors(service)) {

            if (res.contains(successor))
                continue;

            res.add(successor);

            res.addAll(getAllReachable(successor, res));
        }

        return res;
    }

}
