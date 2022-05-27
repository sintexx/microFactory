package org.niels.master.serviceGraph.metrics;

import com.google.common.graph.Graphs;
import org.niels.master.model.Service;
import org.niels.master.model.logic.*;
import org.niels.master.serviceGraph.ServiceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceMetricCalculator {

    private static Logger logger = LoggerFactory.getLogger(ServiceMetricCalculator.class);


    private ServiceModel serviceModel;

    public ServiceMetricCalculator(ServiceModel serviceModel) {

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

        var syncServiceDependencies = countDependenciesByType(service, HttpServiceCall.class);

        metrics.put(Metric.SYNC_SERVICE_DEPENDENCIES, syncServiceDependencies);

        var asyncServiceDependencies = countDependenciesByType(service, AmqpServiceCall.class);

        metrics.put(Metric.ASYNC_SERVICE_DEPENDENCIES, asyncServiceDependencies);

        var isPartOfCycle = checkIfPartOfCycle(service);
        metrics.put(Metric.IS_PART_OF_CYCLE, isPartOfCycle);

        if (isPartOfCycle) {
            metrics.put(Metric.MAX_AFFECTED_SERVICES_CHAIN_PER_HANDLING, 999999);

        } else {
            try {
                // metrics.put(Metric.MAX_AFFECTED_SERVICES_CHAIN_PER_HANDLING, calculateMayAffectedServiceChain(service));

            }catch (Exception ex) {
                logger.error("Overflow on " + service);
            }

        }

        long maxDependencies = getMaxAffectedDependencies(service);

        metrics.put(Metric.MAX_AFFECTED_SERVICES_PER_HANDLING, (int)maxDependencies);


        metrics.put(Metric.DEPENDENCIES_SIMPLE_FAILOVER, countDependenciesByFailover(service, HttpServiceCall.Fallback.RETRY));

        metrics.put(Metric.DEPENDENCIES_COMPLEX_FAILOVER, countDependenciesByFailover(service, HttpServiceCall.Fallback.COMPLEX));

        var ABSOLUT_IMPORTANCE_OF_THE_SERVICE = getAllDependentServices(service.getName()).size();
        metrics.put(Metric.ABSOLUT_IMPORTANCE_OF_THE_SERVICE, ABSOLUT_IMPORTANCE_OF_THE_SERVICE);

        var ABSOLUT_DEPENDENCE_OF_THE_SERVICE = countDependenciesByType(service, ServiceCall.class);

        metrics.put(Metric.ABSOLUT_DEPENDENCE_OF_THE_SERVICE, ABSOLUT_DEPENDENCE_OF_THE_SERVICE);

        var ABSOLUT_CRITICALITY_OF_SERVICE = ABSOLUT_IMPORTANCE_OF_THE_SERVICE * ABSOLUT_DEPENDENCE_OF_THE_SERVICE;
        metrics.put(Metric.ABSOLUT_CRITICALITY_OF_SERVICE, ABSOLUT_CRITICALITY_OF_SERVICE);

        // Set in context to overall count
        metrics.put(Metric.RELATIVE_IMPORTANCE_OF_THE_SERVICE, ABSOLUT_IMPORTANCE_OF_THE_SERVICE / (double)this.serviceModel.getConfig().getServices().size());
        metrics.put(Metric.RELATIVE_DEPENDENCE_OF_THE_SERVICE, ABSOLUT_DEPENDENCE_OF_THE_SERVICE / (double)this.serviceModel.getConfig().getServices().size());
        metrics.put(Metric.RELATIVE_CRITICALITY_OF_SERVICE, ABSOLUT_CRITICALITY_OF_SERVICE / (double)this.serviceModel.getConfig().getServices().size());


        return metrics;
    }

    private long getMaxAffectedDependencies(Service service) {
        var res = service.getInterfaces().stream().map(i -> i.getLogic())
                .map(l -> l.stream().filter(logicStep -> !(logicStep instanceof DatabaseAccess)).count())
                .mapToLong(Long::longValue).max();


        if (res.isPresent()) {
            return (int)res.getAsLong();
        }

        return 0;
    }

    private int countDependenciesByType(Service service, Class<?> type) {
        var res = service.getInterfaces().stream().map(i -> i.getLogic())
                .map(l -> l.stream().filter(logicStep -> type.isInstance(logicStep)).count())
                .mapToLong(Long::longValue).max();

        if (res.isPresent()) {
            return (int)res.getAsLong();
        }

        return 0;
    }

    private List<Service> getAllDependentServices(String serviceName) {
        return this.serviceModel.getConfig().getServices().stream().filter(service -> {
             return service.getInterfaces().stream().map(i -> i.getLogic())
                    .filter(l -> {
                        for (Logic logic : l) {
                            if (logic instanceof ServiceCall serviceCall) {
                                if (serviceCall.getService().equals(serviceName)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }).count() > 0;
        }).collect(Collectors.toList());
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
        var res = getAllReachable(service, new HashSet<>());

        return res.contains(service);
    }

    private Set<Service> getAllReachable(Service service, Set<Service> res) {
        // Graphs.reachableNodes(this.serviceModel.getServiceGraph(), service)
        for (Service successor : this.serviceModel.getServiceGraph().successors(service)) {

            if (res.contains(successor))
                continue;

            res.add(successor);

            var next = getAllReachable(successor, res);

            res.addAll(next);
        }

        return res;
    }

}
