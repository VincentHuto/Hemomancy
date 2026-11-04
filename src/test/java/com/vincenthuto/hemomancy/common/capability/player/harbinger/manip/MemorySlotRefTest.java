package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemorySlotRefTest {
    @Test
    void legacyNamesRemainManipulationReferences() {
        MemorySlotRef ref = MemorySlotRef.fromStorageKey("blood_absorption");
        assertEquals(MemoryEntryKind.MANIPULATION, ref.kind());
        assertEquals("blood_absorption", ref.id());
        assertEquals("blood_absorption", ref.storageKey());
    }

    @Test
    void thelemicMemoryReferencesUseAnUnambiguousStorageKey() {
        MemorySlotRef ref = MemorySlotRef.thelemicMemory(ThelemicMemory.SANGUINE_FISTS);
        assertEquals(MemoryEntryKind.THELEMIC_MEMORY, ref.kind());
        assertEquals(HematicMemoryExpression.THELEMIC, ref.expression());
        assertEquals("thelemic_memory:sanguine_fists", ref.storageKey());
        assertEquals(ref, MemorySlotRef.fromStorageKey(ref.storageKey()));
    }

    @Test
    void expressionIsRuntimeMetadataAndDoesNotAlterLegacyStorage() {
        MemorySlotRef noetic = MemorySlotRef.manipulation("blood_lance");
        MemorySlotRef thelemic = MemorySlotRef.thelemicMemory(ThelemicMemory.HUSHED_GAIT);

        assertEquals(HematicMemoryExpression.NOETIC, noetic.expression());
        assertEquals(HematicMemoryExpression.THELEMIC, thelemic.expression());
        assertEquals("blood_lance", noetic.storageKey());
        assertEquals("thelemic_memory:hushed_gait", thelemic.storageKey());
        assertEquals("manipulation", noetic.toTag().getString("Kind"));
        assertEquals("thelemic_memory", thelemic.toTag().getString("Kind"));
    }

    @Test
    void nbtRoundTripPreservesKindAndIdentifier() {
        MemorySlotRef expected = MemorySlotRef.thelemicMemory(ThelemicMemory.PREDATORY_EYES);
        CompoundTag tag = expected.toTag();
        assertEquals(expected, MemorySlotRef.fromTag(tag).orElseThrow());
        assertTrue(MemorySlotRef.fromTag(new CompoundTag()).isEmpty());
    }

    @Test
    void mixedLoadoutRetainsOrderAndFallsBackToFirstValidSelection() {
        ManipulationLoadout loadout = ManipulationLoadout.of("Field",
                "missing", List.of("blood_lance", "thelemic_memory:sanguine_fists", "blood_lance"), 0);

        assertEquals(List.of("blood_lance", "thelemic_memory:sanguine_fists"), loadout.manipNames());
        assertEquals("blood_lance", loadout.selectedManipName());
    }

    @Test
    void knownMemorySelectionAndMixedSlotsRoundTrip() {
        KnownManipulations original = new KnownManipulations();
        MemorySlotRef selected = MemorySlotRef.thelemicMemory(ThelemicMemory.HUSHED_GAIT);
        original.setEquippedMemoryRefs(List.of(selected, MemorySlotRef.manipulation("blood_lance")));
        original.setSelectedMemoryRef(selected);

        KnownManipulations restored = new KnownManipulations();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertEquals(selected, restored.getSelectedMemoryRef());
        assertTrue(restored.getEquippedMemoryRefs().contains(selected));
        assertTrue(restored.getEquippedMemoryRefs().contains(MemorySlotRef.manipulation("blood_lance")));
    }
}
