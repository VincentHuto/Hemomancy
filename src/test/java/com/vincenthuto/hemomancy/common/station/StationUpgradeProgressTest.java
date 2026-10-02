package com.vincenthuto.hemomancy.common.station;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeProgressTest {
    @Test
    void usageClaimsAndPendingSurviveSerialization() {
        StationUpgradeProgress progress = new StationUpgradeProgress();
        progress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
        progress.markClaimed(UpgradeStation.ARMATURE, 1, true);
        progress.markClaimed(UpgradeStation.ARMATURE, 2, false);
        progress.setLegacyMigrated();
        CompoundTag tag = progress.serializeNBT(null);

        StationUpgradeProgress restored = new StationUpgradeProgress();
        restored.deserializeNBT(null, tag);
        assertTrue(restored.hasUsed(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT));
        assertFalse(restored.hasUsed(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT_TARGETED));
        assertTrue(restored.hasClaimed(UpgradeStation.ARMATURE, 1));
        assertTrue(restored.hasClaimed(UpgradeStation.ARMATURE, 2));
        assertFalse(restored.hasClaimed(UpgradeStation.ALEMBIC, 1));
        assertTrue(restored.hasPending(UpgradeStation.ARMATURE, 1));
        assertFalse(restored.hasPending(UpgradeStation.ARMATURE, 2));
        assertTrue(restored.legacyMigrated());
    }
}
