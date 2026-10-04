package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientModelTextureCoverageTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");

    @Test void inventoryAndCrossbowModelsResolveTheirLocalTextures() throws Exception {
        var missing = new ArrayList<String>();
        for (String id : List.of(
            "barbed_boots",
            "barbed_chestplate",
            "barbed_helm",
            "barbed_leggings",
            "blood_gourd_black",
            "blood_gourd_red",
            "blood_gourd_white",
            "blood_lust_boots",
            "blood_lust_chest",
            "blood_lust_helm",
            "blood_lust_helm_tengu",
            "blood_lust_legs",
            "chitinite_boots",
            "chitinite_chestplate",
            "chitinite_helm",
            "chitinite_leggings",
            "covenant_mantle",
            "curved_horn",
            "dried_gourd",
            "edacious_blood_lust_boots",
            "edacious_blood_lust_chest",
            "edacious_blood_lust_helm",
            "edacious_blood_lust_legs",
            "hemorath_rib",
            "phantasmal_blood_lust_boots",
            "phantasmal_blood_lust_chest",
            "phantasmal_blood_lust_legs",
            "prismatic_boots",
            "prismatic_chestplate",
            "prismatic_helm",
            "prismatic_leggings",
            "sheolic_blood_lust_boots",
            "sheolic_blood_lust_chest",
            "sheolic_blood_lust_helm",
            "sheolic_blood_lust_legs",
            "silent_archon_boots",
            "silent_archon_chestplate",
            "silent_archon_helm",
            "silent_archon_leggings",
            "tome_of_the_unstained",
            "unstained_boots",
            "unstained_chestplate",
            "unstained_helm",
            "unstained_leggings",
            "living_crossbow_firework")) {
            var model = JsonParser.parseString(Files.readString(ASSETS.resolve("models/item/" + id + ".json")))
                    .getAsJsonObject();
            for (var texture : model.getAsJsonObject("textures").entrySet()) {
                String reference = texture.getValue().getAsString();
                if (reference.startsWith("hemomancy:") && !exists(reference))
                    missing.add(id + "/" + texture.getKey() + " -> " + reference);
            }
        }
        assertEquals(List.of(), missing, "Runtime inventory models reference missing authored textures");
    }

    @Test void fieldCaseVialTexturesHaveExplicitAvailableNamespaces() throws Exception {
        var model = JsonParser.parseString(Files.readString(ASSETS.resolve("models/item/phlebotomists_field_case.json")))
                .getAsJsonObject();
        for (String key : List.of("5", "6", "7")) {
            String reference = model.getAsJsonObject("textures").get(key).getAsString();
            assertTrue(reference.startsWith("hemomancy:"), "Custom vial texture needs the Hemomancy namespace: " + reference);
            assertTrue(exists(reference), reference);
        }
    }

    @Test void snowyCapModelsUseTheVanillaSnowTop() throws Exception {
        for (String id : List.of("erythrocytic_dirt_snow", "erythrocytic_dirt_block_snow")) {
            var model = JsonParser.parseString(Files.readString(ASSETS.resolve("models/block/" + id + ".json")))
                    .getAsJsonObject();
            assertEquals("minecraft:block/snow", model.getAsJsonObject("textures").get("top").getAsString(), id);
        }
    }

    @Test void ribParticleIsAvailableInTheBlockAtlas() throws Exception {
        Path definition = Path.of("src/main/resources/assets/minecraft/atlases/blocks.json");
        assertTrue(Files.isRegularFile(definition), "Entity texture needs an explicit block-atlas source");
        var sources = JsonParser.parseString(Files.readString(definition)).getAsJsonObject().getAsJsonArray("sources");
        String particle = JsonParser.parseString(Files.readString(ASSETS.resolve("models/item/hemorath_rib.json")))
                .getAsJsonObject().getAsJsonObject("textures").get("particle").getAsString();
        boolean found = false;
        for (var source : sources) {
            var entry = source.getAsJsonObject();
            if (entry.get("type").getAsString().equals("minecraft:single")
                    && entry.get("resource").getAsString().equals(particle)
                    && entry.get("sprite").getAsString().equals(particle)) found = true;
        }
        assertTrue(found, particle);
    }

    private static boolean exists(String reference) {
        return Files.isRegularFile(ASSETS.resolve("textures/" + reference.substring("hemomancy:".length()) + ".png"));
    }
}
