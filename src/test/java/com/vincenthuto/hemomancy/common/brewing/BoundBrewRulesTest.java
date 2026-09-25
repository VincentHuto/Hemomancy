package com.vincenthuto.hemomancy.common.brewing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundBrewRulesTest {
    @Test
    void bindingStartsWithOneDoseAndRefillStopsAtThree() {
        assertEquals(1, BoundBrewRules.bindDoses());
        assertEquals(1, BoundBrewRules.refillDoses(0));
        assertEquals(3, BoundBrewRules.refillDoses(2));
        assertThrows(IllegalArgumentException.class, () -> BoundBrewRules.refillDoses(3));
    }

    @Test
    void bloodSubstitutionRequiresBothFundsAndWinningRoll() {
        assertFalse(BoundBrewRules.substitutesDose(999, 0.0));
        assertTrue(BoundBrewRules.substitutesDose(1000, 0.199999));
        assertFalse(BoundBrewRules.substitutesDose(1000, 0.2));
    }
}
