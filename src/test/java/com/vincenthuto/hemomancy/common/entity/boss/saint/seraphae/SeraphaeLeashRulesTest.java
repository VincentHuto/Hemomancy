package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeLeashRulesTest {
    @Test void theChainShortensWithIntegrityAndAnchors() {
        assertEquals(15.8D, SeraphaeLeashRules.length(30.0F, 0, 0), 1.0E-6D);
        assertEquals(9.5D, SeraphaeLeashRules.length(75.0F, 0, 0), 1.0E-6D);
        assertEquals(6.0D, SeraphaeLeashRules.length(100.0F, 0, 0), 1.0E-6D);
        assertEquals(18.8D, SeraphaeLeashRules.length(30.0F, 1, 0), 1.0E-6D);
        assertEquals(13.8D, SeraphaeLeashRules.length(30.0F, 0, 1), 1.0E-6D);
        assertEquals(4.0D, SeraphaeLeashRules.length(100.0F, 0, 2), 1.0E-6D, "never below four");
    }

    @Test void theChainStopsHerWhereTheRayLeavesTheLeash() {
        assertEquals(10.0D, SeraphaeLeashRules.chainStop(0, 0, 1, 0, 0, 0, 10), 1.0E-6D);
        assertEquals(15.0D, SeraphaeLeashRules.chainStop(-5, 0, 1, 0, 0, 0, 10), 1.0E-6D);
        assertEquals(5.0D, SeraphaeLeashRules.chainStop(5, 0, 1, 0, 0, 0, 10), 1.0E-6D);
        assertEquals(0.0D, SeraphaeLeashRules.chainStop(12, 0, 1, 0, 0, 0, 10), 1.0E-6D, "already outside");
    }

    @Test void theReachStopsShortOfATargetBeyondTheChainAndAHand() {
        assertFalse(SeraphaeLeashRules.stoppedShort(6.0D, 10.0D));
        assertFalse(SeraphaeLeashRules.stoppedShort(11.5D, 10.0D), "her hands reach a block and a half past the chain");
        assertTrue(SeraphaeLeashRules.stoppedShort(11.6D, 10.0D));
        assertEquals(7.5D, SeraphaeLeashRules.reachLength(6.0D, 10.0D), 1.0E-6D);
        assertEquals(11.5D, SeraphaeLeashRules.reachLength(20.0D, 10.0D), 1.0E-6D);
        assertEquals(5.1D, SeraphaeLeashRules.glide(6.0D, 10.0D), 1.0E-6D);
        assertEquals(10.0D, SeraphaeLeashRules.glide(20.0D, 10.0D), 1.0E-6D);
    }

    @Test void sheWantsToStandFiveFromHerTargetInsideTheLeash() {
        double[] free = SeraphaeLeashRules.desired(10, 0, 0, 0, 0, 0, 20);
        assertEquals(5.0D, free[0], 1.0E-6D);
        double[] clamped = SeraphaeLeashRules.desired(0, 0, 30, 0, 0, 0, 10);
        assertEquals(10.0D, Math.hypot(clamped[0], clamped[1]), 1.0E-6D);
    }

    @Test void reelingBlocksPicksUntilSheIsWithinABlockOfTheChainsEnd() {
        assertTrue(SeraphaeLeashRules.reeling(10.5D, 10.0D));
        assertFalse(SeraphaeLeashRules.reeling(10.0D, 10.0D));
        assertTrue(SeraphaeLeashRules.mayPickWhileReeled(11.0D, 10.0D));
        assertFalse(SeraphaeLeashRules.mayPickWhileReeled(11.2D, 10.0D));
        assertTrue(SeraphaeLeashRules.straining(12.0D, 10.0D));
        assertFalse(SeraphaeLeashRules.straining(11.0D, 10.0D));
    }
}
