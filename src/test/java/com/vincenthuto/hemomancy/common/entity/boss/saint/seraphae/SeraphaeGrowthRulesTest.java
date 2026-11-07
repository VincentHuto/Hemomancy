package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeGrowthRulesTest {
    @Test void theWoundedAreHealedFirstAndShedWhileTheWholeGrow() {
        assertEquals(1.0F, SeraphaeGrowthRules.healing(10, 20));
        assertEquals(0.5F, SeraphaeGrowthRules.healing(19.5F, 20));
        assertEquals(0.0F, SeraphaeGrowthRules.healing(20, 20));
        assertEquals(-1, SeraphaeGrowthRules.pulse(true, 2.0D));
        assertEquals(1, SeraphaeGrowthRules.pulse(false, 6.0D));
        assertEquals(2, SeraphaeGrowthRules.pulse(false, 3.0D), "close to her the whole grow twice as fast");
    }

    @Test void stagesSwellKnotAndBurst() {
        assertEquals(SeraphaeGrowthRules.Stage.NONE, SeraphaeGrowthRules.stage(2));
        assertEquals(SeraphaeGrowthRules.Stage.SWELLING, SeraphaeGrowthRules.stage(3));
        assertEquals(SeraphaeGrowthRules.Stage.SWELLING, SeraphaeGrowthRules.stage(5));
        assertEquals(SeraphaeGrowthRules.Stage.KNOTTING, SeraphaeGrowthRules.stage(6));
        assertEquals(SeraphaeGrowthRules.Stage.KNOTTING, SeraphaeGrowthRules.stage(9));
        assertEquals(SeraphaeGrowthRules.Stage.BURST, SeraphaeGrowthRules.stage(10));
        assertEquals(6.0F, SeraphaeGrowthRules.burstDamage(2));
        assertEquals(8.0F, SeraphaeGrowthRules.burstDamage(3));
    }

    @Test void everyTwoDamageShedsOneAndTheRestCarries() {
        float[] shed = SeraphaeGrowthRules.shedByDamage(6, 0.0F, 5.0F);
        assertEquals(4.0F, shed[0]);
        assertEquals(1.0F, shed[1], 1.0E-6F);
        shed = SeraphaeGrowthRules.shedByDamage(4, 1.0F, 1.0F);
        assertEquals(3.0F, shed[0]);
        assertEquals(0.0F, shed[1], 1.0E-6F);
        assertEquals(0.0F, SeraphaeGrowthRules.shedByDamage(1, 0.0F, 10.0F)[0], "never below zero");
    }

    @Test void noRestGivenQuickensHerPresence() {
        assertEquals(20, SeraphaeGrowthRules.cadence(false));
        assertEquals(14, SeraphaeGrowthRules.cadence(true));
    }

    @Test void flagsCarryTheStageTheHoldAndTheFeed() {
        assertEquals(0, SeraphaeGrowthRules.flags(1, false, false));
        assertEquals(SeraphaeGrowthRules.FLAG_SWELLING, SeraphaeGrowthRules.flags(4, false, false));
        int knotted = SeraphaeGrowthRules.flags(7, true, true);
        assertTrue((knotted & SeraphaeGrowthRules.FLAG_KNOTTING) != 0 && (knotted & SeraphaeGrowthRules.FLAG_SWELLING) != 0);
        assertTrue((knotted & SeraphaeGrowthRules.FLAG_HELD) != 0 && (knotted & SeraphaeGrowthRules.FLAG_FEEDING) != 0);
        assertTrue(knotted < (1 << 16), "per-player flags are bits 0..15");
    }

    @Test void theHoldHealsThenGrowsFasterLate() {
        assertEquals(2, SeraphaeGrowthRules.holdGrowth(2));
        assertEquals(3, SeraphaeGrowthRules.holdGrowth(3));
        // a whole player in a phase II hold bursts on its fifth pulse, at about tick 50
        int growth = 0, pulses = 0;
        while (growth < SeraphaeGrowthRules.MAX) {
            growth += SeraphaeGrowthRules.holdGrowth(2);
            pulses++;
        }
        assertEquals(50, pulses * SeraphaeGrowthRules.HOLD_PULSE);
    }
}
