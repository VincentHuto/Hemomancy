package com.vincenthuto.hemomancy.common.station;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeRiteResourceTest {
    private static final Path RITES = Path.of("src/main/resources/data/hemomancy/recipe");

    @Test
    void everyStationRiteDeclaresItsIdAndSixOfferings() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            JsonObject json = read(tier);
            String name = tier.ritePath();
            assertTrue(json.has("id"), name + " must declare its id");
            assertEquals(tier.rite().toString(), json.get("id").getAsString(), name);
            assertEquals(tier.requiredDegree(), json.get("required_degree").getAsInt(), name);
            assertEquals(tier.riteType(), json.get("riteType").getAsString(), name);
            assertEquals(tier.floor(), json.get("floor").getAsString(), name);
            assertFalse(json.get("rankup").getAsBoolean(), name);
            assertFalse(json.has("result"), name);
            JsonArray braziers = json.getAsJsonArray("brazier_signature");
            int total = braziers.asList().stream()
                    .mapToInt(e -> e.getAsJsonObject().has("count") ? e.getAsJsonObject().get("count").getAsInt() : 1).sum();
            assertEquals(StationUpgradeCatalog.OFFERINGS_PER_RITE, total, name);
            assertEquals(tier.upgradeItem().toString(), braziers.get(0).getAsJsonObject()
                    .getAsJsonObject("ingredient").get("item").getAsString(), name);
            assertEquals(tier.station().blockId().toString(), json.getAsJsonObject("required_structure")
                    .getAsJsonObject("key").getAsJsonObject("S").get("block").getAsString(), name);
        }
    }

    @Test
    void ceremoniesAreStandardAndSigilFree() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            JsonObject ceremony = read(tier).getAsJsonObject("ceremony");
            String name = tier.ritePath();
            assertEquals(tier.anchors(), ceremony.getAsJsonArray("anchors").size(), name);
            assertEquals(0, ceremony.getAsJsonArray("support_sockets").size(), name);
            assertEquals(0, ceremony.getAsJsonArray("waves").size(), name);
            assertEquals("safe_retry", ceremony.get("failure").getAsString(), name);
            assertEquals("faint", ceremony.getAsJsonObject("atmosphere").get("fog").getAsString(), name);
            assertEquals("living_staff", ceremony.get("focus").getAsString(), name);
        }
        assertFalse(Files.exists(RITES.resolve("cardinal_rite/monolithic_script.json")));
    }

    @Test
    void upgradeItemsAreNeverSharedBetweenStations() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            String raw = Files.readString(RITES.resolve(tier.ritePath() + ".json"));
            for (StationUpgradeTier other : StationUpgradeCatalog.all())
                if (other.station() != tier.station())
                    assertFalse(raw.contains("\"" + other.upgradeItem() + "\""),
                            tier.ritePath() + " uses " + other.upgradeItem());
        }
    }

    private static JsonObject read(StationUpgradeTier tier) throws IOException {
        return JsonParser.parseString(Files.readString(RITES.resolve(tier.ritePath() + ".json"))).getAsJsonObject();
    }
}
