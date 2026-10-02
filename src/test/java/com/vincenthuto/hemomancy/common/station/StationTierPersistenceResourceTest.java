package com.vincenthuto.hemomancy.common.station;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StationTierPersistenceResourceTest {
    private static final Path LOOT = Path.of("src/main/resources/data/hemomancy/loot_table/blocks");
    private static final Path STATES = Path.of("src/main/resources/assets/hemomancy/blockstates");
    private static final Path BLOCKS = Path.of("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting");

    @Test
    void everyUpgradeableStationCopiesItsStageIntoTheDrop() throws IOException {
        for (UpgradeStation station : UpgradeStation.values()) {
            String loot = Files.readString(LOOT.resolve(station.blockId().getPath() + ".json"));
            assertTrue(loot.contains("minecraft:copy_state") && loot.contains("\"stage\""),
                    station.blockId() + " loot must copy its stage");
        }
    }

    @Test
    void variantBlockstatesNoLongerEnumerateEveryPropertyCombination() throws IOException {
        assertTrue(Files.readString(STATES.resolve("resonant_forge.json")).contains("\"multipart\""));
        assertTrue(Files.readString(STATES.resolve("hematic_armature.json")).contains("\"multipart\""));
    }

    @Test
    void breakingAStationNoLongerRefundsItsUpgradeItems() throws IOException {
        String alembic = Files.readString(BLOCKS.resolve("GhastlyAlembicBlock.java"));
        assertFalse(alembic.contains("hematic_condenser_kit"), "Alembic still refunds its condenser kit");
        assertFalse(alembic.contains("sanguine_athanor_kit"), "Alembic still refunds its athanor kit");
        String armature = Files.readString(BLOCKS.resolve("HematicArmatureBlock.java"));
        assertFalse(armature.contains("new ItemStack(ItemInit.vicars_consecration_kit.get())"));
        assertFalse(armature.contains("new ItemStack(ItemInit.monolithic_cornerstone.get())"));
    }
}
