package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryEquipRulesTest {
    @Test
    void firstLearnedMuscleMemoryUsesAFreeSharedSlot() {
        List<String> equipped = new ArrayList<>(List.of("blood_absorption", "blood_projection", "blood_lance"));

        MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipMuscleMemory(
                equipped, MuscleMemory.SANGUINE_FISTS, 3);

        assertEquals(MemoryEquipRules.AutoEquipResult.ADDED, result);
        assertTrue(equipped.contains("muscle_memory:sanguine_fists"));
        assertEquals(2, ManipulationEquipHelper.countNormalEquippedNames(equipped));
    }

    @Test
    void sameSectionMemoryReplacesTheEarlierMuscleMemoryWhenSlotsAreFull() {
        List<String> equipped = new ArrayList<>(List.of("blood_absorption", "blood_projection",
                "blood_lance", "muscle_memory:sanguine_fists", "conjure_axe"));

        MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipMuscleMemory(
                equipped, MuscleMemory.LABORING_ARMS, 3);

        assertEquals(MemoryEquipRules.AutoEquipResult.REPLACED_SECTION, result);
        assertFalse(equipped.contains("muscle_memory:sanguine_fists"));
        assertTrue(equipped.contains("muscle_memory:laboring_arms"));
        assertEquals(3, ManipulationEquipHelper.countNormalEquippedNames(equipped));
    }

    @Test
    void fullUnrelatedLayoutLearnsWithoutChangingEquippedEntries() {
        List<String> equipped = new ArrayList<>(List.of("blood_absorption", "blood_projection",
                "blood_lance", "conjure_axe", "muscle_memory:coursing_legs"));

        MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipMuscleMemory(
                equipped, MuscleMemory.SANGUINE_FISTS, 3);

        assertEquals(MemoryEquipRules.AutoEquipResult.FULL, result);
        assertFalse(equipped.contains("muscle_memory:sanguine_fists"));
    }

    @Test
    void manualEquipUsesTheSameVascularSectionReplacementRule() {
        KnownManipulations known = new KnownManipulations();
        known.getEquippedManipNames().add("muscle_memory:sanguine_fists");

        assertTrue(known.equipMemory(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS), 3));
        assertFalse(known.getEquippedManipNames().contains("muscle_memory:sanguine_fists"));
        assertTrue(known.getEquippedManipNames().contains("muscle_memory:laboring_arms"));
    }
}
