package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickRules;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeSelectionRulesTest {
    @Test void weightsReadBandSectorAndPhase() {
        var lash = SeraphaeMove.NERVE_LASH.row();
        assertEquals(35, SeraphaeSelectionRules.weight(lash, BossBands.Band.HUG, BossBands.Sector.FRONT, 1));
        assertEquals(0, SeraphaeSelectionRules.weight(lash, BossBands.Band.MID, BossBands.Sector.FRONT, 1));
        assertEquals(11, SeraphaeSelectionRules.weight(lash, BossBands.Band.NEAR, BossBands.Sector.REAR, 1));
        var reach = SeraphaeMove.LAYING_ON_OF_HANDS.row();
        assertEquals(48, SeraphaeSelectionRules.weight(reach, BossBands.Band.MID, BossBands.Sector.FRONT, 3));
        assertEquals(0, SeraphaeSelectionRules.weight(reach, BossBands.Band.MID, BossBands.Sector.REAR, 1));
        assertEquals(0, SeraphaeSelectionRules.weight(SeraphaeMove.WING_GRASP.row(), BossBands.Band.NEAR,
                BossBands.Sector.FLANK, 1), "phase II and on");
    }

    @Test void pressureFeedsTheThreadTheConvulsionAndTheKnot() {
        assertEquals(0, SeraphaeSelectionRules.kiteBonus(19));
        assertEquals(20, SeraphaeSelectionRules.kiteBonus(100));
        assertEquals(40, SeraphaeSelectionRules.kiteBonus(200));
        assertEquals(30, SeraphaeSelectionRules.hugBonus(200));
        assertEquals(0, SeraphaeSelectionRules.turtleBonus(199));
        assertEquals(20, SeraphaeSelectionRules.turtleBonus(200));
    }

    @Test void herDecisionDelayShortensByPhaseAndUnderNoRestGiven() {
        assertEquals(14, SeraphaeSelectionRules.decisionDelay(1, 0));
        assertEquals(10, SeraphaeSelectionRules.decisionDelay(2, 0));
        assertEquals(8, SeraphaeSelectionRules.decisionDelay(3, 0));
        int repose = com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds.FORCED
                | 1 << com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.REPOSE.ordinal();
        assertEquals(4, SeraphaeSelectionRules.decisionDelay(3, repose));
    }

    @Test void sheTargetsTheMostHurtInSightTiesToTheNearest() {
        var a = new SeraphaeSelectionRules.Candidate(1, 0.2F, 5, true);
        var b = new SeraphaeSelectionRules.Candidate(2, 0.6F, 12, true);
        var c = new SeraphaeSelectionRules.Candidate(3, 0.9F, 8, false);
        var d = new SeraphaeSelectionRules.Candidate(4, 0.6F, 6, true);
        assertEquals(4, SeraphaeSelectionRules.target(List.of(a, b, c, d)));
        assertEquals(1, SeraphaeSelectionRules.target(List.of(a, new SeraphaeSelectionRules.Candidate(5, 0.9F, 30, true))),
                "past 24 blocks she does not choose by wound");
        assertEquals(3, SeraphaeSelectionRules.target(List.of(c)), "the nearest when no one is in sight");
        assertEquals(-1, SeraphaeSelectionRules.target(List.of()));
    }

    @Test void noMoveIsPickedTwiceInARowAndCooldownsGateTheRest() {
        var history = new SaintPickRules.History();
        var ctx = SaintPickContext.of(1, BossBands.Band.NEAR, BossBands.Sector.FRONT);
        history.record(SeraphaeMove.NERVE_LASH);
        for (int roll = 0; roll < 200; roll++) {
            final int r = roll;
            var pick = SaintPickRules.choose(Arrays.asList(SeraphaeMove.values()), ctx, move -> 0, history, bound -> r);
            assertNotEquals(SeraphaeMove.NERVE_LASH, pick);
        }
        var none = SaintPickRules.choose(Arrays.asList(SeraphaeMove.values()), ctx, move -> 10,
                new SaintPickRules.History(), bound -> 0);
        assertNull(none, "no move runs on a timer alone: with every move cooling she waits");
    }

    @Test void stringsAreOneLinkDeepAndFollowTheDesignsPairs() {
        assertEquals(SeraphaeMove.COUNTER_LASH, SeraphaeMove.NERVE_LASH.followUps(1).get(0).next());
        assertEquals(SeraphaeMove.AXON_MIRROR, SeraphaeMove.AXON_LANCE.followUps(2).get(0).next());
        assertTrue(SeraphaeMove.AXON_LANCE.followUps(1).isEmpty());
        assertEquals(SeraphaeMove.LAYING_ON_OF_HANDS, SeraphaeMove.WING_GRASP.followUps(2).get(0).next());
        assertEquals(SeraphaeMove.CONVULSION_WIDE, SeraphaeMove.CONVULSION.followUps(3).get(0).next());
        for (SeraphaeMove move : SeraphaeMove.values()) {
            for (var link : move.followUps(3)) assertTrue(link.next().followUps(3).isEmpty() || link.next() == SeraphaeMove.LAYING_ON_OF_HANDS,
                    move + " strings go one link deep");
        }
    }
}
