package org.niels.master.serviceGraph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.niels.master.model.ModelReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceModelTest {
    @Test
    void readModel() throws URISyntaxException, FileNotFoundException {
        var modelConfig = ModelReader.readModel(new File(ServiceModelTest.class.getClassLoader().getResource("./models/sampleModel.json").toURI()));

        var model = new ServiceModel(modelConfig);

        Assertions.assertEquals(2, model.getServiceGraph().nodes().size());
    }
}