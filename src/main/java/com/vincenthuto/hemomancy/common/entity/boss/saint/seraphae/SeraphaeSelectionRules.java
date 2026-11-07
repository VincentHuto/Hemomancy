package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;

import java.util.List;

/**
 * How Seraphae chooses (design section 7, "Picking"): each move's weight by band, times its sector and phase factors,
 * plus pressure (kiting feeds the Transfusion Thread and the Straining Reach, hugging the Convulsion, turtling past her
 * reach the Regrowth Knot and Healed Ground); her decision delay by phase; and her target, the most hurt challenger,
 * because she means to heal. Pure, so it is JVM-tested.
 */
public final class SeraphaeSelectionRules {
	public static final int PRESSURE_STEP_TICKS = 20;
	public static final int KITE_PER_STEP = 4;
	public static final int KITE_CAP = 40;
	public static final int HUG_PER_STEP = 4;
	public static final int HUG_CAP = 30;
	public static final int TURTLE_TICKS = 200;
	public static final int TURTLE_BONUS = 20;
	public static final int[] DECISION_DELAY = {14, 10, 8};
	public static final double TARGET_RANGE = 24.0D;

	/** One row of the weight table: band weights, sector factors, phase factors and cooldowns. */
	public record Row(int hug, int near, int mid, int far, float front, float flank, float rear, float phaseOne,
			float phaseTwo, float phaseThree, int cooldown, int lateCooldown) {
		public static Row any(int weight, float phaseOne, float phaseTwo, float phaseThree, int cooldown) {
			return new Row(weight, weight, weight, weight, 1.0F, 1.0F, 1.0F, phaseOne, phaseTwo, phaseThree, cooldown,
					cooldown);
		}

		public int band(BossBands.Band band) {
			return switch (band) {
				case HUG -> hug;
				case NEAR -> near;
				case MID -> mid;
				case FAR -> far;
			};
		}

		public float sector(BossBands.Sector sector) {
			return switch (sector) {
				case FRONT -> front;
				case FLANK -> flank;
				case REAR -> rear;
			};
		}

		public float phase(int phase) {
			return phase <= 1 ? phaseOne : phase == 2 ? phaseTwo : phaseThree;
		}

		public int cooldown(int phase) {
			return phase >= 3 ? lateCooldown : cooldown;
		}
	}

	/** One challenger as the target rule sees them. */
	public record Candidate(int id, float missingFraction, double distance, boolean seen) {
	}

	private SeraphaeSelectionRules() {
	}

	/** A move's weight before pressure; 0 means it is not picked here. */
	public static int weight(Row row, BossBands.Band band, BossBands.Sector sector, int phase) {
		int base = row.band(band);
		if (base <= 0) return 0;
		return Math.round(base * row.sector(sector) * row.phase(phase));
	}

	/** Kiting: 4 for every 20 ticks the quarry spent at range in the last 200, at most 40. */
	public static int kiteBonus(int kiteTicks) {
		return Math.min(KITE_CAP, kiteTicks / PRESSURE_STEP_TICKS * KITE_PER_STEP);
	}

	/** Hugging: 4 for every 20 ticks in HUG in the last 200, at most 30. */
	public static int hugBonus(int hugTicks) {
		return Math.min(HUG_CAP, hugTicks / PRESSURE_STEP_TICKS * HUG_PER_STEP);
	}

	/** Turtling: a quarry past her reach and outside her presence for 200 ticks adds 20. */
	public static int turtleBonus(int turtleTicks) {
		return turtleTicks >= TURTLE_TICKS ? TURTLE_BONUS : 0;
	}

	/** Ticks between moves: 14, 10 and 8 by phase, 4 fewer under No Rest Given. */
	public static int decisionDelay(int phase, int deedMask) {
		int base = DECISION_DELAY[Math.max(0, Math.min(DECISION_DELAY.length - 1, phase - 1))];
		return SeraphaeDeeds.decisionDelay(deedMask, base);
	}

	/**
	 * Her target: the challenger with the largest missing-health fraction within 24 blocks and in sight, ties to the
	 * nearest; failing that the nearest in sight, then the nearest. -1 with no candidate.
	 */
	public static int target(List<Candidate> candidates) {
		Candidate best = null;
		for (Candidate c : candidates) {
			if (!c.seen() || c.distance() > TARGET_RANGE) continue;
			if (best == null || c.missingFraction() > best.missingFraction() + 1.0E-4F
					|| Math.abs(c.missingFraction() - best.missingFraction()) <= 1.0E-4F && c.distance() < best.distance()) {
				best = c;
			}
		}
		if (best != null) return best.id();
		Candidate nearestSeen = null, nearest = null;
		for (Candidate c : candidates) {
			if (nearest == null || c.distance() < nearest.distance()) nearest = c;
			if (c.seen() && (nearestSeen == null || c.distance() < nearestSeen.distance())) nearestSeen = c;
		}
		if (nearestSeen != null) return nearestSeen.id();
		return nearest == null ? -1 : nearest.id();
	}
}
