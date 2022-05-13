package org.niels.master.serviceGraph;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Records;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Compass;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.AmqpInterface;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.AmqpServiceCall;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.HttpServiceCall;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Records.rec;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.port;

public class GraphVisualizer {

    public static void writeAllHandlingGraphs(ServiceModel serviceModel, File output) throws IOException {
        for (String allHandling : serviceModel.getAllHandlings()) {
            writeHandlingGraphAsSvg(serviceModel, allHandling,
                    output.toPath().resolve(allHandling + ".svg").toFile());
        }
    }

    public static byte[] getGraphAsPng(ServiceModel serviceModel) throws IOException {

        MutableGraph g = createGraph(serviceModel, null);

        var st = new ByteArrayOutputStream();
        Graphviz.fromGraph(g).render(Format.PNG).toOutputStream(st);

        return st.toByteArray();
    }

    public static void writeGraphAsSvg(ServiceModel serviceModel, File out) throws IOException {

        MutableGraph g = createGraph(serviceModel, null);

        Graphviz.fromGraph(g).render(Format.SVG).toFile(out);
    }

    public static void writeHandlingGraphAsSvg(ServiceModel serviceModel, String handling, File out) throws IOException {
        MutableGraph g = createGraph(serviceModel, handling);

        Graphviz.fromGraph(g)
                .render(Format.SVG).toFile(out);
    }

    @NotNull
    private static MutableGraph createGraph(ServiceModel serviceModel, String handling) {
        MutableGraph g = mutGraph("serviceGraph").setDirected(true);

        g.graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

        var allServiceNodes = new HashMap<String, MutableNode>();

        var amqpNodes = new HashMap<String, MutableNode>();

        for (Service service : serviceModel.getConfig().getServices()) {

            if (handling != null && !service.isPartOfHandling(handling)) {
                continue;
            }

            var serviceNode = mutNode(service.getName());

            allServiceNodes.put(service.getName(), serviceNode);

            var ports = new ArrayList<>();
            for (Interface anInterface : service.getInterfaces()) {
                ports.add(rec(anInterface.getName(), anInterface.getName()));

                if (anInterface instanceof AmqpInterface amqpInterface) {
                    var queryNode = getOrCreateAmqpQueryNode(amqpNodes, amqpInterface.getQuery(), g);

                    queryNode.addLink(between(port(anInterface.getName()), serviceNode));
                }

            }

            serviceNode.add(Records.of(ArrayUtils.addAll(new String[]{service.getName() }, ports.toArray(new String[0]))));

            g.add(serviceNode);

        }


        for (Service service : serviceModel.getConfig().getServices()) {

            if (handling != null && !service.isPartOfHandling(handling)) {
                continue;
            }

            var currentServiceNode = allServiceNodes.get(service.getName());

            for (Interface anInterface : service.getInterfaces()) {

                if (handling != null && !anInterface.getPartOfHandling().contains(handling)) {
                    continue;
                }

                for (Logic logic : anInterface.getLogic()) {
                    if (logic instanceof HttpServiceCall serviceCall) {
                        var connectedService = allServiceNodes.get(serviceCall.getService());

                        currentServiceNode.addLink(between(port(anInterface.getName()),
                                connectedService.port(serviceCall.getEndpoint(), Compass.WEST)));
                    }

                    if (logic instanceof AmqpServiceCall serviceCall) {

                        var amqpNode = getOrCreateAmqpQueryNode(amqpNodes, serviceCall.getQuery(), g);

                        currentServiceNode.addLink(between(port(anInterface.getName()), amqpNode));
                    }
                }
            }
        }


        return g;
    }

    private static MutableNode getOrCreateAmqpQueryNode(HashMap<String, MutableNode> amqpNodes, String query, MutableGraph g) {
        if (amqpNodes.containsKey(query)) {
            return amqpNodes.get(query);
        }

        var amqpQueryNode = mutNode(query);

        g.add(amqpQueryNode);

        amqpNodes.put(query, amqpQueryNode);

        return amqpQueryNode;
    }
}
