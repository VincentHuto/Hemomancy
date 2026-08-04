
package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemInquiryResourceValidationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path INQUIRIES = ROOT.resolve("src/main/resources/data/hemomancy/dialogue_inquiry");
    private static final Path LANGUAGE = ROOT.resolve("src/main/resources/assets/hemomancy/lang/en_us.json");

    @Test
    void everyInquiryLineExistsInRuntimeLanguageFile() throws IOException {
        Set<String> languageKeys;
        try (Reader reader = Files.newBufferedReader(LANGUAGE)) {
            languageKeys = JsonParser.parseReader(reader).getAsJsonObject().keySet();
        }
        Map<Path, List<String>> failures = new TreeMap<>();
        try (Stream<Path> files = Files.walk(INQUIRIES)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try {
                    JsonObject root = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
                    List<String> keys = new ArrayList<>();
                    boolean hasLines = root.has("lines");
                    boolean hasConditions = root.has("conditions");
                    if (hasLines == hasConditions) failures.computeIfAbsent(path, ignored -> new ArrayList<>())
                            .add("must contain exactly one of lines or conditions");
                    if (hasLines) collect(root.getAsJsonArray("lines"), keys);
                    if (hasConditions) {
                        JsonArray conditions = root.getAsJsonArray("conditions");
                        for (JsonElement element : conditions) {
                            JsonObject branch = element.getAsJsonObject();
                            if (!branch.has("lines")) failures.computeIfAbsent(path, ignored -> new ArrayList<>())
                                    .add("condition missing lines");
                            else collect(branch.getAsJsonArray("lines"), keys);
                        }
                        if (!conditions.isEmpty() && constrained(conditions.get(conditions.size() - 1).getAsJsonObject())) {
                            failures.computeIfAbsent(path, ignored -> new ArrayList<>()).add("final condition must be catch-all");
                        }
                    }
                    for (String key : keys) if (!languageKeys.contains(key)) {
                        failures.computeIfAbsent(path, ignored -> new ArrayList<>()).add("missing localisation: " + key);
                    }
                } catch (Exception exception) {
                    failures.computeIfAbsent(path, ignored -> new ArrayList<>()).add(exception.getMessage());
                }
            });
        }
        assertTrue(failures.isEmpty(), () -> failures.entrySet().stream()
                .map(entry -> entry.getKey() + " -> " + String.join(", ", entry.getValue()))
                .reduce((a, b) -> a + "\n" + b).orElse(""));
    }

    private static void collect(JsonArray array, List<String> keys) {
        for (JsonElement element : array) if (!element.getAsString().isBlank()) keys.add(element.getAsString());
    }

    private static boolean constrained(JsonObject branch) {
        return branch.entrySet().stream().map(Map.Entry::getKey).anyMatch(key -> !"lines".equals(key));
    }
}
