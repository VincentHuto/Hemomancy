package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantPositionRules.Band;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantPositionRules.Sector;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantSelectionRules.Context;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantSelectionRules.History;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantSelectionRules.Pressure;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantSelectionRulesTest {
	private static final Context NO_NURSING = new Context(false);
	private static final Context NURSING = new Context(true);

	@Test
	void bandAndSectorThresholds() {
		assertEquals(Band.HUG, MycophantPositionRules.band(3.49));
		assertEquals(Band.NEAR, MycophantPositionRules.band(3.5));
		assertEquals(Band.NEAR, MycophantPositionRules.band(6.99));
		assertEquals(Band.MID, MycophantPositionRules.band(7));
		assertEquals(Band.FAR, MycophantPositionRules.band(13));
		assertEquals(Sector.FRONT, MycophantPositionRules.sector(60));
		assertEquals(Sector.FLANK, MycophantPositionRules.sector(60.01));
		assertEquals(Sector.REAR, MycophantPositionRules.sector(120.01));
		assertEquals(0.0, MycophantPositionRules.degreesFromFacing(0, 0, 0F, 0, 5), 1.0E-6);
		assertEquals(180.0, MycophantPositionRules.degreesFromFacing(0, 0, 0F, 0, -5), 1.0E-6);
		// Yaw 90 faces -x, as MycophantEntity.forward() does.
		assertEquals(0.0, MycophantPositionRules.degreesFromFacing(0, 0, 90F, -5, 0), 1.0E-4);
		assertEquals(90.0, MycophantPositionRules.degreesFromFacing(0, 0, 0F, 5, 0), 1.0E-4);
	}

	@Test
	void everyCellHasTwoAnswersPerPhase() {
		for (int phase = 1; phase <= 2; phase++) {
			for (Band band : Band.values()) {
				for (Sector sector : Sector.values()) {
					int answers = 0;
					for (MycophantAttack attack : MycophantAttack.values()) {
						if (MycophantSelectionRules.weight(attack, phase, band, sector) > 0) answers++;
					}
					assertTrue(answers >= 2, "phase " + phase + " " + band + " " + sector + " has " + answers);
				}
			}
		}
	}

	@Test
	void neverTheSameMoveTwiceInARow() {
		for (int phase = 1; phase <= 2; phase++) {
			for (MycophantAttack attack : MycophantAttack.values()) {
				Band bestBand = null;
				Sector bestSector = null;
				int best = 0;
				for (Band band : Band.values()) {
					for (Sector sector : Sector.values()) {
						int w = MycophantSelectionRules.weight(attack, phase, band, sector);
						if (w > best) {
							best = w;
							bestBand = band;
							bestSector = sector;
						}
					}
				}
				if (best == 0) continue;
				History history = new History();
				history.record(attack);
				Random random = new Random(attack.ordinal() * 31L + phase);
				for (int i = 0; i < 500; i++) {
					MycophantAttack chosen = MycophantSelectionRules.choose(phase, bestBand, bestSector, a -> 0, history,
							new Pressure(), NURSING, sum -> random.nextInt(sum));
					assertNotEquals(attack, chosen, "phase " + phase + " repeated " + attack);
				}
			}
		}
	}

	@Test
	void recentMovesDecay() {
		History history = new History();
		MycophantAttack[] order = {MycophantAttack.PLUCK, MycophantAttack.TAIL_LASH, MycophantAttack.HAND_SWEEP,
				MycophantAttack.SIPHON_SPIT, MycophantAttack.RAKE_STRING};
		for (MycophantAttack attack : order) history.record(attack);
		assertEquals(MycophantAttack.RAKE_STRING, history.last());
		assertEquals(0.0F, history.repeatFactor(MycophantAttack.RAKE_STRING), 1.0E-6F);
		assertEquals(0.4F, history.repeatFactor(MycophantAttack.SIPHON_SPIT), 1.0E-6F);
		assertEquals(0.6F, history.repeatFactor(MycophantAttack.HAND_SWEEP), 1.0E-6F);
		assertEquals(0.8F, history.repeatFactor(MycophantAttack.TAIL_LASH), 1.0E-6F);
		assertEquals(1.0F, history.repeatFactor(MycophantAttack.PLUCK), 1.0E-6F);
		assertEquals(1.0F, history.repeatFactor(MycophantAttack.GAZE), 1.0E-6F);
		assertEquals(MycophantAttack.NONE, new History().last());
	}

	@Test
	void kiteRaisesTheEmbrace() {
		assertEquals(8, MycophantSelectionRules.embraceWeight(0));
		assertEquals(28, MycophantSelectionRules.embraceWeight(100));
		assertEquals(40, MycophantSelectionRules.embraceWeight(400));
		assertEquals(24, MycophantSelectionRules.settleBonus(200));
		assertEquals(0, MycophantSelectionRules.settleBonus(0));

		Pressure pressure = new Pressure();
		for (int i = 0; i < 150; i++) pressure.sample(Band.FAR);
		for (int i = 0; i < 30; i++) pressure.sample(Band.HUG);
		assertEquals(150, pressure.kiteTicks());
		assertEquals(30, pressure.hugTicks());
		for (int i = 0; i < 200; i++) pressure.sample(Band.MID);
		assertEquals(200, pressure.kiteTicks(), "only the last 200 ticks count");
		assertEquals(0, pressure.hugTicks());
		pressure.resetKite();
		assertEquals(0, pressure.kiteTicks());
		pressure.sample(Band.NEAR);
		assertEquals(0, pressure.kiteTicks());
	}

	@Test
	void followUpsStopForAnInterrupt() {
		assertEquals(MycophantAttack.PLUCK, MycophantSelectionRules.followUp(MycophantAttack.CLAW_PIN, 1, true, false,
				Band.NEAR, 0, false, () -> 0.5));
		assertEquals(MycophantAttack.PLUCK, MycophantSelectionRules.followUp(MycophantAttack.CLAW_PIN, 2, true, false,
				Band.NEAR, 1, false, () -> 0.99));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.CLAW_PIN, 1, false, false,
				Band.NEAR, 0, false, () -> 0.0));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.CLAW_PIN, 1, true, false,
				Band.NEAR, 0, true, () -> 0.0));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.CLAW_PIN, 1, true, false,
				Band.NEAR, 2, false, () -> 0.0));

		assertEquals(MycophantAttack.RUSHING_EMBRACE, MycophantSelectionRules.followUp(MycophantAttack.HAND_SWEEP, 1,
				true, false, Band.MID, 0, false, () -> 0.69));
		assertEquals(MycophantAttack.FUSION_EMBRACE, MycophantSelectionRules.followUp(MycophantAttack.HAND_SWEEP, 2,
				true, false, Band.MID, 0, false, () -> 0.69));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.HAND_SWEEP, 1,
				true, false, Band.MID, 0, false, () -> 0.7));
		assertEquals(MycophantAttack.PLUCK, MycophantSelectionRules.followUp(MycophantAttack.WING_SWEEP, 2,
				false, false, Band.NEAR, 0, false, () -> 0.1));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.WING_SWEEP, 2,
				false, false, Band.MID, 0, false, () -> 0.1));
		assertEquals(MycophantAttack.FUSION_EMBRACE, MycophantSelectionRules.followUp(MycophantAttack.LATTICE_NET, 2,
				false, true, Band.MID, 0, false, () -> 0.8));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.LATTICE_NET, 2,
				false, false, Band.MID, 0, false, () -> 0.0));
		assertEquals(MycophantAttack.BROOD_SETTLE, MycophantSelectionRules.followUp(MycophantAttack.RAKE_STRING, 1,
				false, false, Band.HUG, 0, false, () -> 0.2));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.RAKE_STRING, 1,
				false, false, Band.NEAR, 0, false, () -> 0.0));
		assertEquals(MycophantAttack.NONE, MycophantSelectionRules.followUp(MycophantAttack.SIPHON_SPIT, 1,
				true, false, Band.MID, 0, false, () -> 0.0));

		assertEquals(6, MycophantSelectionRules.linkGapTicks(1));
		assertEquals(4, MycophantSelectionRules.linkGapTicks(2));
	}

	@Test
	void variantsByPhase() {
		Set<Integer> phaseOne = new HashSet<>();
		Set<Integer> phaseTwo = new HashSet<>();
		for (int r = 0; r < 20; r++) {
			final int roll = r;
			phaseOne.add(MycophantSelectionRules.rollVariant(MycophantAttack.RAKE_STRING, 1, n -> roll));
			phaseTwo.add(MycophantSelectionRules.rollVariant(MycophantAttack.RAKE_STRING, 2, n -> roll));
		}
		assertEquals(Set.of(0, 1), phaseOne);
		assertEquals(Set.of(1, 2), phaseTwo);
		assertEquals(1, MycophantSelectionRules.rollVariant(MycophantAttack.TAIL_LASH, 2, n -> 0));
		assertEquals(0, MycophantSelectionRules.rollVariant(MycophantAttack.TAIL_LASH, 1, n -> 1));
		assertEquals(0, MycophantSelectionRules.rollVariant(MycophantAttack.GAZE, 2, n -> 1));
	}

	@Test
	void simulatedFightHasVariety() {
		for (int phase = 1; phase <= 2; phase++) {
			Random random = new Random(1024L + phase);
			History history = new History();
			Pressure pressure = new Pressure();
			int[] readyAt = new int[MycophantAttack.values().length];
			Map<MycophantAttack, Integer> counts = new EnumMap<>(MycophantAttack.class);
			int now = 0;
			int total = 0;
			for (Band band : Band.values()) {
				for (Sector sector : Sector.values()) {
					int picks = 0;
					while (picks < 100) {
						for (int t = 0; t < 20; t++) pressure.sample(band);
						now += 20;
						final int tick = now;
						MycophantAttack chosen = MycophantSelectionRules.choose(phase, band, sector,
								a -> Math.max(0, readyAt[a.ordinal()] - tick), history, pressure, NO_NURSING,
								sum -> random.nextInt(sum));
						if (chosen == MycophantAttack.NONE) continue;
						history.record(chosen);
						if (chosen.isEmbrace()) pressure.resetKite();
						readyAt[chosen.ordinal()] = now + MycophantControllerRules.cooldownTicks(chosen, phase);
						counts.merge(chosen, 1, Integer::sum);
						picks++;
						total++;
					}
				}
			}
			for (Map.Entry<MycophantAttack, Integer> entry : counts.entrySet()) {
				double share = entry.getValue() / (double) total;
				assertTrue(share <= 0.33, "phase " + phase + " " + entry.getKey() + " took " + share + " of " + counts);
			}
		}
	}
}
