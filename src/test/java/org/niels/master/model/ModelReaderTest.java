package org.niels.master.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class ModelReaderTest {

    @Test
    void readModel() throws URISyntaxException, FileNotFoundException {
        var model = ModelReader.readModel(new File(ModelReaderTest.class.getClassLoader().getResource("./models/sampleModel.json").toURI()));

        Assertions.assertEquals(2, model.getServices().size() );
        Assertions.assertEquals(1, model.getDatabaseServers().size());
    }
}