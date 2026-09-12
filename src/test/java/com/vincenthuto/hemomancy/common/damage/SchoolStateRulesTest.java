package com.vincenthuto.hemomancy.common.damage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SchoolStateRulesTest {
    @Test void pressureCannotOverfillOrAffectBloodlessTargets() {
        assertEquals(1, SchoolStateRules.pressure(0, false));
        assertEquals(3, SchoolStateRules.pressure(3, false));
        assertEquals(0, SchoolStateRules.pressure(0, true));
        assertEquals(6, SchoolStateRules.ruptureDamage(99));
    }

    @Test void rimeCannotBeAcceleratedByRefreshingItsDuration() {
        assertEquals(2, SchoolStateRules.rime(1, 1, 100, 90));
        assertEquals(1, SchoolStateRules.rime(1, 2, 99, 90));
        assertEquals(3, SchoolStateRules.rime(2, 2, 100, 90));
        assertEquals(0.45, SchoolStateRules.rimeSlow(3), 0.0001);
    }

    @Test void necrosisStoresOnlyActualDirectHealthDamageAndCapsItsDebt() {
        assertEquals(2, SchoolStateRules.necrosisDebt(0, 8, true));
        assertEquals(6, SchoolStateRules.necrosisDebt(5, 20, true));
        assertEquals(5, SchoolStateRules.necrosisDebt(5, 20, false));
        assertEquals(5, SchoolStateRules.necrosisDebt(5, -2, true));
        assertEquals(0.25, SchoolStateRules.healingMultiplier(true, true), 0.0001);
        assertEquals(0.75, SchoolStateRules.healingMultiplier(true, false), 0.0001);
    }

    @Test void combustionSpendsRemainingHeatWithoutInventingMoreDamage() {
        assertEquals(0, SchoolStateRules.combustionDamage(0));
        assertEquals(2, SchoolStateRules.combustionDamage(30), 0.0001);
        assertEquals(2, SchoolStateRules.combustionDamage(80, 60, 51));
        assertEquals(1, SchoolStateRules.combustionDamage(79, 60, 51));
        assertEquals(0, SchoolStateRules.combustionDamage(79, 80, 70));
        assertEquals(4, SchoolStateRules.combustionDamage(400));
    }

    @Test void thermalOppositionConsumesTheExistingStateInsteadOfAddingBoth() {
        assertEquals(2, SchoolStateRules.rimeAfterHeat(3));
        assertEquals(0, SchoolStateRules.rimeAfterHeat(1));
        assertFalse(SchoolStateRules.mayIgnite(1, false, false));
        assertFalse(SchoolStateRules.mayIgnite(0, true, false));
        assertFalse(SchoolStateRules.mayIgnite(0, false, true));
        assertTrue(SchoolStateRules.mayIgnite(0, false, false));
    }

    @Test void awarenessKeepsCloseDefenseAndExplicitExposure() {
        assertFalse(SchoolStateRules.canPerceive(true, false, false, false, 25));
        assertTrue(SchoolStateRules.canPerceive(true, false, false, false, 16));
        assertTrue(SchoolStateRules.canPerceive(true, true, true, false, 100));
        assertTrue(SchoolStateRules.canPerceive(true, true, false, true, 100));
        assertFalse(SchoolStateRules.canPerceive(false, true, false, false, 100));
    }
}
