package org.niels.master.serviceGraph;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.AmqpServiceCall;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.HttpServiceCall;
import org.niels.master.model.logic.ServiceCall;

import java.util.HashMap;

public class ServiceGraphCalculator {
    public static MutableGraph<Service> generateGraphModel(ServiceModel serviceModel, String handling) {
        MutableGraph<Service> g = GraphBuilder.directed().build();

        for (Service service : serviceModel.getConfig().getServices()) {
            g.addNode(service);


            for (Interface anInterface : service.getInterfaces()) {

                if (handling != null && !anInterface.getPartOfHandling().contains(handling)) {
                    continue;
                }

                for (Logic logic : anInterface.getLogic()) {
                    if (logic instanceof ServiceCall serviceCall) {
                        if (serviceCall.getService() != null) {
                            g.putEdge(service, serviceModel.getServiceByName().get(serviceCall.getService()));
                        }
                    }
                }
            }
        }

        return g;
    }
}
