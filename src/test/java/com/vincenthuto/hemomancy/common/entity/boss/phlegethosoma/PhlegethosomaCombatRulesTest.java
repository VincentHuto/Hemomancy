package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethosomaCombatRulesTest {
    @Test void phasesAdvanceAtThresholdsAndNeverRegress() {
        assertEquals(1, PhlegethosomaCombatRules.phase(1, 120, 120));
        assertEquals(2, PhlegethosomaCombatRules.phase(1, 72, 120));
        assertEquals(3, PhlegethosomaCombatRules.phase(1, 30, 120));
        assertEquals(3, PhlegethosomaCombatRules.phase(3, 100, 120));
        assertEquals(2, PhlegethosomaCombatRules.phase(2, 100, 120));
    }
    @Test void suctionRetainsTelegraphAndRecoveryInRupturedPhase() {
        var normal = PhlegethosomaCombatRules.suction(1);
        var ruptured = PhlegethosomaCombatRules.suction(3);
        assertEquals(30, normal.windup());
        assertEquals(35, normal.active());
        assertEquals(20, ruptured.windup());
        assertEquals(25, ruptured.active());
        assertTrue(ruptured.recovery() >= 20);
        assertEquals(90, PhlegethosomaCombatRules.suctionCooldown(3));
    }
    @Test void interruptionNeedsDirectDamageDuringSuction() {
        assertFalse(PhlegethosomaCombatRules.interrupts(false, true, 7));
        assertFalse(PhlegethosomaCombatRules.interrupts(true, false, 7));
        assertFalse(PhlegethosomaCombatRules.interrupts(true, true, 5.99F));
        assertTrue(PhlegethosomaCombatRules.interrupts(true, true, 6));
    }
    @Test void clotBudgetCannotGrowPastSix() {
        assertEquals(1, PhlegethosomaCombatRules.clotCount(1, 0));
        assertEquals(3, PhlegethosomaCombatRules.clotCount(3, 0));
        assertEquals(1, PhlegethosomaCombatRules.clotCount(3, 5));
        assertEquals(0, PhlegethosomaCombatRules.clotCount(3, 6));
    }
}
