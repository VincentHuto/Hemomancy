package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QliphothBloomIdentityTest {
    @Test
    void matchingCoordinatesInDifferentDimensionsKeepIndependentFruit() {
        QliphothBloomSavedData data = new QliphothBloomSavedData();
        UUID owner = UUID.randomUUID();
        BlockPos center = new BlockPos(12, 70, -4);
        var overworld = new QliphothBloomSavedData.BloomEntry(owner, center, "minecraft:overworld", 3, 100);
        var nether = new QliphothBloomSavedData.BloomEntry(owner, center, "minecraft:the_nether", 3, 100);
        data.addBloom(overworld);
        data.addBloom(nether);

        assertNotEquals(overworld.bloomId(), nether.bloomId());
        data.incrementPomesDropped(overworld);
        data.setPendingPome(overworld, 0);
        data.markPendingPomeClaimed(overworld);
        assertEquals(1, data.getPomesDropped(overworld));
        assertEquals(0, data.getPomesDropped(nether));
        assertTrue(data.hasPendingPome(overworld));
        assertFalse(data.hasPendingPome(nether));

        QliphothBloomSavedData loaded = QliphothBloomSavedData.load(data.save(new CompoundTag(), null), null);
        var loadedOverworld = loaded.getBloomAt(center, "minecraft:overworld");
        var loadedNether = loaded.getBloomAt(center, "minecraft:the_nether");
        assertEquals(overworld.bloomId(), loadedOverworld.bloomId());
        assertEquals(nether.bloomId(), loadedNether.bloomId());
        assertEquals(1, loaded.getPomesDropped(loadedOverworld));
        assertEquals(0, loaded.getPomesDropped(loadedNether));
        assertTrue(loaded.isPendingPomeClaimed(loadedOverworld));
        assertFalse(loaded.hasPendingPome(loadedNether));
    }

    @Test
    void rebuildingAtTheSameSiteStartsANewFruitLifecycle() {
        QliphothBloomSavedData data = new QliphothBloomSavedData();
        UUID owner = UUID.randomUUID();
        BlockPos center = new BlockPos(12, 70, -4);
        var original = new QliphothBloomSavedData.BloomEntry(owner, center, "minecraft:overworld", 3, 100);
        data.addBloom(original);
        data.incrementPomesDropped(original);
        data.removeBloomInChunk(center, original.dimension());

        var replacement = new QliphothBloomSavedData.BloomEntry(owner, center, original.dimension(), 3, 100);
        data.addBloom(replacement);
        assertNotEquals(original.bloomId(), replacement.bloomId());
        assertEquals(0, data.getPomesDropped(replacement));
    }

    @Test
    void legacyBloomRetainsMigrationMarkerAcrossSaves() {
        var original = new QliphothBloomSavedData.BloomEntry(UUID.randomUUID(),
                new BlockPos(8, 70, 8), "minecraft:overworld", 3, 100);
        QliphothBloomSavedData data = new QliphothBloomSavedData();
        data.addBloom(original);
        CompoundTag oldSave = data.save(new CompoundTag(), null);
        ((ListTag) oldSave.get("blooms")).getCompound(0).remove("BloomId");

        var migrated = QliphothBloomSavedData.load(oldSave, null);
        var legacyBloom = migrated.getBloomAt(original.center(), original.dimension());
        assertTrue(legacyBloom.migratesLegacyProgress());
        var reloaded = QliphothBloomSavedData.load(migrated.save(new CompoundTag(), null), null);
        assertTrue(reloaded.getBloomById(legacyBloom.bloomId()).migratesLegacyProgress());

        var replacement = new QliphothBloomSavedData.BloomEntry(original.ownerUUID(),
                original.center(), original.dimension(), 3, 200);
        assertFalse(replacement.migratesLegacyProgress());
    }
}
