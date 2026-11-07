package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;

/**
 * How Velorum's picker weighs a move (design section 7): a weight per band, a factor per sector, and his four
 * pressures on top (kite pressure on Bid to Kneel, opening pressure on the Long Night, hiding pressure on the
 * Procession, stillness pressure on the Still Floor). Pure, so a simulated fight can be run over it. JVM-tested.
 */
public final class VelorumSelectionRules {
	/** Kite pressure: +3 for every 20 ticks the target has spent in MID or FAR, at most +24. */
	public static final int KITE_STEP = 3;
	public static final int KITE_TICKS = 20;
	public static final int KITE_CAP = 24;
	/** Hiding pressure: +16 once the target has had no line of sight to him for 40 ticks. */
	public static final int HIDING_TICKS = 40;
	public static final int HIDING_BONUS = 16;
	/** Stillness pressure: +12 once the target stands at 60 stillness or more. */
	public static final float STILLNESS_AT = 0.6F;
	public static final int STILLNESS_BONUS = 12;
	/** Set in Place answers only a preserved or kneeling challenger this close. */
	public static final double SET_IN_PLACE_REACH = 6.0D;
	public static final int SET_IN_PLACE_WEIGHT = 60;

	/** Weights per band (HUG, NEAR, MID, FAR) and factors per sector (FRONT, FLANK, REAR). */
	public record Cells(int hug, int near, int mid, int far, float front, float flank, float rear) {
		public static Cells of(int hug, int near, int mid, int far, float front, float flank, float rear) {
			return new Cells(hug, near, mid, far, front, flank, rear);
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

		public int weight(BossBands.Band band, BossBands.Sector sector) {
			return Math.round(band(band) * sector(sector));
		}
	}

	private VelorumSelectionRules() {
	}

	public static int kite(int kiteTicks) {
		return Math.min(KITE_CAP, KITE_STEP * (Math.max(0, kiteTicks) / KITE_TICKS));
	}

	public static int hiding(int unseenTicks) {
		return unseenTicks >= HIDING_TICKS ? HIDING_BONUS : 0;
	}

	public static int stillness(float targetMeter) {
		return targetMeter >= STILLNESS_AT ? STILLNESS_BONUS : 0;
	}

	/** A pressure adds only where the move already answers the band and sector. */
	public static int withPressure(int base, int pressure) {
		return base <= 0 ? 0 : base + pressure;
	}

	// ── revision 2026-11-06 (R.5) ────────────────────────────────────

	/** Veins of Frost needs two feeding figures near its target, and takes +6 for each past two, at most +12. */
	public static final int VEIN_FEEDERS = 2;
	public static final int VEIN_STEP = 6;
	public static final int VEIN_CAP = 12;
	/** Feeding figures count within this distance of the target. */
	public static final double VEIN_REACH = 14.0D;
	/** The Shadow Step: +16 once the target has stayed FAR 60 ticks, +16 while two allies grip him. */
	public static final int STEP_FAR_TICKS = 60;
	public static final int STEP_BONUS = 16;
	/** Gloam Cuts are weighed x1.3 in phase 3. */
	public static final float CUTS_PHASE_THREE = 1.3F;

	public static int veinFeeders(int figures) {
		return Math.min(VEIN_CAP, VEIN_STEP * Math.max(0, figures - VEIN_FEEDERS));
	}

	public static int stepFar(int farTicks) {
		return farTicks >= STEP_FAR_TICKS ? STEP_BONUS : 0;
	}

	public static int stepGrip(boolean gripped) {
		return gripped ? STEP_BONUS : 0;
	}

	public static int cuts(int base, int phase) {
		return phase >= 3 ? Math.round(base * CUTS_PHASE_THREE) : base;
	}
}
