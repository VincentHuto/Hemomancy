package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

/**
 * Central tuning and deterministic ceremony rules. Values live here rather
 * than being scattered through event handlers so data, HUDs, and tests agree.
 */
public final class CardinalRiteCeremonyRules {
	public static final int BLOOD_PER_ANCHOR_ML = 50;
	public static final int ANCHORS_PER_DEGREE = 4;
	public static final int ICHOR_TTL_TICKS = 200;
	public static final int NPC_RESERVE_ML = 1_000;
	public static final int BLOODSPENT_RECOVERY_TICKS = 24_000;
	public static final int CONSECRATION_TIMEOUT_TICKS = 12_000;
	public static final int DISCONNECT_GRACE_TICKS = 200;
	public static final int COLLAPSE_INSTABILITY = 100;

	private static final int[] FULL_WAVES = { 2, 3, 4, 6 };
	private static final int[] STILL_INTERVALS = { 200, 160, 120, 80 };
	private static final double[] ANCHOR_DECAY_PER_SECOND = { 0.0D, 0.25D, 0.5D, 0.75D };
	private static final int[] COLLAPSE_FRAGILE_BLOCKS = { 0, 1, 2, 3 };
	private static final float[] COLLAPSE_DAMAGE = { 2.0F, 4.0F, 6.0F, 8.0F };

	private CardinalRiteCeremonyRules() {
	}

	public static int anchorCount(int degree) {
		return Math.max(1, degree) * ANCHORS_PER_DEGREE;
	}

	public static int upfrontBloodCost(int degree) {
		return anchorCount(degree) * BLOOD_PER_ANCHOR_ML;
	}

	public static int fullWaveCount(int formIndex) {
		return FULL_WAVES[clampForm(formIndex)];
	}

	public static int fullWaveCount(CardinalRiteType type) {
		return fullWaveCount(formIndex(type));
	}

	public static int stillIntervalTicks(int formIndex) {
		return STILL_INTERVALS[clampForm(formIndex)];
	}

	public static double anchorDecayPerSecond(int formIndex) {
		return ANCHOR_DECAY_PER_SECOND[clampForm(formIndex)];
	}

	public static int fragileBlocksOnCollapse(int formIndex) {
		return COLLAPSE_FRAGILE_BLOCKS[clampForm(formIndex)];
	}

	public static float collapseDamage(int formIndex) {
		return COLLAPSE_DAMAGE[clampForm(formIndex)];
	}

	public static int allyQuota(int degree) {
		if (degree < 5) return 0;
		if (degree == 5) return 1;
		if (degree == 6) return 2;
		return 3;
	}

	public static CardinalRiteInstability instabilityBand(int instability) {
		if (instability >= COLLAPSE_INSTABILITY) return CardinalRiteInstability.COLLAPSED;
		if (instability >= 70) return CardinalRiteInstability.RUPTURING;
		if (instability >= 40) return CardinalRiteInstability.STRAINED;
		return CardinalRiteInstability.STABLE;
	}

	public static CardinalRiteProfessionFailure professionFailure(int degree) {
		if (degree >= 7) return CardinalRiteProfessionFailure.COLLAPSE;
		if (degree >= 5) return CardinalRiteProfessionFailure.SEVERE_RECOVERY;
		if (degree >= 3) return CardinalRiteProfessionFailure.RECOVERY_WAVE;
		return CardinalRiteProfessionFailure.RETRY;
	}

	public static int anchorDeficitInstability(int[] anchorBloodMl) {
		if (anchorBloodMl == null) return 0;
		int pressure = 0;
		for (int blood : anchorBloodMl) {
			int missing = Math.max(0, BLOOD_PER_ANCHOR_ML - blood);
			pressure += (missing + 9) / 10;
		}
		return Math.min(10, pressure);
	}

	public static int formIndex(CardinalRiteType type) {
		if (type == null) return 0;
		return switch (type.getSerializedName()) {
			case "lesser" -> 1;
			case "greater" -> 2;
			case "grand" -> 3;
			default -> 0;
		};
	}

	private static int clampForm(int formIndex) {
		return Math.max(0, Math.min(FULL_WAVES.length - 1, formIndex));
	}
}
