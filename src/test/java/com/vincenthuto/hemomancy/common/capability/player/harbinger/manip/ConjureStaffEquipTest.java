package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConjureStaffEquipTest {
    @Test
    void staffDoesNotConsumeASlotAndCannotBeRemoved() {
        var names = new ArrayList<>(List.of("conjure_staff", "blood_shot"));
        ManipulationEquipHelper.normalizeEquippedNames(names);
        assertEquals(1, ManipulationEquipHelper.countNormalEquippedNames(names));
        assertFalse(ManipulationEquipHelper.unequipNameIfAllowed(names, "conjure_staff"));
        assertTrue(names.contains("conjure_staff"));
    }

    @Test
    void oldLoadoutsRestoreStaffWithoutDisplacingNormalMemories() {
        var names = new ArrayList<>(List.of("blood_shot", "deadly_gaze"));
        ManipulationEquipHelper.normalizeEquippedNames(names);
        assertTrue(names.contains("conjure_staff"));
        assertEquals(2, ManipulationEquipHelper.countNormalEquippedNames(names));
        assertFalse(ManipulationEquipHelper.normalizeEquippedNames(names));
    }
}
