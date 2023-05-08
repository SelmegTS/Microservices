package com.itm.space.backendresources.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {

    private JsonUtils() {

    }

    public static String serializeToJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(object);
    }

    public static String loadJsonFromFile(String filename) throws IOException {
        return Files.readString(Paths.get("src/test/resources/" + filename));
    }
}
