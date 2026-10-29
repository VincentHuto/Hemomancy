package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeAnchorRulesTest {
    private static final BlockPos TOMB = new BlockPos(100, 64, -40);
    private static final Vec3 HER = Vec3.atBottomCenterOf(TOMB);

    /** The chamber's four fight anchors (R1) and the aisle-end anchors 10.8 out, which must stay asleep. */
    private static List<BlockPos> chamber() {
        return List.of(TOMB.offset(-2, 1, -6), TOMB.offset(-2, 1, 6), TOMB.offset(3, 0, -4), TOMB.offset(3, 0, 4),
                TOMB.offset(-9, 1, -6), TOMB.offset(-9, 1, 6));
    }

    @Test void theChamberWakesItsFourTombAnchorsAndLeavesTheAisleEndsAsleep() {
        var placements = SeraphaeAnchorRules.placements(TOMB, HER, chamber());
        assertEquals(SeraphaeAnchorRules.ANCHOR_COUNT, placements.size());
        assertEquals(List.of(TOMB.offset(3, 0, -4), TOMB.offset(3, 0, 4), TOMB.offset(-2, 1, -6), TOMB.offset(-2, 1, 6)),
                placements.stream().map(SeraphaeAnchorRules.Placement::home).toList());
        for (var placement : placements) {
            assertEquals(Vec3.atBottomCenterOf(placement.home()), placement.position(), "anchor not on its block");
        }
    }

    @Test void theChoiceDoesNotDependOnScanOrder() {
        var shuffled = new ArrayList<>(chamber());
        Collections.reverse(shuffled);
        assertEquals(SeraphaeAnchorRules.wakeable(TOMB, chamber()), SeraphaeAnchorRules.wakeable(TOMB, shuffled));
        // equal distances are broken by height, then by position
        var tied = List.of(TOMB.offset(0, 2, 5), TOMB.offset(5, 0, 0), TOMB.offset(0, 0, -5), TOMB.offset(-5, 0, 0),
                TOMB.offset(0, 0, 5));
        assertEquals(List.of(TOMB.offset(0, 0, -5), TOMB.offset(-5, 0, 0), TOMB.offset(5, 0, 0), TOMB.offset(0, 0, 5)),
                SeraphaeAnchorRules.wakeable(TOMB, tied));
    }

    @Test void reachIsEightAcrossAndFourUpOrDown() {
        assertTrue(SeraphaeAnchorRules.inReach(TOMB, TOMB.offset(8, 4, 0)));
        assertTrue(SeraphaeAnchorRules.inReach(TOMB, TOMB.offset(0, -4, -8)));
        assertFalse(SeraphaeAnchorRules.inReach(TOMB, TOMB.offset(9, 0, 0)));
        assertFalse(SeraphaeAnchorRules.inReach(TOMB, TOMB.offset(6, 0, 6)), "reach is round, not square");
        assertFalse(SeraphaeAnchorRules.inReach(TOMB, TOMB.offset(0, 5, 0)));
        assertFalse(SeraphaeAnchorRules.inReach(TOMB, TOMB.offset(0, -5, 0)));
    }

    @Test void noChamberKeepsTheOldRingRoundHer() {
        Vec3 elsewhere = new Vec3(10.5, 70, 3.25);
        var placements = SeraphaeAnchorRules.placements(BlockPos.containing(elsewhere), elsewhere, List.of());
        assertEquals(4, placements.size());
        for (int slot = 0; slot < 4; slot++) {
            var placement = placements.get(slot);
            assertNull(placement.home());
            assertEquals(70.0, placement.position().y);
            assertEquals(SeraphaeAnchorRules.RING_RADIUS,
                    Math.hypot(placement.position().x - elsewhere.x, placement.position().z - elsewhere.z), 1.0E-9);
        }
        assertEquals(new Vec3(20.5, 70, 3.25), placements.get(0).position());
    }

    @Test void aShortChamberFillsTheRestFromTheRing() {
        var placements = SeraphaeAnchorRules.placements(TOMB, HER, List.of(TOMB.offset(3, 0, 4), TOMB.offset(3, 0, 4)));
        assertEquals(4, placements.size());
        assertEquals(TOMB.offset(3, 0, 4), placements.get(0).home());
        for (int slot = 1; slot < 4; slot++) {
            assertNull(placements.get(slot).home());
            assertEquals(SeraphaeAnchorRules.ringSlot(HER, slot), placements.get(slot).position());
        }
    }

    @Test void neverMoreThanFourWake() {
        var many = new ArrayList<BlockPos>();
        for (int x = -3; x <= 3; x++) many.add(TOMB.offset(x, 0, 2));
        assertEquals(4, SeraphaeAnchorRules.wakeable(TOMB, many).size());
        assertEquals(4, SeraphaeAnchorRules.placements(TOMB, HER, many).size());
    }
}
