package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumSelectionRulesTest {
	private static SaintPickContext ctx(int phase, BossBands.Band band, BossBands.Sector sector, int kite, int unseen,
			int sinceOpening, float targetMeter, int deeds) {
		return new SaintPickContext(null, phase, null, band, sector, 0.0D, kite, 0, unseen, sinceOpening, 0, 0.0F,
				targetMeter, 0.0F, deeds, "PROPER", 1);
	}

	private static SaintPickContext ctx(int phase, BossBands.Band band, BossBands.Sector sector) {
		return ctx(phase, band, sector, 0, 0, 0, 0.0F, 0);
	}

	@Test
	void weightsByBandAndSector() {
		assertEquals(30, VelorumMove.STAFF_TOLL.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.FRONT)));
		assertEquals(15, VelorumMove.STAFF_TOLL.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.FLANK)));
		assertEquals(0, VelorumMove.STAFF_TOLL.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.REAR)));
		assertEquals(30, VelorumMove.HANGING_LANTERN.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.REAR)));
		assertEquals(8, VelorumMove.HANGING_LANTERN.weight(ctx(1, BossBands.Band.NEAR, BossBands.Sector.FLANK)));
		assertEquals(0, VelorumMove.HANGING_LANTERN.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.FRONT)));
		assertEquals(8, VelorumMove.CRUOR_RELEASE.weight(ctx(1, BossBands.Band.MID, BossBands.Sector.FLANK)));
		assertEquals(0, VelorumMove.CRUOR_RELEASE.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.FRONT)));
	}

	@Test
	void theFourPressures() {
		assertEquals(26 + 24, VelorumMove.BID_TO_KNEEL.weight(ctx(1, BossBands.Band.FAR, BossBands.Sector.FRONT, 200, 0, 0, 0, 0)));
		assertEquals(26 + 6, VelorumMove.BID_TO_KNEEL.weight(ctx(1, BossBands.Band.FAR, BossBands.Sector.FRONT, 40, 0, 0, 0, 0)));
		assertEquals(0, VelorumMove.BID_TO_KNEEL.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.FRONT, 200, 0, 0, 0, 0)));
		assertEquals(20 + 16, VelorumMove.PROCESSION.weight(ctx(2, BossBands.Band.FAR, BossBands.Sector.REAR, 0, 40, 0, 0, 0)));
		assertEquals(18 + 12, VelorumMove.STILL_FLOOR.weight(ctx(1, BossBands.Band.NEAR, BossBands.Sector.FRONT, 0, 0, 0, 0.6F, 0)));
		assertEquals(6, VelorumMove.LONG_NIGHT.weight(ctx(1, BossBands.Band.MID, BossBands.Sector.FRONT, 0, 0, 99, 0, 0)));
		assertEquals(18, VelorumMove.LONG_NIGHT.weight(ctx(1, BossBands.Band.MID, BossBands.Sector.FRONT, 0, 0, 200, 0, 0)));
		assertEquals(48, VelorumMove.LONG_NIGHT.weight(ctx(1, BossBands.Band.MID, BossBands.Sector.FRONT, 0, 0, 5000, 0, 0)));
		assertEquals(0, VelorumSelectionRules.withPressure(0, 24));
	}

	@Test
	void everyBandHasTwoAnswersInEveryPhase() {
		for (int phase = 1; phase <= 3; phase++) {
			for (BossBands.Band band : BossBands.Band.values()) {
				int answers = 0;
				for (VelorumMove move : VelorumMove.values()) {
					if (!move.picked() || !move.inPhase(phase)) continue;
					SaintPickContext c = ctx(phase, band, BossBands.Sector.FRONT);
					if (move.eligible(c) && move.weight(c) > 0 && move != VelorumMove.LONG_NIGHT) answers++;
				}
				assertTrue(answers >= 2, "phase " + phase + " " + band + " has " + answers);
			}
		}
	}

	@Test
	void aSimulatedFightSpreadsItsPicksAndOpensOften() {
		for (int phase = 1; phase <= 3; phase++) {
			Map<SaintMove, Integer> counts = simulate(phase, 7L * phase, 3000, null);
			int picks = counts.values().stream().mapToInt(Integer::intValue).sum();
			for (Map.Entry<SaintMove, Integer> entry : counts.entrySet()) {
				assertTrue(entry.getValue() <= picks / 3, entry.getKey() + " took " + entry.getValue() + " of " + picks);
			}
			assertTrue(counts.getOrDefault(VelorumMove.LONG_NIGHT, 0) > 0, "an opening comes in phase " + phase);
		}
	}

	@Test
	void anOpeningComesWithinSixHundredTicks() {
		List<Integer> gaps = new ArrayList<>();
		simulate(1, 3L, 3000, gaps);
		assertTrue(!gaps.isEmpty());
		for (int gap : gaps) assertTrue(gap <= 600, "a Long Night came " + gap + " ticks after the last");
	}

	@Test
	void noMoveIsOnATimer() {
		List<Integer> a = new ArrayList<>(), b = new ArrayList<>();
		simulate(1, 11L, 400, a);
		simulate(1, 12L, 400, b);
		assertNotEquals(a, b, "two seeds give two schedules");
	}

	// ── revision 2026-11-06 ──────────────────────────────────────────

	@Test
	void theNewMovesWeightsAndPressures() {
		// Arrested Pulse: NEAR 10, MID 14, FAR 12, any sector; +12 on a target at 40 or more
		assertEquals(14, VelorumMove.ARRESTED_PULSE.weight(ctx(1, BossBands.Band.MID, BossBands.Sector.REAR)));
		assertEquals(0, VelorumMove.ARRESTED_PULSE.weight(ctx(1, BossBands.Band.HUG, BossBands.Sector.FRONT)));
		assertEquals(12 + 12, VelorumMove.ARRESTED_PULSE.weight(ctx(1, BossBands.Band.FAR, BossBands.Sector.FRONT, 0, 0, 0, 0.4F, 0)));
		assertEquals(10, VelorumMove.ARRESTED_PULSE.weight(ctx(1, BossBands.Band.NEAR, BossBands.Sector.FRONT, 0, 0, 0, 0.39F, 0)));
		// Cruor Javelins: MID 18, FAR 24, NEAR 6; FRONT, the flank at half
		assertEquals(24, VelorumMove.CRUOR_JAVELINS.weight(ctx(2, BossBands.Band.FAR, BossBands.Sector.FRONT)));
		assertEquals(9, VelorumMove.CRUOR_JAVELINS.weight(ctx(2, BossBands.Band.MID, BossBands.Sector.FLANK)));
		assertEquals(0, VelorumMove.CRUOR_JAVELINS.weight(ctx(2, BossBands.Band.MID, BossBands.Sector.REAR)));
		// the Shadow Step: FAR from anywhere; HUG and NEAR only from behind him; never MID
		assertEquals(22, VelorumMove.UMBRAL_STEP.weight(ctx(2, BossBands.Band.FAR, BossBands.Sector.FLANK)));
		assertEquals(10, VelorumMove.UMBRAL_STEP.weight(ctx(2, BossBands.Band.HUG, BossBands.Sector.REAR)));
		assertEquals(0, VelorumMove.UMBRAL_STEP.weight(ctx(2, BossBands.Band.NEAR, BossBands.Sector.FRONT)));
		assertEquals(0, VelorumMove.UMBRAL_STEP.weight(ctx(2, BossBands.Band.MID, BossBands.Sector.REAR)));
		assertEquals(16, VelorumSelectionRules.stepFar(60));
		assertEquals(0, VelorumSelectionRules.stepFar(59));
		assertEquals(16, VelorumSelectionRules.stepGrip(true));
		// Gloam Cuts: NEAR 16, MID 20, FAR 8; FLANK 6; x1.3 in phase 3
		assertEquals(20, VelorumMove.GLOAM_CUTS.weight(ctx(2, BossBands.Band.MID, BossBands.Sector.FRONT)));
		assertEquals(26, VelorumMove.GLOAM_CUTS.weight(ctx(3, BossBands.Band.MID, BossBands.Sector.FRONT)));
		assertEquals(6, VelorumMove.GLOAM_CUTS.weight(ctx(2, BossBands.Band.NEAR, BossBands.Sector.FLANK)));
		assertEquals(0, VelorumMove.GLOAM_CUTS.weight(ctx(2, BossBands.Band.HUG, BossBands.Sector.FRONT)));
		// Veins of Frost: needs two feeding figures by the target, +6 a figure past two, at most +12
		assertFalse(VelorumMove.VEINS_OF_FROST.eligible(ctx(2, BossBands.Band.MID, BossBands.Sector.FRONT)));
		assertEquals(0, VelorumSelectionRules.veinFeeders(2));
		assertEquals(6, VelorumSelectionRules.veinFeeders(3));
		assertEquals(12, VelorumSelectionRules.veinFeeders(9));
		assertEquals(16, VelorumMove.VEINS_OF_FROST.weight(ctx(2, BossBands.Band.MID, BossBands.Sector.FRONT)));
		// the Eclipse is state, never weight
		assertFalse(VelorumMove.ECLIPSE.picked());
	}

	@Test
	void theTenebrisShareOfPicksGrowsFromThePhaseOne() {
		double[] share = new double[4];
		for (int phase = 1; phase <= 3; phase++) {
			Map<SaintMove, Integer> counts = simulate(phase, 31L * phase, 4000, null);
			int picks = counts.values().stream().mapToInt(Integer::intValue).sum();
			int tenebris = 0;
			for (SaintMove move : TENEBRIS) tenebris += counts.getOrDefault(move, 0);
			share[phase] = tenebris / (double) picks;
		}
		assertTrue(share[1] < share[2], "phase 2 has more of the dark: " + share[1] + " then " + share[2]);
		assertTrue(share[1] < share[3], "phase 3 has more of the dark: " + share[1] + " then " + share[3]);
	}

	@Test
	void theDarksSimulatedCoverageGrowsFromPhaseOneToThree() {
		double one = simulateDark(1, 41L), two = simulateDark(2, 43L), three = simulateDark(3, 47L);
		assertTrue(one < three, "the dark covers more of the floor in phase 3: " + one + " then " + three);
		assertTrue(one < two, "and in phase 2: " + one + " then " + two);
		assertTrue(three <= VelorumDarkRules.COVERAGE_CAP + 1.0E-9D, "never past the cap outside the Eclipse");
	}

	private static final List<VelorumMove> TENEBRIS = List.of(VelorumMove.LONG_NIGHT, VelorumMove.PROCESSION,
			VelorumMove.UMBRAL_STEP, VelorumMove.GLOAM_CUTS, VelorumMove.ECLIPSE);

	/** A pool on the simulated boss floor: centre, cap and the tick it was born and dies. */
	private record Pool(double x, double z, double cap, int born, int dies) {
	}

	/**
	 * The mean share of a 15 by 15 boss floor the pools cover over a simulated fight: the picker as in {@link #simulate},
	 * each Tenebris pick leaving its pools (the Long Night's at the rank pillars' edges, the others where the fight is),
	 * the oldest fading past the 45% cap.
	 */
	private static double simulateDark(int phase, long seed) {
		Random random = new Random(seed);
		SaintPickRules.History history = new SaintPickRules.History();
		Map<SaintMove, Integer> cooldowns = new HashMap<>();
		List<Pool> pools = new ArrayList<>();
		List<VelorumMove> moves = List.of(VelorumMove.values());
		BossBands.Sector[] sectors = {BossBands.Sector.FRONT, BossBands.Sector.FRONT, BossBands.Sector.FLANK,
				BossBands.Sector.REAR};
		int time = 0, since = 0, samples = 0;
		double covered = 0.0D;
		while (time < 6000) {
			BossBands.Band band = BossBands.Band.values()[random.nextInt(4)];
			SaintPickContext c = ctx(phase, band, sectors[random.nextInt(sectors.length)], 0, 0, since, 0.0F, 0);
			SaintMove pick = SaintPickRules.choose(moves, c, move -> cooldowns.getOrDefault(move, 0), history, random::nextInt);
			if (since >= VelorumDeedRules.openingDue(false) && cooldowns.getOrDefault(VelorumMove.LONG_NIGHT, 0) == 0) {
				pick = VelorumMove.LONG_NIGHT;
			}
			int elapsed = 5;
			if (pick != null) {
				history.record(pick);
				BossMoveTimeline timeline = pick.timeline(c.inputs(0));
				elapsed = timeline.total() + VelorumLocomotionRules.decisionDelay(phase, false);
				int born = time + timeline.windup();
				if (pick == VelorumMove.LONG_NIGHT) {
					double cap = VelorumDarkRules.cap(VelorumDarkRules.Source.LONG_NIGHT, phase);
					int life = VelorumDarkRules.life(VelorumDarkRules.Source.LONG_NIGHT, phase);
					for (int i = 0; i < VelorumDarkRules.LONG_NIGHT_POOLS; i++) {
						double x = SaintChamberLayout.PILLARS[random.nextInt(SaintChamberLayout.PILLARS.length)] - 9.0D;
						pools.add(new Pool(x, random.nextBoolean() ? -0.5D : 15.5D, cap, born, born + life));
					}
				} else if (pick == VelorumMove.GLOAM_CUTS) {
					for (int i = 0; i < 3; i++) pools.add(randomPool(random, VelorumDarkRules.Source.CUT, phase, born + 13));
				} else if (pick == VelorumMove.UMBRAL_STEP) {
					for (int i = 0; i < 2; i++) pools.add(randomPool(random, VelorumDarkRules.Source.STEP, phase, born));
				} else if (pick == VelorumMove.PROCESSION) {
					// the rising pool lives the move's span and 100 more
					Pool pool = randomPool(random, VelorumDarkRules.Source.PROCESSION, phase, time);
					pools.add(new Pool(pool.x(), pool.z(), pool.cap(), time, pool.dies() + timeline.total()));
				}
				final int spent = elapsed;
				cooldowns.replaceAll((move, left) -> Math.max(0, left - spent));
				cooldowns.put(pick, pick.cooldown(c));
				since = pick == VelorumMove.LONG_NIGHT ? 0 : since + elapsed;
			} else {
				final int spent = elapsed;
				cooldowns.replaceAll((move, left) -> Math.max(0, left - spent));
				since += elapsed;
			}
			for (int t = time; t < time + elapsed; t += 10) {
				final int now = t;
				pools.removeIf(pool -> pool.dies() <= now);
				while (VelorumDarkRules.overCap(coverage(pools, now)) && !pools.isEmpty()) pools.remove(0);
				covered += coverage(pools, now);
				samples++;
			}
			time += elapsed;
		}
		return covered / Math.max(1, samples);
	}

	private static Pool randomPool(Random random, VelorumDarkRules.Source source, int phase, int born) {
		return new Pool(random.nextDouble() * 15.0D, random.nextDouble() * 15.0D, VelorumDarkRules.cap(source, phase), born,
				born + VelorumDarkRules.life(source, phase));
	}

	/** The share of the 15 by 15 floor under the pools born by {@code now}, each on its own growth curve. */
	private static double coverage(List<Pool> pools, int now) {
		return VelorumDarkRules.coverage(0.0D, 0.0D, 15.0D, 15.0D, (x, z) -> {
			for (Pool pool : pools) {
				if (pool.born() > now) continue;
				if (Math.hypot(pool.x() - x, pool.z() - z) <= VelorumDarkRules.ownRadius(pool.cap(), now - pool.born())) return true;
			}
			return false;
		});
	}

	/** Runs the picker over random positions, honouring cooldowns, the history and the Long Night's pressure. */
	private static Map<SaintMove, Integer> simulate(int phase, long seed, int steps, List<Integer> longNightGaps) {
		Random random = new Random(seed);
		SaintPickRules.History history = new SaintPickRules.History();
		Map<SaintMove, Integer> cooldowns = new HashMap<>();
		Map<SaintMove, Integer> counts = new HashMap<>();
		int since = 0;
		BossBands.Sector[] sectors = {BossBands.Sector.FRONT, BossBands.Sector.FRONT, BossBands.Sector.FRONT,
				BossBands.Sector.FLANK, BossBands.Sector.REAR};
		List<VelorumMove> moves = List.of(VelorumMove.values());
		for (int step = 0; step < steps; step++) {
			BossBands.Band band = BossBands.Band.values()[random.nextInt(4)];
			SaintPickContext c = ctx(phase, band, sectors[random.nextInt(sectors.length)], 0, 0, since, 0.0F, 0);
			SaintMove pick = SaintPickRules.choose(moves, c, move -> cooldowns.getOrDefault(move, 0), history,
					random::nextInt);
			// the entity's opening pressure: past the due time the Long Night is called, as VelorumEntity does
			if (since >= VelorumDeedRules.openingDue(false) && cooldowns.getOrDefault(VelorumMove.LONG_NIGHT, 0) == 0) {
				pick = VelorumMove.LONG_NIGHT;
			}
			int elapsed;
			if (pick == null) {
				elapsed = 5;
			} else {
				history.record(pick);
				counts.merge(pick, 1, Integer::sum);
				BossMoveTimeline timeline = pick.timeline(c.inputs(0));
				elapsed = timeline.total() + VelorumLocomotionRules.decisionDelay(phase, false);
				final int spent = elapsed;
				cooldowns.replaceAll((move, left) -> Math.max(0, left - spent));
				cooldowns.put(pick, pick.cooldown(c));
				if (pick == VelorumMove.LONG_NIGHT) {
					if (longNightGaps != null) longNightGaps.add(since);
					since = 0;
					continue;
				}
				since += elapsed;
				continue;
			}
			final int spent = elapsed;
			cooldowns.replaceAll((move, left) -> Math.max(0, left - spent));
			since += elapsed;
		}
		return counts;
	}
}
