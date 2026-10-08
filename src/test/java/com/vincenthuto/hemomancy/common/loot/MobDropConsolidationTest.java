package com.vincenthuto.hemomancy.common.loot;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MobDropConsolidationTest {
    private JsonObject json(String path) throws Exception {
        return JsonParser.parseString(Files.readString(Path.of("src/main/resources", path))).getAsJsonObject();
    }

    @Test void acceptedSourcesSupplyTheExistingMaterialIds() throws Exception {
        var sources = Map.ofEntries(
                Map.entry("chiton", "chitinous_husk"), Map.entry("phlegethontic_bombardier", "chitinous_husk"),
                Map.entry("bloody_belly_comb_jelly", "cuttlefish_chromatophores"), Map.entry("luminal_cicada", "cuttlefish_chromatophores"),
                Map.entry("excoriated", "calcified_blood_spine"), Map.entry("venous_strider", "calcified_blood_spine"),
                Map.entry("vampire_bat", "calcified_blood_spine"), Map.entry("peacock_spider", "puppeteering_thread"),
                Map.entry("hemojelly", "puppeteering_thread"), Map.entry("siphonophore", "ganglion_cluster"),
                Map.entry("choir_keeper", "venous_pinion"));
        for (var source : sources.entrySet()) {
            var pools = json("data/hemomancy/loot_table/entities/" + source.getKey() + ".json").getAsJsonArray("pools");
            assertTrue(pools.asList().stream().anyMatch(pool -> pool.toString().contains("hemomancy:" + source.getValue())), source.getKey());
        }
    }

    @Test void displayRenamesDoNotReplaceRegistryIds() throws Exception {
        var lang = json("assets/hemomancy/lang/en_us.json");
        assertEquals("Chromatophores", lang.get("item.hemomancy.cuttlefish_chromatophores").getAsString());
        assertEquals("Enthralling Filament", lang.get("item.hemomancy.puppeteering_thread").getAsString());
    }

    @Test void foodsHaveBothCookingRoutesAndBurningLoot() throws Exception {
        for (String food : new String[]{"pelagic_herring", "venison"}) {
            for (String method : new String[]{"smelting", "smoking"}) {
                var recipe = json("data/hemomancy/recipe/cooked_" + food + "_from_" + method + ".json");
                assertEquals("minecraft:" + method, recipe.get("type").getAsString());
                assertEquals("hemomancy:raw_" + food, recipe.getAsJsonObject("ingredient").get("item").getAsString());
                assertEquals("hemomancy:cooked_" + food, recipe.getAsJsonObject("result").get("id").getAsString());
            }
            String mob = food.equals("venison") ? "crimson_doe" : food;
            var loot = json("data/hemomancy/loot_table/entities/" + mob + ".json").toString();
            assertTrue(loot.contains("minecraft:furnace_smelt"));
            assertTrue(loot.contains("is_on_fire"));
        }
    }

    @Test void surfaceJelliesExcludeColdWaterAndSiphonophoresAreDuctilis() throws Exception {
        var biomes = json("data/hemomancy/tags/worldgen/biome/hemojelly_spawnlist.json").getAsJsonArray("values").toString();
        assertFalse(biomes.contains("cold")); assertFalse(biomes.contains("frozen")); assertFalse(biomes.contains("river"));
        assertTrue(biomes.contains("pelagic_ocean"));
        assertFalse(json("data/hemomancy/worldgen/biome/mycelial_depths.json").getAsJsonObject("spawners").toString().contains("hemomancy:hemojelly"));
        assertEquals("ductilis", json("data/hemomancy/blood_profiles/siphonophore.json").getAsJsonArray("tendencies").get(0).getAsString());
    }

    @Test void spiderHusksRemainRareAndFoodsHaveNativeAssets() throws Exception {
        var pools = json("data/hemomancy/loot_table/entities/peacock_spider.json").getAsJsonArray("pools");
        var husk = pools.asList().stream().map(JsonElement::getAsJsonObject)
                .filter(pool -> pool.getAsJsonArray("entries").toString().contains("chitinous_husk")).findFirst().orElseThrow();
        assertEquals(.1, husk.getAsJsonArray("conditions").get(1).getAsJsonObject().get("chance").getAsDouble());
        assertFalse(husk.getAsJsonArray("entries").get(0).getAsJsonObject().has("functions"));
        for (String food : new String[]{"raw_pelagic_herring", "cooked_pelagic_herring", "raw_venison", "cooked_venison"}) {
            assertEquals("hemomancy:item/" + food, json("assets/hemomancy/models/item/" + food + ".json")
                    .getAsJsonObject("textures").get("layer0").getAsString());
            var texture = javax.imageio.ImageIO.read(Path.of("src/main/resources/assets/hemomancy/textures/item/" + food + ".png").toFile());
            assertEquals(16, texture.getWidth()); assertEquals(16, texture.getHeight()); assertTrue(texture.getColorModel().hasAlpha());
        }
    }
}
