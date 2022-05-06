package org.niels.master.model.logic;

import com.google.gson.*;

import java.lang.reflect.Type;

public class LogicAdapter implements JsonDeserializer<Logic> {
    @Override
    public Logic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        Class<?> klass = null;
        switch (type) {
            case "serviceCall":
                klass = ServiceLogic.class;
                break;
            case "databaseAccess":
                klass = DatabaseLogic.class;
                break;
            default:
                return null;
        }

        return context.deserialize(json, klass);
    }
}
