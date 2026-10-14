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
	public static final int CONSECRATION_TIMEOUT_TICKS = 12_000;
	public static final int DISCONNECT_GRACE_TICKS = 200;
	public static final int COLLAPSE_INSTABILITY = 100;

	private static final int[] STILL_INTERVALS = { 200, 160, 120, 80 };
	private static final int[] COLLAPSE_FRAGILE_BLOCKS = { 0, 1, 2, 3 };
	private static final float[] COLLAPSE_DAMAGE = { 2.0F, 4.0F, 6.0F, 8.0F };

	private CardinalRiteCeremonyRules() {
	}

	public static int anchorCount(int degree) {
		if (degree <= 4) return ANCHORS_PER_DEGREE;
		if (degree == 5) return ANCHORS_PER_DEGREE * 2;
		return ANCHORS_PER_DEGREE * 3;
	}

	/**
	 * Ordeal waves a ceremony runs. An authored {@code wave_count} wins; otherwise the
	 * degree decides (D5 one, D6 three, D7+ four to six from the authored pool), and a
	 * rite with no wave pool runs only its guaranteed waves.
	 */
	public static int waveCount(CardinalRiteCeremonyDefinition ceremony, int requiredDegree) {
		if (ceremony == null) return 0;
		if (ceremony.hasAuthoredWaveCount()) return ceremony.waveCount();
		if (ceremony.waves().isEmpty()) return ceremony.guaranteedWaves().size();
		int byDegree = requiredDegree == 5 ? 1
				: requiredDegree == 6 ? 3
				: requiredDegree >= 7 ? Math.min(6, Math.max(4, ceremony.waves().size()))
				: 0;
		return Math.max(ceremony.guaranteedWaves().size(), byDegree);
	}

	/** Instability below this with no broken anchor counts as a clean rite. */
	public static final int CLEAN_RITE_INSTABILITY = 25;
	/** Share of the anchors' blood an awakened Reservoir returns after a clean rite. */
	public static final float CLEAN_RITE_REFUND = 0.5F;

	public static int stillIntervalTicks(int formIndex) {
		return STILL_INTERVALS[clampForm(formIndex)];
	}

	public static int fragileBlocksOnCollapse(int formIndex) {
		return COLLAPSE_FRAGILE_BLOCKS[clampForm(formIndex)];
	}

	public static float collapseDamage(int formIndex) {
		return COLLAPSE_DAMAGE[clampForm(formIndex)];
	}

	public static int fragileBlocksOnCollapse(String failureProfile) {
		return switch (requireFailureProfile(failureProfile)) {
			case "safe_retry", "offering_loss" -> 0;
			case "fragile_damage" -> 1;
			case "collapse" -> 3;
			default -> throw new IllegalStateException("Unhandled cardinal rite failure profile");
		};
	}

	public static float collapseDamage(String failureProfile) {
		return switch (requireFailureProfile(failureProfile)) {
			case "safe_retry" -> 0.0F;
			case "offering_loss" -> 2.0F;
			case "fragile_damage" -> 4.0F;
			case "collapse" -> 8.0F;
			default -> throw new IllegalStateException("Unhandled cardinal rite failure profile");
		};
	}

	public static CardinalRiteInstability instabilityBand(int instability) {
		if (instability >= COLLAPSE_INSTABILITY) return CardinalRiteInstability.COLLAPSED;
		if (instability >= 70) return CardinalRiteInstability.RUPTURING;
		if (instability >= 40) return CardinalRiteInstability.STRAINED;
		return CardinalRiteInstability.STABLE;
	}

	public static int anchorDeficitInstability(int[] anchorBloodMl) {
		return anchorDeficitInstability(anchorBloodMl, null);
	}

	/** Pressure from anchors below their own requirement; a requirement of 0 (a sleeping anchor) presses nothing. */
	public static int anchorDeficitInstability(int[] anchorBloodMl, int[] requiredMl) {
		if (anchorBloodMl == null) return 0;
		int pressure = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) {
			int required = requiredMl != null && i < requiredMl.length ? requiredMl[i] : BLOOD_PER_ANCHOR_ML;
			int missing = Math.max(0, required - anchorBloodMl[i]);
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
		return Math.max(0, Math.min(STILL_INTERVALS.length - 1, formIndex));
	}

	private static String requireFailureProfile(String failureProfile) {
		if ("safe_retry".equals(failureProfile)
				|| "offering_loss".equals(failureProfile)
				|| "fragile_damage".equals(failureProfile)
				|| "collapse".equals(failureProfile)) {
			return failureProfile;
		}
		throw new IllegalArgumentException("Unknown cardinal rite failure profile: " + failureProfile);
	}
}
