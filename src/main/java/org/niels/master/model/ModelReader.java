package org.niels.master.model;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.interfaces.InterfaceAdapter;
import org.niels.master.model.logic.Logic;
import org.niels.master.model.logic.LogicAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ModelReader {
    public static ServiceModelConfig readModel(File modelFile) throws FileNotFoundException {
        var gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Logic.class, new LogicAdapter());
        gsonBuilder.registerTypeAdapter(Interface.class, new InterfaceAdapter());

        var gson = gsonBuilder.create();

        var reader = new JsonReader(new FileReader(modelFile));

        return gson.fromJson(reader, ServiceModelConfig.class);
    }
}
