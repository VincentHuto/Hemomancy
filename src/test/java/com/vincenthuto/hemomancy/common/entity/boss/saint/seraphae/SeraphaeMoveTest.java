package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeMoveTest {
    private static SaintMoveInputs inputs(SeraphaeMove move, int phase, int mask) {
        int variant = move.reach() ? SeraphaeMove.laneVariant(10.0D, false) : 0;
        return new SaintMoveInputs(phase, variant, mask, 1);
    }

    @Test void idsAreUniqueKeysLowerSnakeAndEnough() {
        Set<Integer> ids = new HashSet<>();
        for (SeraphaeMove move : SeraphaeMove.values()) {
            assertTrue(ids.add(move.id()), "duplicate id " + move.id());
            assertTrue(move.id() >= 1 && move.id() <= 255);
            assertTrue(move.key().matches("[a-z_]+"), move.key());
            assertEquals(move.name().toLowerCase(java.util.Locale.ROOT), move.key());
        }
        assertTrue(SeraphaeMove.values().length >= 8);
        assertSame(SeraphaeMove.FRACTURE, SeraphaeMove.byId(SeraphaeMove.FRACTURE.id()));
    }

    @Test void everyTimelineMeetsItsKindsFloorAndTheAreaRule() {
        for (int mask : new int[]{0, SaintDeeds.FORCED | 1 << ChapelRules.Kind.REPOSE.ordinal()}) {
            for (SeraphaeMove move : SeraphaeMove.values()) {
                SaintMoveInputs in = inputs(move, 3, mask);
                BossMoveTimeline timeline = move.timeline(in);
                assertTrue(timeline.windup() >= SaintMoves.revealFloor(move.kind()), move + " wind-up");
                assertTrue(timeline.active() > 0 && timeline.recovery() >= 10, move + " recovery");
                if (SaintMoves.kindFor(move.footprint(in)) == SaintMove.Kind.AREA) {
                    assertEquals(SaintMove.Kind.AREA, move.kind(), move + " reaches past 7 or is not attached: AREA");
                }
            }
        }
    }

    @Test void everyAreaFootprintShowsFromItsFirstTickWithoutTheBeholdingDeed() {
        for (SeraphaeMove move : SeraphaeMove.values()) {
            SaintMoveInputs in = inputs(move, 3, 0);
            if (move.kind() == SaintMove.Kind.AREA) assertEquals(0, SaintControllerRules.revealTick(move.telegraph(in),
                    move.timeline(in), move.kind()), move.key());
        }
    }

    @Test void unwatchedHandsHidesTheReachUntilTheAreaFloor() {
        int beholding = SaintDeeds.FORCED | 1 << ChapelRules.Kind.BEHOLDING.ordinal();
        SaintMoveInputs in = inputs(SeraphaeMove.LAYING_ON_OF_HANDS, 1, beholding);
        BossMoveTimeline timeline = SeraphaeMove.LAYING_ON_OF_HANDS.timeline(in);
        int reveal = SaintControllerRules.revealTick(SeraphaeMove.LAYING_ON_OF_HANDS.telegraph(in), timeline,
                SaintMove.Kind.AREA);
        assertEquals(timeline.commitTick() - SaintMoves.AREA_FLOOR, reveal, "clamped to 20 before the commit (B-3)");
    }

    @Test void eachPhaseAddsSomethingNew() {
        Set<SeraphaeMove> one = new HashSet<>(), two = new HashSet<>(), three = new HashSet<>();
        for (SeraphaeMove move : SeraphaeMove.values()) {
            if (!move.picked()) continue;
            if (move.row().phase(1) > 0 && move.inPhase(1)) one.add(move);
            if (move.row().phase(2) > 0 && move.inPhase(2)) two.add(move);
            if (move.row().phase(3) > 0 && move.inPhase(3)) three.add(move);
        }
        assertFalse(one.containsAll(two), "phase II adds a move");
        assertFalse(two.containsAll(three), "phase III adds a move");
        assertTrue(one.contains(SeraphaeMove.LAYING_ON_OF_HANDS));
        assertTrue(three.contains(SeraphaeMove.UNCLOSING_EYE) && three.contains(SeraphaeMove.STRAINING_REACH));
    }

    @Test void theReachCarriesItsLaneInTheVariant() {
        int variant = SeraphaeMove.laneVariant(11.5D, true);
        assertEquals(11.5D, SeraphaeMove.laneLength(variant), 1.0E-6D);
        BossMoveTimeline hasty = SeraphaeMove.LAYING_ON_OF_HANDS.timeline(new SaintMoveInputs(1, variant, 0, 1));
        assertEquals(20, hasty.windup(), "thrown into her hands: the area floor");
        if (SeraphaeMove.LAYING_ON_OF_HANDS.footprint(new SaintMoveInputs(1, variant, 0, 1)) instanceof BossFootprint.Lane lane) {
            assertEquals(11.5D, lane.length(), 1.0E-6D);
            assertEquals(lane.length(), lane.frontSpeed() * SeraphaeLeashRules.REACH_GLIDE_TICKS, 1.0E-6D);
        } else fail("the reach is a lane");
    }

    @Test void theConvulsionIsSpokesNotAJumpRingAndOnlyTheLashIsJumpable() {
        for (SeraphaeMove move : SeraphaeMove.values()) {
            BossFootprint shape = move.footprint(inputs(move, 3, 0));
            assertFalse(shape instanceof BossFootprint.Ring, move + ": one jump ring at most, and hers is none");
            if (move != SeraphaeMove.NERVE_LASH) assertNotEquals(BossFootprint.Height.JUMPABLE, shape.height(), move.key());
        }
        assertEquals(BossFootprint.Height.JUMPABLE, SeraphaeMove.NERVE_LASH.footprint(inputs(SeraphaeMove.NERVE_LASH, 1, 0)).height());
        assertInstanceOf(BossFootprint.Annulus.class, SeraphaeMove.CONVULSION.footprint(inputs(SeraphaeMove.CONVULSION, 2, 0)));
    }

    @Test void onlyTheSaintStartsTheFlood() {
        assertFalse(SeraphaeMove.RADIANCE_FLOOD.picked());
        assertFalse(SeraphaeMove.COUNTER_LASH.picked());
        assertEquals(0, SeraphaeMove.RADIANCE_FLOOD.weight(
                com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext.of(1,
                        com.vincenthuto.hemomancy.common.entity.boss.BossBands.Band.NEAR,
                        com.vincenthuto.hemomancy.common.entity.boss.BossBands.Sector.FRONT)));
    }
}
