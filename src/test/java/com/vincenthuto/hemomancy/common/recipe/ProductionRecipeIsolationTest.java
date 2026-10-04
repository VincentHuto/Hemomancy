package com.vincenthuto.hemomancy.common.recipe;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductionRecipeIsolationTest {
    @Test void distillationFixturesAreAvailableOnlyInGameTestResources() {
        for (String name : new String[]{"test", "test_count"}) {
            String relative = "data/hemomancy/recipe/distillation/" + name + ".json";
            assertFalse(Files.exists(Path.of("src/main/resources", relative)), "Production shortcut: " + name);
            assertTrue(Files.exists(Path.of("src/gameTest/resources", relative)), "Missing XP fixture: " + name);
        }
    }
}
