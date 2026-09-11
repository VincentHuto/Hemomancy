package com.vincenthuto.hemomancy.common.manipulation.family;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerricFamilyTest {
    @Test void defensesShareMagnetismMasteryAndUnlockInOrder() {
        assertEquals("sanguine_magnetism", ManipulationFamilyRegistry.baselineId("ferric_rampart"));
        assertEquals("sanguine_magnetism", ManipulationFamilyRegistry.baselineId("ferric_spikes"));
        assertEquals(1, ManipulationFamilyRegistry.form("ferric_rampart").orElseThrow().requiredLevel());
        assertEquals(2, ManipulationFamilyRegistry.form("ferric_spikes").orElseThrow().requiredLevel());
    }
}
