package com.vincenthuto.hemomancy.common.station;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeRecipeResourceTest {
    private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe");
    private static final Path LANG = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");
    private static final Path ITEM_MODELS = Path.of("src/main/resources/assets/hemomancy/models/item");

    @Test
    void everyUpgradeItemHasAShapedRepeatCraftRecipe() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            Path file = RECIPES.resolve(tier.upgradeItemPath() + ".json");
            assertTrue(Files.exists(file), "missing recipe " + file);
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            assertEquals("minecraft:crafting_shaped", json.get("type").getAsString(), file.toString());
            assertEquals(tier.upgradeItem().toString(),
                    json.getAsJsonObject("result").get("id").getAsString(), file.toString());
        }
    }

    @Test
    void scriptoriumItemsHaveNamesAndModels() throws IOException {
        JsonObject lang = JsonParser.parseString(Files.readString(LANG)).getAsJsonObject();
        assertEquals("Rubricator's Quill", lang.get("item.hemomancy.rubricators_quill").getAsString());
        assertEquals("Palimpsest Burin", lang.get("item.hemomancy.palimpsest_burin").getAsString());
        assertTrue(Files.exists(ITEM_MODELS.resolve("rubricators_quill.json")));
        assertTrue(Files.exists(ITEM_MODELS.resolve("palimpsest_burin.json")));
    }

    @Test
    void loomKitsHaveNamesModelsAndSprites() throws IOException {
        JsonObject lang = JsonParser.parseString(Files.readString(LANG)).getAsJsonObject();
        assertEquals("Ganglionic Harness", lang.get("item.hemomancy.ganglionic_harness").getAsString());
        assertEquals("Cortical Crown", lang.get("item.hemomancy.cortical_crown").getAsString());
        for (String kit : new String[]{"ganglionic_harness", "cortical_crown"}) {
            assertTrue(Files.exists(ITEM_MODELS.resolve(kit + ".json")), kit + " model");
            assertTrue(Files.exists(Path.of("src/main/resources/assets/hemomancy/textures/item/" + kit + ".png")), kit + " sprite");
        }
        for (String tier : new String[]{"ganglionic", "cortical"})
            for (String key : new String[]{"title", "prompt", "requirements", "ready", "claimed"})
                assertTrue(lang.has("hemomancy.mnemonist.loom." + tier + "." + key), tier + "." + key);
    }

    @Test
    void loomLessonsTellTheTruthAboutMovingAndPaidWeaves() throws IOException {
        for (String page : new String[]{"ganglionic_weave", "cortical_weave"}) {
            String text = Files.readString(Path.of("src/main/resources/data/hemomancy/books/libersanguinium/manipulations/pages/" + page + ".json"));
            assertFalse(text.contains("keeps its contents"), page + " promises contents survive moving");
            assertTrue(text.contains("stored enzymes are lost"), page + " must warn that moving loses stored enzymes");
            assertTrue(text.contains("Finish or cancel any paid weave"), page + " must say a paid weave blocks the rite");
        }
    }
}
