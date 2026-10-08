package com.vincenthuto.hemomancy.common.circus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CinderBellowsRulesTest {
    @Test void breathHasFourDamagePulsesAndARealRecoveryWindow() {
        assertEquals(CinderBellowsRules.Phase.INHALE, CinderBellowsRules.phase(1));
        assertEquals(CinderBellowsRules.Phase.INHALE, CinderBellowsRules.phase(20));
        assertEquals(CinderBellowsRules.Phase.BREATHE, CinderBellowsRules.phase(21));
        assertEquals(CinderBellowsRules.Phase.RECOVER, CinderBellowsRules.phase(61));
        int pulses = 0;
        for (int tick = 1; tick <= 100; tick++) if (CinderBellowsRules.damagePulse(tick)) pulses++;
        assertEquals(4, pulses);
        assertFalse(CinderBellowsRules.inCone(5.01, 1));
        assertFalse(CinderBellowsRules.inCone(3, .8));
        assertTrue(CinderBellowsRules.inCone(3, 1));
    }
    @Test void jugglerPreservesAggregateAttackDamageAcrossThreeThrows() {
        int throwsInCycle = 0;
        for (int tick = 0; tick < 35; tick++) if (MarrowJugglerRules.throwTick(tick)) throwsInCycle++;
        assertEquals(3, throwsInCycle);
        assertEquals(5.0, MarrowJugglerRules.daggerDamage(5) * throwsInCycle, .0001);
    }
}
