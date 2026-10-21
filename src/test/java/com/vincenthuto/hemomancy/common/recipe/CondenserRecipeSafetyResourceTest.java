package com.vincenthuto.hemomancy.common.recipe;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** Projecting into a cauldron or Condenser must only ever fill it. */
class CondenserRecipeSafetyResourceTest {
    private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe");

    @Test
    void noInfusionTargetsACauldronOrCondenser() throws IOException {
        for (Path file : json(RECIPES.resolve("blood_infusion"))) {
            String raw = Files.readString(file);
            assertFalse(raw.contains("\"minecraft:cauldron\""), file.toString());
            assertFalse(raw.contains("\"hemomancy:sanguine_condenser\""), file.toString());
        }
    }

    @Test
    void noBloodStructureTargetsACondenser() throws IOException {
        for (Path file : json(RECIPES.resolve("blood_structure")))
            assertFalse(Files.readString(file).contains("\"hemomancy:sanguine_condenser\""), file.toString());
    }

    private static java.util.List<Path> json(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(path -> path.toString().endsWith(".json")).toList();
        }
    }
}
