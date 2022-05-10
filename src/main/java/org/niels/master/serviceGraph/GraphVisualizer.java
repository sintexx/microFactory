package org.niels.master.serviceGraph;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Records;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Compass;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.HttpServiceCall;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static guru.nidi.graphviz.attribute.Records.rec;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.port;

public class GraphVisualizer {
    public static byte[] getGraphAsPng(ServiceModel serviceModel) throws IOException {

        MutableGraph g = createGraph(serviceModel);

        var st = new ByteArrayOutputStream();
        Graphviz.fromGraph(g).render(Format.PNG).toOutputStream(st);

        return st.toByteArray();
    }

    @NotNull
    private static MutableGraph createGraph(ServiceModel serviceModel) {
        MutableGraph g = mutGraph("serviceGraph").setDirected(true);

        g.graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

        var allServiceNodes = new HashMap<String, MutableNode>();

        for (Service service : serviceModel.getConfig().getServices()) {
            var serviceNode = mutNode(service.getName());

            allServiceNodes.put(service.getName(), serviceNode);

            var ports = new ArrayList<>();
            for (Interface anInterface : service.getInterfaces()) {
                ports.add(rec(anInterface.getName(), anInterface.getName()));
            }

            serviceNode.add(Records.of(ArrayUtils.addAll(new String[]{service.getName() }, ports.toArray(new String[0]))));

            g.add(serviceNode);

        }

        for (Service service : serviceModel.getConfig().getServices()) {

            var currentServiceNode = allServiceNodes.get(service.getName());

            for (Interface anInterface : service.getInterfaces()) {
                for (Logic logic : anInterface.getLogic()) {
                    if (logic instanceof HttpServiceCall serviceCall) {
                        var connectedService = allServiceNodes.get(serviceCall.getService());

                        currentServiceNode.addLink(between(port(anInterface.getName()),
                                connectedService.port(serviceCall.getMethod(), Compass.WEST)));
                    }
                }
            }
        }


        return g;
    }
}
