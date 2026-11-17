package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** LP-1: no recipe makes a Sanguine Formation. Formations come from blood that sets. */
class FormationSourceResourceTest {
    private static final String FORMATION = "hemomancy:sanguine_formation";

    @Test
    void noRecipeMakesASanguineFormation() throws IOException {
        List<String> makers = new ArrayList<>();
        try (Stream<Path> files = Files.walk(RepoPaths.DATA.resolve("recipe"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonElement root = JsonParser.parseString(SourceAssertions.read(file));
                if (root.isJsonObject() && makesFormation(root.getAsJsonObject().get("result")))
                    makers.add(RepoPaths.display(file));
            }
        }
        assertEquals(List.of(), makers, "recipes whose result is a Sanguine Formation");
    }

    private static boolean makesFormation(JsonElement result) {
        if (result == null || result.isJsonNull()) return false;
        if (result.isJsonPrimitive()) return FORMATION.equals(result.getAsString());
        if (result.isJsonArray()) {
            for (JsonElement entry : result.getAsJsonArray())
                if (makesFormation(entry)) return true;
            return false;
        }
        JsonObject object = result.getAsJsonObject();
        return makesFormation(object.get("id")) || makesFormation(object.get("item"));
    }
}
