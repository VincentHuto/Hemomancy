package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryEquipRulesTest {
    @Test
    void firstLearnedThelemicMemoryUsesAFreeSharedSlot() {
        List<String> equipped = new ArrayList<>(List.of("blood_absorption", "blood_projection", "blood_lance"));

        MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipThelemicMemory(
                equipped, ThelemicMemory.SANGUINE_FISTS, 3);

        assertEquals(MemoryEquipRules.AutoEquipResult.ADDED, result);
        assertTrue(equipped.contains("thelemic_memory:sanguine_fists"));
        assertEquals(2, ManipulationEquipHelper.countNormalEquippedNames(equipped));
    }

    @Test
    void sameSectionMemoryReplacesTheEarlierThelemicMemoryWhenSlotsAreFull() {
        List<String> equipped = new ArrayList<>(List.of("blood_absorption", "blood_projection",
                "blood_lance", "thelemic_memory:sanguine_fists", "conjure_axe"));

        MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipThelemicMemory(
                equipped, ThelemicMemory.LABORING_ARMS, 3);

        assertEquals(MemoryEquipRules.AutoEquipResult.REPLACED_SECTION, result);
        assertFalse(equipped.contains("thelemic_memory:sanguine_fists"));
        assertTrue(equipped.contains("thelemic_memory:laboring_arms"));
        assertEquals(3, ManipulationEquipHelper.countNormalEquippedNames(equipped));
    }

    @Test
    void fullUnrelatedLayoutLearnsWithoutChangingEquippedEntries() {
        List<String> equipped = new ArrayList<>(List.of("blood_absorption", "blood_projection",
                "blood_lance", "conjure_axe", "thelemic_memory:coursing_legs"));

        MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipThelemicMemory(
                equipped, ThelemicMemory.SANGUINE_FISTS, 3);

        assertEquals(MemoryEquipRules.AutoEquipResult.FULL, result);
        assertFalse(equipped.contains("thelemic_memory:sanguine_fists"));
    }

    @Test
    void manualEquipUsesTheSameVascularSectionReplacementRule() {
        KnownManipulations known = new KnownManipulations();
        known.getEquippedManipNames().add("thelemic_memory:sanguine_fists");

        assertTrue(known.equipMemory(MemorySlotRef.thelemicMemory(ThelemicMemory.LABORING_ARMS), 3));
        assertFalse(known.getEquippedManipNames().contains("thelemic_memory:sanguine_fists"));
        assertTrue(known.getEquippedManipNames().contains("thelemic_memory:laboring_arms"));
    }

    @Test
    void cyclingAnExpiredMemoryWithNoPreparedReplacementRemovesIt() {
        assertNull(MemoryEquipRules.nextPreparedInSection(
                List.of(), ThelemicMemory.SANGUINE_FISTS, ThelemicMemory.SANGUINE_FISTS.section()));
    }
}
