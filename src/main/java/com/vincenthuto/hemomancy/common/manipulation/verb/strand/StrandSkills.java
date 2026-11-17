package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

/**
 * The projection strand skill line (core-loop Track 12, section 11; round two P5): what each level of Long Strand,
 * Taut Line, Whipcrack, Free Hand, Deep Brand and Second Strand changes, as plain numbers. The skills themselves live in
 * the Living Staff branch and are read through {@code SkillPointHelper}; this class only says what a level is worth, so
 * the server, every client and the tests agree. Names no manipulation. Long Strand, Whipcrack and Free Hand also train
 * the held manipulations that are not strands, through {@code HeldSkills} (CQ-20 B).
 */
public final class StrandSkills {
	public static final int LONG_STRAND_MAX = 3;
	public static final int TAUT_LINE_MAX = 2;
	public static final int WHIPCRACK_MAX = 3;
	public static final int FREE_HAND_MAX = 3;
	public static final int DEEP_BRAND_MAX = 2;

	/** Long Strand: a tenth more length a level. */
	public static final double LENGTH_PER_LEVEL = 0.1D;
	/**
	 * Taut Line: the lash's tip follows a fifth faster a level. {@link #QUICKEN_PER_LEVEL} serves only the jet kit, which
	 * no memory holds; the skill no longer claims it (CQ-20 B). The kit stays while CQ-25 is open.
	 */
	public static final double TAUT_PER_LEVEL = 0.2D;
	public static final double QUICKEN_PER_LEVEL = 0.25D;
	/** Whipcrack: an accent hits 15% harder and lays its state 20% longer a level. */
	public static final double ACCENT_DAMAGE_PER_LEVEL = 0.15D;
	public static final double ACCENT_STATE_PER_LEVEL = 0.2D;
	/** Free Hand: a tenth of walking pace back a level, never past walking. */
	public static final double PACE_PER_LEVEL = 0.1D;
	/**
	 * Deep Brand: ticks a brand takes to set, and ticks it lasts, by level. Only the lasting half is the skill's now, on
	 * Blood Binding's mark (30, 45, 60 seconds); the set ticks serve only the brand kit, which no memory holds (CQ-20 B).
	 */
	private static final int[] BRAND_SET_TICKS = {BrandRules.SET_TICKS, 15, 10};
	private static final int[] BRAND_LAST_TICKS = {BrandRules.BRAND_TICKS, 900, 1200};

	/** Second Strand (CL-27): the twin's blood on top of the strand's own, and what it adds to each form. */
	public static final double TWIN_COST = 1.5D;
	/** The twin lash trails its partner: its tip follows at this share of the lead's tautness. */
	public static final double TWIN_TAUT = 0.85D;
	/** The twin lash's sweeps hit for this share of the lead's; it never cracks. */
	public static final float TWIN_SWEEP = 0.5F;
	/** Two jets on one body: every pulse and snap hits this much harder. */
	public static final float TWIN_JET = 1.4F;
	/** Two hands sink twice the blood into a plant for one paid pulse (the cap still holds). */
	public static final int TWIN_PLANT_PULSES = 2;
	/** A twin brand also takes on the nearest other harmable creature this close to the first. */
	public static final double TWIN_BRAND_RADIUS = 4.0D;

	private StrandSkills() {
	}

	private static int clamp(int level, int max) {
		return Math.max(0, Math.min(max, level));
	}

	/** Long Strand: the strand's length (and its pick's reach) multiplier. */
	public static double lengthScale(int longStrand) {
		return 1.0D + LENGTH_PER_LEVEL * clamp(longStrand, LONG_STRAND_MAX);
	}

	/** Taut Line: the lash's {@code taut} factor for {@link LashRules#step}. */
	public static double taut(int tautLine) {
		return 1.0D + TAUT_PER_LEVEL * clamp(tautLine, TAUT_LINE_MAX);
	}

	/** Taut Line: how much faster the jet's pressure fills. */
	public static double quicken(int tautLine) {
		return 1.0D + QUICKEN_PER_LEVEL * clamp(tautLine, TAUT_LINE_MAX);
	}

	/** Whipcrack: an accent's damage multiplier. */
	public static double accentDamage(int whipcrack) {
		return 1.0D + ACCENT_DAMAGE_PER_LEVEL * clamp(whipcrack, WHIPCRACK_MAX);
	}

	/** Whipcrack: an accent's state duration multiplier. */
	public static double accentState(int whipcrack) {
		return 1.0D + ACCENT_STATE_PER_LEVEL * clamp(whipcrack, WHIPCRACK_MAX);
	}

	/** Free Hand: the pace with a strand out, from the form's own pace, capped at walking. */
	public static double pace(double formPace, int freeHand) {
		return Math.min(1.0D, formPace + PACE_PER_LEVEL * clamp(freeHand, FREE_HAND_MAX));
	}

	/** Deep Brand: ticks a brand takes to set. */
	public static int brandSetTicks(int deepBrand) {
		return BRAND_SET_TICKS[clamp(deepBrand, DEEP_BRAND_MAX)];
	}

	/** Deep Brand: ticks a set brand lasts. */
	public static int brandTicks(int deepBrand) {
		return BRAND_LAST_TICKS[clamp(deepBrand, DEEP_BRAND_MAX)];
	}

	/** The blood a pulse costs with or without the twin. */
	public static double pulseCost(double bloodPerPulse, boolean twin) {
		return twin ? bloodPerPulse * TWIN_COST : bloodPerPulse;
	}
}
