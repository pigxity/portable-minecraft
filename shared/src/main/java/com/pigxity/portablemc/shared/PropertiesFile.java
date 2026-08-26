package com.pigxity.portablemc.shared;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesFile {

    private PropertiesFile() {
    }

    public static Map<String, String> read(Path path) throws IOException {
        return parse(Files.readString(path));
    }

    public static Map<String, String> parse(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));

        Map<String, String> result = new LinkedHashMap<>();

        for (String name : properties.stringPropertyNames()) {
            result.put(name, properties.getProperty(name));
        }

        return result;
    }

    public static void write(Path path, Map<String, String> properties) throws IOException {
        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(path, serialize(properties));
    }

    public static String serialize(Map<String, String> properties) {
        StringBuilder output = new StringBuilder();

        properties.forEach((key, value) ->
                output.append(key)
                        .append('=')
                        .append(value)
                        .append('\n'));

        return output.toString();
    }
}