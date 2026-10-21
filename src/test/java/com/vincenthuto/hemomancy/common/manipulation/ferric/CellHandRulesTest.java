package com.vincenthuto.hemomancy.common.manipulation.ferric;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CellHandRulesTest {
    @Test void modesClassifyAndCarryTheirLengths() {
        assertTrue(CellHandRules.summon(CellHandRules.summonMode(true)));
        assertTrue(CellHandRules.absorption(CellHandRules.summonMode(true)));
        assertFalse(CellHandRules.absorption(CellHandRules.summonMode(false)));
        assertTrue(CellHandRules.using(CellHandRules.useMode(false)));
        assertTrue(CellHandRules.absorption(CellHandRules.useMode(true)));
        assertFalse(CellHandRules.summon(CellHandRules.DISPEL) || CellHandRules.using(CellHandRules.DISPEL));
        assertEquals(CellHandRules.SUMMON_TICKS, CellHandRules.ticks(CellHandRules.SUMMON_PROJECTION));
        assertEquals(CellHandRules.DISPEL_TICKS, CellHandRules.ticks(CellHandRules.DISPEL));
        assertTrue(CellHandRules.USE_TICKS > CellHandRules.USE_REFRESH, "a held hand must never lapse between refreshes");
    }

    @Test void summonEruptsWrapsThenFlashesOnce() {
        assertEquals(1, CellHandRules.erupt(CellHandRules.ERUPT_END), 1e-9);
        assertEquals(0, CellHandRules.wrap(CellHandRules.ERUPT_END), 1e-9);
        assertEquals(1, CellHandRules.wrap(CellHandRules.WRAP_END), 1e-9);
        assertEquals(0, CellHandRules.flash(CellHandRules.WRAP_END - .5), 1e-9);
        assertTrue(CellHandRules.flash((CellHandRules.WRAP_END + CellHandRules.SET_END) / 2.0) > .9);
        assertEquals(0, CellHandRules.flash(CellHandRules.SET_END + .5), 1e-9);
        assertEquals(1, CellHandRules.settle(CellHandRules.SUMMON_TICKS), 1e-9);
    }

    @Test void absorptionRunsTheUsePulseBackwards() {
        for (double time : new double[] { 0, 3.3, 17 })
            assertEquals(1, CellHandRules.usePulse(time, 1, false) + CellHandRules.usePulse(time, 1, true), 1e-9);
    }
}
