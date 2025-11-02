package com.project.bitpacking.config;

import com.project.bitpacking.benchmark.Benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple JSON parser for configuration files.
 * Avoids external dependencies by implementing basic JSON parsing.
 */
public class SimpleJsonParser {
    private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    /**
     * Parses compression method configurations from JSON.
     */
    public static List<CompressionMethodConfig> parseCompressionMethods(String json) {
        List<CompressionMethodConfig> configs = new ArrayList<>();
        List<Map<String, String>> objects = parseArray(json);

        for (Map<String, String> obj : objects) {
            CompressionMethodConfig config = new CompressionMethodConfig();
            config.setName(obj.getOrDefault("name", ""));
            config.setDisplayName(obj.getOrDefault("displayName", obj.getOrDefault("name", "")));
            config.setClassName(obj.getOrDefault("className", ""));
            config.setDescription(obj.getOrDefault("description", ""));
            configs.add(config);
        }

        return configs;
    }

    /**
     * Parses benchmark configurations from JSON.
     */
    public static List<Benchmark> parseBenchmarks(String json) {
        List<Benchmark> benchmarks = new ArrayList<>();
        List<Map<String, Object>> objects = parseArrayWithArrays(json);

        for (Map<String, Object> obj : objects) {
            Benchmark benchmark = new Benchmark();
            benchmark.setName((String) obj.getOrDefault("name", ""));
            benchmark.setDescription((String) obj.getOrDefault("description", ""));
            benchmark.setGeneratorClass((String) obj.getOrDefault("generatorClass", ""));
            benchmark.setDataFile((String) obj.getOrDefault("dataFile", null));

            @SuppressWarnings("unchecked")
            List<String> params = (List<String>) obj.getOrDefault("parameters", new ArrayList<>());
            benchmark.setParameters(params);

            @SuppressWarnings("unchecked")
            Map<String, String> metadata = (Map<String, String>) obj.getOrDefault("metadata", new HashMap<>());
            benchmark.setMetadata(metadata);

            benchmarks.add(benchmark);
        }

        return benchmarks;
    }

    private static List<Map<String, String>> parseArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("[")) return result;

        int depth = 0;
        int start = 1;
        for (int i = 1; i < json.length() - 1; i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    String objStr = json.substring(start, i + 1);
                    result.add(parseObject(objStr));
                    start = i + 2;
                    while (start < json.length() && (json.charAt(start) == ',' || json.charAt(start) == ' ' || json.charAt(start) == '\n')) {
                        start++;
                    }
                    i = start - 1;
                }
            }
        }

        return result;
    }

    private static List<Map<String, Object>> parseArrayWithArrays(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("[")) return result;

        int depth = 0;
        int start = 1;
        for (int i = 1; i < json.length() - 1; i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    String objStr = json.substring(start, i + 1);
                    result.add(parseObjectWithArrays(objStr));
                    start = i + 2;
                    while (start < json.length() && (json.charAt(start) == ',' || json.charAt(start) == ' ' || json.charAt(start) == '\n')) {
                        start++;
                    }
                    i = start - 1;
                }
            }
        }

        return result;
    }

    private static Map<String, String> parseObject(String json) {
        Map<String, String> result = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{")) return result;

        // Remove outer braces
        json = json.substring(1, json.length() - 1).trim();

        // Split by comma but respect quoted strings
        List<String> pairs = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                // Track quoted strings
                boolean inQuotes = true;
                i++;
                while (i < json.length()) {
                    if (json.charAt(i) == '"' && json.charAt(i - 1) != '\\') {
                        break;
                    }
                    i++;
                }
            } else if (c == ',' && depth == 0) {
                pairs.add(json.substring(start, i).trim());
                start = i + 1;
            }
        }
        if (start < json.length()) {
            pairs.add(json.substring(start).trim());
        }

        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String key = extractString(pair.substring(0, colonIdx).trim());
                String value = extractString(pair.substring(colonIdx + 1).trim());
                if (key != null && value != null) {
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    private static Map<String, Object> parseObjectWithArrays(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{")) return result;

        // Handle null values first
        Pattern nullPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*null");
        Matcher nullMatcher = nullPattern.matcher(json);
        while (nullMatcher.find()) {
            String key = nullMatcher.group(1);
            result.put(key, null);
        }

        // Handle empty objects (like metadata: {})
        Pattern emptyObjectPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{\\s*\\}");
        Matcher emptyObjectMatcher = emptyObjectPattern.matcher(json);
        while (emptyObjectMatcher.find()) {
            String key = emptyObjectMatcher.group(1);
            if (!result.containsKey(key)) {
                result.put(key, new HashMap<String, String>());
            }
        }

        // Parse arrays (handles nested brackets)
        Pattern arrayPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[(.*?)\\]");
        Matcher arrayMatcher = arrayPattern.matcher(json);
        while (arrayMatcher.find()) {
            String key = arrayMatcher.group(1);
            if (result.containsKey(key)) continue;
            String arrayContent = arrayMatcher.group(2);
            List<String> values = new ArrayList<>();
            // Split by comma but respect quoted strings
            String[] items = arrayContent.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String item : items) {
                String cleaned = item.trim();
                if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                    cleaned = cleaned.substring(1, cleaned.length() - 1);
                }
                if (!cleaned.isEmpty()) {
                    values.add(cleaned);
                }
            }
            result.put(key, values);
        }

        // Parse simple string fields (but skip if already in result)
        Pattern stringPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher stringMatcher = stringPattern.matcher(json);
        while (stringMatcher.find()) {
            String key = stringMatcher.group(1);
            if (!result.containsKey(key)) {
                result.put(key, stringMatcher.group(2));
            }
        }

        return result;
    }

    private static String extractString(String jsonValue) {
        jsonValue = jsonValue.trim();
        if (jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) {
            return jsonValue.substring(1, jsonValue.length() - 1);
        }
        return jsonValue;
    }
}

