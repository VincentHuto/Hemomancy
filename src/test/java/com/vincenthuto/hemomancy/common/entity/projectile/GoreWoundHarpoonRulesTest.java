package com.vincenthuto.hemomancy.common.entity.projectile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class GoreWoundHarpoonRulesTest {
    @Test
    void onlyLivingCrossbowShotsReturnAndNeverDuplicateDeathRecovery() {
        assertTrue(GoreWoundHarpoonRules.shouldReturn(true, false, false));
        assertFalse(GoreWoundHarpoonRules.shouldReturn(false, false, false));
        assertFalse(GoreWoundHarpoonRules.shouldReturn(true, true, false));
        assertFalse(GoreWoundHarpoonRules.shouldReturn(true, false, true));
        assertFalse(GoreWoundHarpoonRules.shouldDropOnKill(true, 0.0F));
        assertTrue(GoreWoundHarpoonRules.shouldDropOnKill(false, 0.05F));
        assertFalse(GoreWoundHarpoonRules.shouldDropOnKill(false, 0.11F));
    }

    @Test
    void homingOnlyNudgesNearbyTargetsAlreadyNearTheFlightPath() {
        assertTrue(GoreWoundHarpoonRules.canHome(8.0D, 0.96D));
        assertFalse(GoreWoundHarpoonRules.canHome(13.0D, 1.0D));
        assertFalse(GoreWoundHarpoonRules.canHome(8.0D, 0.7D));
        assertEquals(0.08D, GoreWoundHarpoonRules.HOMING_BLEND, 0.0001D);
    }
}
