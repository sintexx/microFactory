package org.niels.master.model.interfaces;

import com.google.gson.*;

import java.lang.reflect.Type;

public class InterfaceAdapter implements JsonDeserializer<Interface> {
    @Override
    public Interface deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        Class<?> klass = null;
        switch (type) {
            case "http":
                klass = HttpInterface.class;
                break;
            case "amqp":
                klass = AmqpInterface.class;
                break;
            default:
                return null;
        }

        return context.deserialize(json, klass);
    }
}
