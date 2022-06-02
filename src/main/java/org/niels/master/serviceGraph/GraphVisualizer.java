package org.niels.master.serviceGraph;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.Compass;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import lombok.AllArgsConstructor;
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
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Optional;

import static guru.nidi.graphviz.attribute.Records.rec;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.port;

public class GraphVisualizer {

    private ServiceModel serviceModel;

    public GraphVisualizer(ServiceModel serviceModel){
        this.serviceModel = serviceModel;
    }


    public void writeAllHandlingGraphs(File output) throws IOException {
        Graphviz.useEngine(new GraphvizCmdLineEngine());

        for (String allHandling : serviceModel.getAllHandlings()) {
            writeHandlingGraphAsSvg(serviceModel, allHandling,
                    output.toPath().resolve(allHandling + ".svg").toFile());
        }

        MutableGraph g = createGraph(serviceModel, null);


        writeHandlingGraphAsSvg(serviceModel, null, output.toPath().resolve("complete.svg").toFile());
    }



    public void writeHandlingGraphAsSvg(ServiceModel serviceModel, String handling, File out) throws IOException {
        MutableGraph g = createGraph(serviceModel, handling);

        Graphviz.fromGraph(g)
                .render(Format.SVG).toFile(out);
    }

    @NotNull
    private MutableGraph createGraph(ServiceModel serviceModel, String handling) {
        MutableGraph g = mutGraph("serviceGraph").setDirected(true);

        g.graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

        var allServiceNodes = new HashMap<String, MutableNode>();

        var amqpNodes = new HashMap<String, MutableNode>();

        for (Service service : serviceModel.getConfig().getServices()) {

            if (handling != null && !service.isPartOfHandling(handling)) {
                continue;
            }

            var serviceNode = mutNode(service.getName());

            serviceNode.attrs().add(Shape.NONE);

            allServiceNodes.put(service.getName(), serviceNode);

            var tableBuilder = new StringBuilder("<table border=\"0\" cellspacing=\"0\">" + System.lineSeparator());

            var serviceColor = "white";

            tableBuilder.append("<tr><td port=\"serviceName\" border=\"1\" bgcolor=\"" + serviceColor + "\"><b>" + service.getName() + "</b></td></tr>" + System.lineSeparator());

            for (Interface anInterface : service.getInterfaces()) {

                var firstHandling= anInterface.getPartOfHandling().stream().findFirst();
                String interfaceColor = "white";
                if (firstHandling.isPresent()) {
                    interfaceColor = ColorUtil.getColorForHandling(firstHandling.get());
                }

                var interfaceSymbol = anInterface.getType().equals(Interface.Type.HTTP) ? "⬡" : "⬖";

                tableBuilder.append("<tr><td port=\"" + anInterface.getName() + "\" border=\"1\" bgcolor=\"" + interfaceColor + "\">" + interfaceSymbol + " " + anInterface.getName() + "</td></tr>" + System.lineSeparator());

                if (anInterface instanceof AmqpInterface amqpInterface) {

                    if (amqpInterface.getPartOfHandling().contains(handling) || handling == null) {
                        var queryNode = getOrCreateAmqpQueryNode(amqpNodes, amqpInterface.getQuery(), g, firstHandling);

                        queryNode.addLink(between(port(anInterface.getName()), serviceNode));
                    }
                }

            }

            tableBuilder.append("</table>");


            serviceNode.setName(Label.html(tableBuilder.toString()));

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

                        if (connectedService == null) {
                            throw new RuntimeException("Connected service " + serviceCall.getService() + " could not be found");
                        }

                        currentServiceNode.addLink(between(port(anInterface.getName()),
                                connectedService.port(serviceCall.getEndpoint(), Compass.WEST)));
                    }

                    if (logic instanceof AmqpServiceCall serviceCall) {

                        var amqpNode = getOrCreateAmqpQueryNode(amqpNodes, serviceCall.getQuery(), g, null);

                        currentServiceNode.addLink(between(port(anInterface.getName()), amqpNode));
                    }
                }
            }
        }

        addLegend(g);

        return g;
    }

    private void addLegend( MutableGraph g ) {
        var legendNode = mutNode("LEGEND");

        legendNode.attrs().add(Shape.NONE);


        var legendHtmlBuilder = new StringBuilder("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">\n" +
                "     <TR>\n" +
                "      <TD COLSPAN=\"2\"><B>Legend</B></TD>\n" +
                "     </TR>");

        legendHtmlBuilder.append("<TR><TD>HTTP</TD><TD>⬡</TD></TR>\n" + System.lineSeparator());
        legendHtmlBuilder.append("<TR><TD>AMQP</TD><TD>⬖</TD></TR>\n" + System.lineSeparator());

        for (String allHandling : this.serviceModel.getAllHandlings()) {
            var color = ColorUtil.getColorForHandling(allHandling);

            legendHtmlBuilder.append("<TR><TD>" + allHandling + "</TD><TD BGCOLOR=\"" + color + "\"></TD></TR>\n" + System.lineSeparator());

        }

        legendHtmlBuilder.append("</TABLE>");

        legendNode.setName(Label.html(legendHtmlBuilder.toString()));

        g.add(legendNode);
    }

    private MutableNode getOrCreateAmqpQueryNode(HashMap<String, MutableNode> amqpNodes, String query, MutableGraph g, Optional<String> firstHandling) {
        if (amqpNodes.containsKey(query)) {
            return amqpNodes.get(query);
        }

        var amqpQueryNode = mutNode(query);

        if (firstHandling != null && firstHandling.isPresent()) {
            amqpQueryNode.attrs().add(Style.FILLED, Color.rgb(ColorUtil.getColorForHandling(firstHandling.get())));
        }

        g.add(amqpQueryNode);

        amqpNodes.put(query, amqpQueryNode);

        return amqpQueryNode;
    }
}
