package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HemorathMoveTimelineTest {
	private static final int BEHOLDING = SaintDeeds.FORCED | 1 << ChapelRules.Kind.BEHOLDING.ordinal();

	@Test
	void everyWindUpKeepsItsFloorInBothPhasesAndUnderBeholding() {
		for (HemorathMove move : HemorathMove.values()) {
			for (int phase = 1; phase <= 2; phase++) {
				for (int deeds : new int[]{0, BEHOLDING}) {
					for (int variant : new int[]{0, HemorathMove.VARIANT_HELD}) {
						BossMoveTimeline timeline = move.timeline(new SaintMoveInputs(phase, variant, deeds, 1));
						assertTrue(timeline.windup() >= SaintMoves.revealFloor(move.kind()), move.key() + " phase " + phase);
						TelegraphSpec spec = move.telegraph(new SaintMoveInputs(phase, variant, deeds, 1));
						int reveal = SaintControllerRules.revealTick(spec, timeline, move.kind());
						assertTrue(timeline.commitTick() - reveal >= SaintMoves.revealFloor(move.kind()) || reveal == 0,
								move.key() + " reveals too late");
					}
				}
			}
		}
	}

	@Test
	void phaseTwoCutsWindUpsByFifteenPercent() {
		BossMoveTimeline one = HemorathMove.IRON_RAKE.timeline(new SaintMoveInputs(1, 0, 0, 1));
		BossMoveTimeline two = HemorathMove.IRON_RAKE.timeline(new SaintMoveInputs(2, 0, 0, 1));
		assertEquals(28, one.windup());
		assertEquals(24, two.windup());
		assertEquals(20, HemorathMove.CHAINED_DEBT.timeline(new SaintMoveInputs(2, 0, 0, 1)).windup(),
				"an area wind-up never drops under 20");
		assertEquals(12, HemorathMove.BACKHAND.timeline(new SaintMoveInputs(2, 0, 0, 1)).windup());
	}

	@Test
	void theBeholdingClawFeintsAndHidesItsTell() {
		assertEquals(22, HemorathMove.RAKING_CLAW.timeline(new SaintMoveInputs(1, HemorathMove.VARIANT_HELD, BEHOLDING, 1)).windup());
		TelegraphSpec hidden = HemorathMove.IRON_RAKE.telegraph(new SaintMoveInputs(1, 0, BEHOLDING, 1));
		BossMoveTimeline rake = HemorathMove.IRON_RAKE.timeline(new SaintMoveInputs(1, 0, BEHOLDING, 1));
		assertEquals(rake.commitTick() - SaintMoves.AREA_FLOOR, SaintControllerRules.revealTick(hidden, rake, SaintMove.Kind.AREA));
		TelegraphSpec shown = HemorathMove.IRON_RAKE.telegraph(new SaintMoveInputs(1, 0, 0, 1));
		assertEquals(0, SaintControllerRules.revealTick(shown, rake, SaintMove.Kind.AREA));
	}

	@Test
	void theForcedTideWaitsOutTheCard() {
		BossMoveTimeline forced = HemorathMove.EXSANGUINATING_TIDE.timeline(new SaintMoveInputs(2, HemorathMove.VARIANT_FORCED, 0, 1));
		BossMoveTimeline normal = HemorathMove.EXSANGUINATING_TIDE.timeline(new SaintMoveInputs(2, 0, 0, 1));
		assertEquals(normal.windup() + SaintPhaseTable.CARD_TICKS, forced.windup());
		assertTrue(SaintControllerRules.revealTick(HemorathMove.EXSANGUINATING_TIDE.telegraph(new SaintMoveInputs(2,
				HemorathMove.VARIANT_FORCED, 0, 1)), forced, SaintMove.Kind.AREA) >= SaintPhaseTable.CARD_TICKS);
	}

	@Test
	void kindsFollowResolutionB2() {
		for (HemorathMove move : List.of(HemorathMove.CROWN_FALL, HemorathMove.GORGE, HemorathMove.CHAINED_DEBT,
				HemorathMove.IRON_RAKE, HemorathMove.MARROW_RUPTURE, HemorathMove.SPIKED_HALO, HemorathMove.IRON_THICKET)) {
			assertEquals(SaintMove.Kind.AREA, move.kind(), move.key());
		}
		for (HemorathMove move : HemorathMove.values()) {
			if (move.kind() == SaintMove.Kind.MELEE) {
				assertEquals(SaintMove.Kind.MELEE, SaintMoves.kindFor(move.footprint(new SaintMoveInputs(1, 0, 0, 1))),
						move.key() + " reaches like an area move");
			}
		}
	}

	@Test
	void openingsAreTheTabledLengthsAndFactors() {
		assertEquals(50, HemorathOpenings.GORGED.ticks());
		assertEquals(40, HemorathOpenings.STRAINED.ticks());
		assertEquals(24, HemorathOpenings.PLANTED.ticks());
		assertEquals(56, HemorathOpenings.HALOLESS.ticks());
		assertEquals(30, HemorathOpenings.CLOTTED.ticks());
		assertEquals(40, HemorathOpenings.WRUNG.ticks());
		assertEquals(30, HemorathOpenings.BROKEN_GRASP.ticks());
		assertEquals(36, HemorathOpenings.TITHE.ticks());
		assertEquals(1.4F, HemorathOpenings.WRUNG.damageTaken());
		assertEquals(1.5F, HemorathOpenings.feed(HemorathOpenings.GORGED.id(), false));
		assertEquals(2.0F, HemorathOpenings.feed(HemorathOpenings.STRAINED.id(), false));
		assertEquals(1.5F, HemorathOpenings.feed(0, true));
		assertEquals(0.0F, HemorathOpenings.feed(HemorathOpenings.PLANTED.id(), false));
		assertEquals(36, HemorathMove.TITHE_RECKONING.baseWindup(), "the Tithe opening lasts its wind-up");
		int haloless = HemorathMove.SPIKED_HALO.timeline(new SaintMoveInputs(2, 0, 0, 1)).active()
				+ HemorathMove.SPIKED_HALO.timeline(new SaintMoveInputs(2, 0, 0, 1)).recovery();
		assertEquals(HemorathOpenings.HALOLESS.ticks(), haloless, "Haloless runs from the throw to the end of the recovery");
	}

	@Test
	void idsAreUniqueAndKeysAreLowerCase() {
		java.util.Set<Integer> ids = new java.util.HashSet<>();
		for (HemorathMove move : HemorathMove.values()) {
			assertTrue(ids.add(move.id()), "duplicate id " + move.id());
			assertTrue(move.id() > 0 && move.id() <= 255);
			assertEquals(move.name().toLowerCase(java.util.Locale.ROOT), move.key());
			assertEquals(move, HemorathMove.byId(move.id()));
		}
	}
}
