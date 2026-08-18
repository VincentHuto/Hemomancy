package com.vincenthuto.hemomancy.common.recipe;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScarRecipeTierMetadataTest {
    @Test
    void canonicalScarFamiliesUseTheirCanonicalTier() throws Exception {
        Map<Integer, String[]> families = Map.of(
                1, new String[]{"heart", "pyre", "feral", "halo", "blight", "rime", "thorn", "shade"},
                2, new String[]{"marrow", "sol", "flux", "veil", "wither", "glacier", "anvil", "moon"},
                3, new String[]{"phoenix", "corona", "chimera", "transcendence", "oblivion", "descendence", "crucible", "eye"});
        Path root = Path.of("src/main/resources/data/hemomancy/recipe/scar");
        for (var family : families.entrySet()) {
            for (String scar : family.getValue()) {
                String json = Files.readString(root.resolve("scar_" + scar + ".json"));
                assertTrue(json.matches("(?s).*\\\"tier\\\"\\s*:\\s*" + family.getKey() + ".*"), scar);
            }
        }
    }
}
