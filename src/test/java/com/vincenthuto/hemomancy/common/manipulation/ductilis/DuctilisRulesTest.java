package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DuctilisRulesTest {
    @Test void stormRejectsGapsAndWallsAndPrefersConductiveThenDistanceThenUuid() {
        var near = node(4, 2, false, false);
        var marked = node(3, 7, true, false);
        var sameDistance = node(2, 7, true, false);
        var beyond = node(1, 9, true, false);
        assertEquals(sameDistance, DuctilisRules.nextHop(List.of(near, marked, beyond, sameDistance),
                0, 0, 0, false, false, Set.of(), n -> true));
        assertEquals(near, DuctilisRules.nextHop(List.of(near, marked), 0, 0, 0,
                false, false, Set.of(), n -> !n.conductive()));
        assertNull(DuctilisRules.nextHop(List.of(node(1, 6, false, false)), 0, 0, 0,
                false, false, Set.of(), n -> true));
        assertNull(DuctilisRules.nextHop(List.of(near), 0, 0, 0,
                false, false, Set.of(near.id()), n -> true));
        assertNotNull(DuctilisRules.nextHop(List.of(node(1, 18, false, false)), 0, 0, 0,
                true, false, Set.of(), n -> true));
    }

    @Test void recoveryStartsWhenParalysisEndsAndCannotBeRefreshedDuringLock() {
        assertFalse(DuctilisRules.canParalyze(100, true, 0));
        assertFalse(DuctilisRules.canParalyze(119, false, 120));
        assertTrue(DuctilisRules.canParalyze(120, false, 120));
        assertEquals(20, DuctilisRules.paralysisDuration(60, true));
        assertEquals(60, DuctilisRules.paralysisDuration(60, false));
        assertEquals(10, DuctilisRules.paralysisDuration(10, false));
    }

    @Test void dischargeDeduplicatesDirectArcAndPulseHitsButAllowsLaterPulses() {
        var discharge = new Discharge();
        var victim = UUID.randomUUID();
        assertTrue(discharge.claim(victim, 40));
        assertFalse(discharge.claim(victim, 40));
        assertTrue(discharge.claim(victim, 50));
    }

    @Test void relayAndMarkedArcsShareThreeVictimPulseBudget() {
        var discharge=new Discharge(); discharge.beginPulse(40);
        for (int i=0;i<3;i++) assertTrue(discharge.claim(new UUID(0,i),40));
        discharge.beginPulse(40);
        assertFalse(discharge.claim(new UUID(0,4),40));
        discharge.beginPulse(50);
        assertTrue(discharge.claim(new UUID(0,4),50));
    }

    @Test void finishedChainCannotBranchUntilALaterEnvironmentalPulse() {
        var discharge=new Discharge();
        assertTrue(discharge.claim(new UUID(0,1),40));
        discharge.finishChain(40); discharge.beginPulse(40);
        assertFalse(discharge.claim(new UUID(0,2),40));
        discharge.beginPulse(50);
        assertTrue(discharge.claim(new UUID(0,2),50));
    }

    private static DuctilisRules.Hop node(int id, double x, boolean marked, boolean relay) {
        return new DuctilisRules.Hop(new UUID(0, id), x, 0, 0, marked, relay);
    }
}
