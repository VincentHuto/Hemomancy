package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickRules;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A few thousand simulated decisions per phase with a scripted challenger visiting every band and sector (design
 * section 16): no move takes more than a third of a phase's picks, every band has two or more answers in every phase,
 * and kite and hug pressure move the weights.
 */
final class PutricielSelectionDistributionTest {
	private static final List<PutricielMove> MOVES = List.of(PutricielMove.values());
	private static final int DECISIONS = 4000;
	/** Ticks between picks in the simulation: a move's length plus the decision delay, roughly. */
	private static final int TICKS_PER_DECISION = 45;

	private static SaintPickContext ctx(int phase, BossBands.Band band, BossBands.Sector sector, int kite, int hug,
			int ticksInPhase, float kindling) {
		return new SaintPickContext(null, phase, null, band, sector, 0.0D, kite, hug, 0, 200, ticksInPhase, kindling,
				0.0F, 0.0F, 0, "NONE", 1);
	}

	@Test
	void noMoveTakesMoreThanAThirdOfAPhasesPicks() {
		for (int phase = 1; phase <= 4; phase++) {
			Random random = new Random(phase * 7919L);
			SaintPickRules.History history = new SaintPickRules.History();
			Map<SaintMove, Integer> cooldowns = new HashMap<>();
			Map<SaintMove, Integer> picks = new HashMap<>();
			int total = 0;
			for (int i = 0; i < DECISIONS; i++) {
				BossBands.Band band = BossBands.Band.values()[random.nextInt(4)];
				BossBands.Sector sector = random.nextInt(10) < 6 ? BossBands.Sector.FRONT
						: random.nextInt(2) == 0 ? BossBands.Sector.FLANK : BossBands.Sector.REAR;
				SaintPickContext context = ctx(phase, band, sector, random.nextInt(120), random.nextInt(120),
						i * TICKS_PER_DECISION, random.nextFloat());
				cooldowns.replaceAll((move, left) -> Math.max(0, left - TICKS_PER_DECISION));
				SaintMove pick = SaintPickRules.choose(MOVES, context, move -> cooldowns.getOrDefault(move, 0), history,
						random::nextInt);
				if (pick == null) continue;
				history.record(pick);
				cooldowns.put(pick, pick.cooldown(context));
				picks.merge(pick, 1, Integer::sum);
				total++;
			}
			assertTrue(total > DECISIONS / 2, "phase " + phase + " picks too rarely: " + total);
			for (Map.Entry<SaintMove, Integer> entry : picks.entrySet()) {
				assertTrue(entry.getValue() <= total / 3, "phase " + phase + ": " + entry.getKey() + " took "
						+ entry.getValue() + " of " + total);
			}
		}
	}

	@Test
	void everyBandHasTwoAnswersInEveryPhase() {
		for (int phase = 1; phase <= 4; phase++) {
			Map<BossBands.Band, Integer> answers = new EnumMap<>(BossBands.Band.class);
			for (BossBands.Band band : BossBands.Band.values()) {
				int count = 0;
				for (PutricielMove move : MOVES) {
					if (!move.picked() || !move.inPhase(phase)) continue;
					for (BossBands.Sector sector : BossBands.Sector.values()) {
						SaintPickContext context = ctx(phase, band, sector, 0, 0, 1000, 0.4F);
						if (move.eligible(context) && move.weight(context) > 0) {
							count++;
							break;
						}
					}
				}
				answers.put(band, count);
				assertTrue(count >= 2, "phase " + phase + " " + band + " has " + count + " answers");
			}
		}
	}

	@Test
	void kiteAndHugPressureMoveTheWeights() {
		SaintPickContext still = ctx(1, BossBands.Band.FAR, BossBands.Sector.FRONT, 0, 0, 0, 0.0F);
		SaintPickContext kited = ctx(1, BossBands.Band.FAR, BossBands.Sector.FRONT, 160, 0, 0, 0.0F);
		assertTrue(PutricielMove.SHELF_VOLLEY.weight(kited) > PutricielMove.SHELF_VOLLEY.weight(still));
		SaintPickContext close = ctx(1, BossBands.Band.HUG, BossBands.Sector.REAR, 0, 0, 0, 0.0F);
		SaintPickContext hugged = ctx(1, BossBands.Band.HUG, BossBands.Sector.REAR, 0, 160, 0, 0.0F);
		assertTrue(PutricielMove.SAC_SWELL.weight(hugged) > PutricielMove.SAC_SWELL.weight(close));
	}

	@Test
	void theBloomIsGatedNotTimed() {
		SaintPickContext early = ctx(3, BossBands.Band.MID, BossBands.Sector.FRONT, 0, 0, 899, 0.2F);
		SaintPickContext late = ctx(3, BossBands.Band.MID, BossBands.Sector.FRONT, 0, 0, 900, 0.2F);
		SaintPickContext hot = ctx(3, BossBands.Band.MID, BossBands.Sector.FRONT, 0, 0, 2000, 0.5F);
		assertTrue(!PutricielMove.BLOOM_OF_ROT.eligible(early) && PutricielMove.BLOOM_OF_ROT.eligible(late));
		assertTrue(!PutricielMove.BLOOM_OF_ROT.eligible(hot), "Kindling under half (resolution D-7)");
		assertTrue(PutricielMove.BLOOM_OF_ROT.weight(late) == PutricielMove.BLOOM_WEIGHT);
	}
}
