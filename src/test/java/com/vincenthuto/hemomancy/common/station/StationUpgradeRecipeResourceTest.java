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
}
