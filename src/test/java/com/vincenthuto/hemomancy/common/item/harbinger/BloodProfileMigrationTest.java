package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.*;

class BloodProfileMigrationTest {
    @Test void shipsExactlyTheApprovedRestrictions() throws Exception {
        var expected = new TreeSet<String>();
        JsonParser.parseString(Files.readString(Path.of("src/test/resources/blood_profiles/restricted_entities.json")))
                .getAsJsonArray().forEach(v -> expected.add(v.getAsString()));
        var actual = new TreeSet<String>();
        var root = Path.of("src/main/resources/data");
        try (var files = Files.walk(root)) {
            for (var path : files.filter(p -> p.toString().replace('\\', '/').contains("/blood_profiles/") && p.toString().endsWith(".json")).toList()) {
                var definition = EntityBloodProfile.parse(JsonParser.parseString(Files.readString(path)).getAsJsonObject());
                if (definition.requiresLivingSyringe()) {
                    var relative = root.relativize(path);
                    actual.add(relative.getName(0) + ":" + path.getFileName().toString().replace(".json", ""));
                }
            }
        }
        assertEquals(expected, actual);
    }

    @Test void preservesEveryShippedMembership() throws Exception {
        var baseline = JsonParser.parseString(Files.readString(Path.of(
                "src/test/resources/blood_profiles/legacy_memberships.json"))).getAsJsonObject();
        for (var entity : baseline.entrySet()) {
            var id = entity.getKey().split(":", 2);
            var path = Path.of("src/main/resources/data", id[0], "blood_profiles", id[1] + ".json");
            assertTrue(Files.exists(path), "Missing profile for " + entity.getKey());
            var actual = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            for (String field : new String[]{"tendencies", "properties"}) {
                var expectedValues = new TreeSet<String>();
                entity.getValue().getAsJsonObject().getAsJsonArray(field).forEach(v -> expectedValues.add(v.getAsString()));
                var actualValues = new TreeSet<String>();
                actual.getAsJsonArray(field).forEach(v -> actualValues.add(v.getAsString()));
                assertEquals(expectedValues, actualValues, entity.getKey() + " " + field);
            }
        }
    }
}
