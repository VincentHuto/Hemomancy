package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Saint picker: cells, eligibility, cooldowns, the anti-repeat history, pressure and follow-ups. */
final class SaintPickRulesTest {
	private static final List<TestMoves> MOVES = List.of(TestMoves.values());

	private static SaintPickContext at(int phase, BossBands.Band band) {
		return SaintPickContext.of(phase, band, BossBands.Sector.FRONT);
	}

	@Test
	void cellsGiveFullHalfOrNothing() {
		assertEquals(20, SaintPickRules.cell(20, true, true));
		assertEquals(10, SaintPickRules.cell(20, false, true));
		assertEquals(0, SaintPickRules.cell(20, false, false));
	}

	@Test
	void onlyPickedEligibleInPhaseMovesOffCooldownAreChosen() {
		Set<SaintMove> seen = new HashSet<>();
		for (int roll = 0; roll < 200; roll++) {
			int r = roll;
			SaintMove pick = SaintPickRules.choose(MOVES, at(1, BossBands.Band.HUG), move -> 0,
					new SaintPickRules.History(), bound -> r);
			seen.add(pick);
		}
		// HUG: the sweep (20) and the always-weighted return (0) and forced (never picked); the lance answers FAR
		assertEquals(Set.of(TestMoves.SWEEP), seen);
		Set<SaintMove> phaseTwo = new HashSet<>();
		for (int roll = 0; roll < 200; roll++) {
			int r = roll;
			phaseTwo.add(SaintPickRules.choose(MOVES, at(2, BossBands.Band.HUG), move -> 0, new SaintPickRules.History(),
					bound -> r));
		}
		assertEquals(Set.of(TestMoves.SWEEP, TestMoves.SECOND_PHASE), phaseTwo, "a phase adds its moves");
		assertNull(SaintPickRules.choose(MOVES, at(1, BossBands.Band.HUG), move -> move == TestMoves.SWEEP ? 5 : 0,
				new SaintPickRules.History(), bound -> 0), "on cooldown, nothing answers HUG");
	}

	@Test
	void weightsAreSpentInListOrder() {
		// MID: sweep half (10), lance half (15)
		SaintPickRules.History history = new SaintPickRules.History();
		assertEquals(TestMoves.SWEEP, SaintPickRules.choose(MOVES, at(1, BossBands.Band.MID), m -> 0, history, b -> 9));
		assertEquals(TestMoves.LANCE, SaintPickRules.choose(MOVES, at(1, BossBands.Band.MID), m -> 0, history, b -> 10));
		assertEquals(TestMoves.LANCE, SaintPickRules.choose(MOVES, at(1, BossBands.Band.MID), m -> 0, history, b -> 24));
	}

	@Test
	void historyDampsRecentPicks() {
		SaintPickRules.History history = new SaintPickRules.History();
		history.record(TestMoves.SWEEP);
		assertEquals(0.0F, history.repeatFactor(TestMoves.SWEEP));
		history.record(TestMoves.LANCE);
		assertEquals(0.4F, history.repeatFactor(TestMoves.SWEEP));
		history.record(TestMoves.RETURN);
		assertEquals(0.6F, history.repeatFactor(TestMoves.SWEEP));
		history.record(TestMoves.SECOND_PHASE);
		assertEquals(0.8F, history.repeatFactor(TestMoves.SWEEP));
		history.record(TestMoves.FORCED);
		assertEquals(1.0F, history.repeatFactor(TestMoves.SWEEP), "forgotten after four picks");
		assertEquals(TestMoves.FORCED, history.last());
		SaintPickRules.History justPicked = new SaintPickRules.History();
		justPicked.record(TestMoves.SWEEP);
		assertNull(SaintPickRules.choose(MOVES, at(1, BossBands.Band.HUG), m -> 0, justPicked, b -> 0),
				"the last pick never repeats straight away");
	}

	@Test
	void pressureCountsOverATwoHundredTickWindow() {
		SaintPickRules.Pressure pressure = new SaintPickRules.Pressure();
		for (int i = 0; i < 150; i++) pressure.sample(BossBands.Band.FAR, i % 3 != 0);
		assertEquals(150, pressure.kiteTicks());
		assertEquals(50, pressure.unseenTicks());
		for (int i = 0; i < 100; i++) pressure.sample(BossBands.Band.HUG, true);
		assertEquals(100, pressure.kiteTicks(), "the oldest kite samples left the window");
		assertEquals(100, pressure.hugTicks());
		assertEquals(SaintPickRules.Pressure.WINDOW, 200);
		pressure.resetKite();
		assertEquals(0, pressure.kiteTicks());
		assertEquals(100, pressure.hugTicks());
		for (int i = 0; i < 200; i++) pressure.sample(BossBands.Band.NEAR, true);
		assertEquals(0, pressure.hugTicks());
		assertEquals(0, pressure.unseenTicks());
	}

	@Test
	void followUpsIgnoreDampingAndCooldownsUpToTwoDeep() {
		SaintFollowContext hit = new SaintFollowContext(null, 1, true, BossBands.Band.HUG, BossBands.Sector.FRONT);
		SaintFollowContext miss = new SaintFollowContext(null, 1, false, BossBands.Band.HUG, BossBands.Sector.FRONT);
		assertEquals(TestMoves.RETURN, SaintPickRules.followUp(TestMoves.SWEEP, hit, 0, false, () -> 0.99D));
		assertNull(SaintPickRules.followUp(TestMoves.SWEEP, miss, 0, false, () -> 0.0D), "the return needs a hit");
		assertEquals(TestMoves.LANCE, SaintPickRules.followUp(TestMoves.RETURN, miss, 1, false, () -> 0.49D));
		assertNull(SaintPickRules.followUp(TestMoves.RETURN, miss, 1, false, () -> 0.5D), "the chance roll failed");
		assertNull(SaintPickRules.followUp(TestMoves.LANCE, miss, SaintPickRules.MAX_CHAIN, false, () -> 0.0D),
				"a string is at most three moves");
		assertNull(SaintPickRules.followUp(TestMoves.SWEEP, hit, 0, true, () -> 0.0D), "an interrupt cancels the chain");
		SaintFollowUp link = SaintPickRules.followUpLink(TestMoves.SWEEP, hit, 0, false, () -> 0.0D);
		assertEquals(4, link.linkGap());
	}

	@Test
	void followConditionsReadBandsAndHits() {
		SaintFollowContext near = new SaintFollowContext(null, 1, false, BossBands.Band.NEAR, BossBands.Sector.REAR);
		assertTrue(FollowCondition.bands(BossBands.Band.HUG, BossBands.Band.NEAR).test(near));
		assertEquals(false, FollowCondition.bands(BossBands.Band.FAR).test(near));
		assertTrue(FollowCondition.sectors(BossBands.Sector.REAR).and(FollowCondition.ALWAYS).test(near));
		assertEquals(false, FollowCondition.onHit().test(near));
	}
}
