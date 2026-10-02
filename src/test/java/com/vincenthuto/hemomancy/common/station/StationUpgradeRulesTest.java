package com.vincenthuto.hemomancy.common.station;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.station.StationUpgradeRules.ClaimState.*;
import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeRulesTest {
    @Test
    void claimGatesApplyInPlayerFacingOrder() {
        assertEquals(CLAIMED, StationUpgradeRules.claimState(7, false, true, false, false, 5));
        assertEquals(INACTIVE_BLOOD, StationUpgradeRules.claimState(7, false, false, true, true, 5));
        assertEquals(DEGREE_TOO_LOW, StationUpgradeRules.claimState(4, true, false, true, true, 5));
        assertEquals(PREVIOUS_TIER_INELIGIBLE, StationUpgradeRules.claimState(7, true, false, false, true, 7));
        assertEquals(USAGE_INCOMPLETE, StationUpgradeRules.claimState(7, true, false, true, false, 7));
        assertEquals(READY, StationUpgradeRules.claimState(5, true, false, true, true, 5));
    }

    @Test
    void tiersOnlyAdvanceOneStepToAtMostTwo() {
        assertTrue(StationUpgradeRules.isNextTier(0, 1));
        assertTrue(StationUpgradeRules.isNextTier(1, 2));
        assertFalse(StationUpgradeRules.isNextTier(0, 2));
        assertFalse(StationUpgradeRules.isNextTier(2, 3));
        assertFalse(StationUpgradeRules.isNextTier(1, 1));
    }
}
