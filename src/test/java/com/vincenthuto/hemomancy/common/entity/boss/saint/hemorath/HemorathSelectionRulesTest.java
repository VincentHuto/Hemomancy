package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands.Band;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands.Sector;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickRules;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HemorathSelectionRulesTest {
	private static final List<HemorathMove> MOVES = List.of(HemorathMove.values());

	@Test
	void everyHugAndNearCellHasTwoAnswersInEveryPhase() {
		for (int phase = 1; phase <= 2; phase++) {
			for (Band band : new Band[]{Band.HUG, Band.NEAR}) {
				for (Sector sector : Sector.values()) {
					for (float f : new float[]{0.1F, 0.5F, 0.8F}) {
						HemorathPickRules.Situation s = HemorathPickRules.Situation.of(phase, band, sector, f);
						long answers = MOVES.stream().filter(m -> m.picked() && HemorathPickRules.weight(m, s) > 0).count();
						assertTrue(answers >= 2, phase + " " + band + " " + sector + " " + f + ": " + answers);
					}
				}
			}
		}
	}

	@Test
	void middleAndFarFrontAlwaysHaveAnAnswer() {
		for (int phase = 1; phase <= 2; phase++) {
			for (Band band : new Band[]{Band.MID, Band.FAR}) {
				HemorathPickRules.Situation s = HemorathPickRules.Situation.of(phase, band, Sector.FRONT, 0.4F);
				long answers = MOVES.stream().filter(m -> m.picked() && HemorathPickRules.weight(m, s) > 0).count();
				assertTrue(answers >= (phase == 1 ? 2 : 3), phase + " " + band + ": " + answers);
			}
		}
	}

	@Test
	void hungerFeedsAndFullnessVents() {
		HemorathPickRules.Situation hollow = HemorathPickRules.Situation.of(1, Band.MID, Sector.FRONT, 0.2F);
		HemorathPickRules.Situation half = HemorathPickRules.Situation.of(1, Band.MID, Sector.FRONT, 0.5F);
		HemorathPickRules.Situation full = HemorathPickRules.Situation.of(1, Band.MID, Sector.FRONT, 0.9F);
		int siphonHalf = HemorathPickRules.weight(HemorathMove.MARROW_SIPHON, half);
		assertEquals(Math.round(siphonHalf * 1.5F), HemorathPickRules.weight(HemorathMove.MARROW_SIPHON, hollow));
		assertEquals(Math.round(siphonHalf * 0.3F), HemorathPickRules.weight(HemorathMove.MARROW_SIPHON, full));
		assertEquals(0, HemorathPickRules.weight(HemorathMove.RIB_BURST, half), "the Rib Burst needs him Brimming");
		HemorathPickRules.Situation brim = HemorathPickRules.Situation.of(1, Band.NEAR, Sector.FRONT, 0.75F);
		HemorathPickRules.Situation over = HemorathPickRules.Situation.of(1, Band.NEAR, Sector.FRONT, 0.9F);
		assertEquals(HemorathPickRules.weight(HemorathMove.RIB_BURST, brim) * 2, HemorathPickRules.weight(HemorathMove.RIB_BURST, over));
	}

	@Test
	void situationalMovesNeedTheirSituation() {
		HemorathPickRules.Situation mid = HemorathPickRules.Situation.of(2, Band.MID, Sector.FRONT, 0.5F);
		assertEquals(0, HemorathPickRules.weight(HemorathMove.WELLS_ANSWER, mid));
		assertEquals(0, HemorathPickRules.weight(HemorathMove.WELLS_ANSWER, mid.withPit(true, 100.0F)));
		assertTrue(HemorathPickRules.weight(HemorathMove.WELLS_ANSWER, mid.withPit(true, 600.0F)) > 0);
		assertEquals(0, HemorathPickRules.weight(HemorathMove.TITHE_RECKONING, mid.withDebt(149.0F)));
		assertEquals(HemorathTitheRules.weight(400.0F), HemorathPickRules.weight(HemorathMove.TITHE_RECKONING, mid.withDebt(400.0F)));
		assertEquals(0, HemorathPickRules.weight(HemorathMove.IRON_THICKET, mid.withPatches(2)));
		assertTrue(HemorathPickRules.weight(HemorathMove.IRON_THICKET, mid.withPatches(3)) > 0);
		assertEquals(0, HemorathPickRules.weight(HemorathMove.GORGE, mid));
		assertTrue(HemorathPickRules.weight(HemorathMove.GORGE, mid.withDeeds(true)) > 0);
		assertEquals(0, HemorathPickRules.weight(HemorathMove.SPIKED_HALO, HemorathPickRules.Situation.of(1, Band.MID, Sector.FRONT, 0.5F)),
				"the halo is a phase-2 move");
	}

	@Test
	void kitingAndHuggingEarnTheirAnswers() {
		HemorathPickRules.Situation mid = HemorathPickRules.Situation.of(1, Band.MID, Sector.FRONT, 0.5F);
		assertEquals(HemorathPickRules.weight(HemorathMove.MARROW_SIPHON, mid) + 24,
				HemorathPickRules.weight(HemorathMove.MARROW_SIPHON, mid.withPressure(200, 0)));
		HemorathPickRules.Situation hug = HemorathPickRules.Situation.of(1, Band.HUG, Sector.FRONT, 0.5F);
		assertEquals(HemorathPickRules.weight(HemorathMove.EMPTY_PULSE, hug) + 9,
				HemorathPickRules.weight(HemorathMove.EMPTY_PULSE, hug.withPressure(0, 60)));
	}

	@Test
	void theLastThirstLeavesOnlyTheRakeAndTheHalo() {
		HemorathPickRules.Situation thirst = HemorathPickRules.Situation.of(2, Band.HUG, Sector.REAR, 0.5F).withThirst(true);
		for (HemorathMove move : MOVES) {
			boolean allowed = move == HemorathMove.IRON_RAKE || move == HemorathMove.SPIKED_HALO;
			assertEquals(allowed, HemorathPickRules.weight(move, thirst) > 0, move.key());
		}
	}

	@Test
	void stringsAreTwoDeepAndFollowUpsAreNotPicked() {
		assertFalse(HemorathMove.BACKHAND.picked());
		assertFalse(HemorathMove.CROWN_FALL.picked());
		assertFalse(HemorathMove.LAST_THIRST.picked());
		assertEquals(HemorathMove.BACKHAND, HemorathMove.RAKING_CLAW.followUps(1).get(0).next());
		assertTrue(HemorathMove.EMPTY_PULSE.followUps(1).isEmpty());
		assertEquals(HemorathMove.MARROW_RUPTURE, HemorathMove.EMPTY_PULSE.followUps(2).get(0).next());
		assertEquals(HemorathMove.RAKING_CLAW, HemorathMove.CHAINED_DEBT.followUps(1).get(0).next());
		assertEquals(HemorathMove.TITHE_RECKONING, HemorathMove.CHAINED_DEBT.followUps(2).get(0).next());
		SaintFollowContext hit = new SaintFollowContext(null, 2, true, Band.MID, Sector.FRONT);
		assertNull(SaintPickRules.followUpLink(HemorathMove.CHAINED_DEBT, hit, SaintPickRules.MAX_CHAIN, false, () -> 0.0D));
		assertNotNull(SaintPickRules.followUpLink(HemorathMove.CHAINED_DEBT, hit, 0, false, () -> 0.0D));
		SaintFollowContext missed = new SaintFollowContext(null, 2, false, Band.MID, Sector.FRONT);
		assertNull(SaintPickRules.followUpLink(HemorathMove.CHAINED_DEBT, missed, 0, false, () -> 0.0D),
				"the chain's string fires only on a hook");
	}

	@Test
	void aLongSimulatedFightLetsNoMoveTakeMoreThanAThird() {
		Random random = new Random(7);
		SaintPickRules.History history = new SaintPickRules.History();
		Map<HemorathMove, Integer> counts = new EnumMap<>(HemorathMove.class);
		Map<HemorathMove, Integer> cooldowns = new EnumMap<>(HemorathMove.class);
		int picks = 0;
		for (int step = 0; step < 4000; step++) {
			Band band = Band.values()[random.nextInt(4)];
			Sector sector = Sector.values()[random.nextInt(3)];
			int phase = step < 2000 ? 1 : 2;
			float f = random.nextFloat();
			HemorathPickRules.Situation s = HemorathPickRules.Situation.of(phase, band, sector, f)
					.withPatches(random.nextInt(10)).withDebt(random.nextInt(600))
					.withPit(random.nextInt(4) == 0, random.nextInt(1500)).withPressure(random.nextInt(200), random.nextInt(200));
			cooldowns.replaceAll((move, left) -> Math.max(0, left - 30));
			HemorathMove pick = null;
			int sum = 0;
			int[] weights = new int[MOVES.size()];
			for (int i = 0; i < MOVES.size(); i++) {
				HemorathMove move = MOVES.get(i);
				if (!move.picked() || cooldowns.getOrDefault(move, 0) > 0) continue;
				weights[i] = Math.round(HemorathPickRules.weight(move, s) * history.repeatFactor(move));
				sum += weights[i];
			}
			if (sum <= 0) continue;
			int roll = random.nextInt(sum);
			for (int i = 0; i < MOVES.size(); i++) {
				roll -= weights[i];
				if (roll < 0) {
					pick = MOVES.get(i);
					break;
				}
			}
			history.record(pick);
			cooldowns.put(pick, pick.cooldownTicks());
			counts.merge(pick, 1, Integer::sum);
			picks++;
		}
		assertTrue(picks > 3000);
		for (Map.Entry<HemorathMove, Integer> entry : counts.entrySet()) {
			assertTrue(entry.getValue() < picks / 3, entry.getKey().key() + " took " + entry.getValue() + " of " + picks);
		}
	}
}
