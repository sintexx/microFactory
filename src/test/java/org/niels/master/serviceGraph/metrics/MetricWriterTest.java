package org.niels.master.serviceGraph.metrics;

import org.junit.jupiter.api.Test;
import org.niels.master.model.ModelReader;
import org.niels.master.serviceGraph.ServiceModel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class MetricWriterTest {

    @Test
    void createWorkbookWithMetrics() throws IOException, URISyntaxException {
        var modelConfig = ModelReader.readModel(new File(MetricWriterTest.class.getClassLoader().getResource("./models/sampleModel.json").toURI()));

        var model = new ServiceModel(modelConfig);

        var writer = new MetricWriter(model);

        writer.createWorkbookWithMetrics();

        writer.writeToFile(new File("test.xlsx"));
    }
}