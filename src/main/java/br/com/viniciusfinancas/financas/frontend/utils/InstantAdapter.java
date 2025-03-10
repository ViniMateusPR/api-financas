package br.com.viniciusfinancas.financas.frontend.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.Date;

public class InstantAdapter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Instant.from(formatter.parse(json.getAsString()));
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }
}