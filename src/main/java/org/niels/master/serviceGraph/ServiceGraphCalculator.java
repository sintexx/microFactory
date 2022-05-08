package org.niels.master.serviceGraph;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.ServiceCall;

import java.util.HashMap;

public class ServiceGraphCalculator {
    public static MutableGraph<Service> generateGraphModel(ServiceModel serviceModel) {
        MutableGraph<Service> g = GraphBuilder.directed().build();

        var servicesByName = new HashMap<String, Service>();


        for (Service service : serviceModel.getConfig().getServices()) {
            g.addNode(service);


            for (Interface anInterface : service.getInterfaces()) {
                for (Logic logic : anInterface.getLogic()) {
                    if (logic instanceof ServiceCall serviceCall) {
                        g.putEdge(service, serviceModel.getServiceByName().get(serviceCall.getService()));
                    }
                }
            }
        }

        return g;
    }
}
