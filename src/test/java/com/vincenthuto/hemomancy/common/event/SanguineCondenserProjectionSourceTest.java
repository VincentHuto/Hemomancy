package com.vincenthuto.hemomancy.common.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanguineCondenserProjectionSourceTest {
    private static final Path MAIN = Path.of("src/main/java");
    private static final Path ITEM = MAIN.resolve(
            "com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");

    @Test
    void condenserProjectionRunsAfterStructuresAndBeforeInfusion() throws IOException {
        String source = Files.readString(ITEM);
        int condenser = source.indexOf("SanguineCondenserProjection.tryProject");
        assertTrue(condenser > source.indexOf("BloodStructureFeedManager.feedStructure"),
                "the Condenser branch must not steal blood-structure targets");
        assertTrue(condenser < source.indexOf("BloodInfusionManager.feedBlock"),
                "infusion must never claim a cauldron or Condenser first");
    }

    @Test
    void bareBlocksNoLongerCondenseFormations() throws IOException {
        assertFalse(Files.readString(ITEM).contains("SanguineFormationProjectionHandler"));
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList())
                assertFalse(Files.readString(file).contains("sanguine_formation_projectors"), file.toString());
        }
        assertFalse(Files.exists(Path.of("src/main/resources/data/hemomancy/tags/block/sanguine_formation_projectors.json")));
    }
}
