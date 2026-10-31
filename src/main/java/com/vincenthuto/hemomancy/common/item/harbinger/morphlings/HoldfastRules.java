package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Holdfast's first-pass numbers (spec, "Holdfast - The Holding Stone"). */
public final class HoldfastRules {
	public static final int BRACE_TICKS = 3 * 20;
	public static final double BRACE_ALLY_RANGE = 3.0D;
	public static final int BRACE_COOLDOWN = 15 * 20;

	public static final int BREAKWATER_ANCHOR_TICKS = 2 * 20;
	public static final double BREAKWATER_RANGE = 5.0D;
	public static final float BREAKWATER_SHARE = 0.25F;
	public static final double BREAKWATER_BLOOD_PER_HP = 2.0D;

	public static final double HOLDING_COST = 350.0D;
	public static final double HOLDING_RANGE = 8.0D;
	public static final double HOLDING_HOME_RANGE = 10.0D;
	public static final int HOLDING_PIN_TICKS = 4 * 20;
	public static final float HOLDING_DAMAGE = 6.0F;
	public static final int HOLDING_COOLDOWN = 30 * 20;
	public static final int HOLDING_STRAIN = 9 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("grip"),
			MorphlingPower.conditional("cling", 2, 0),
			MorphlingPower.reactive("brace", 3, BRACE_COOLDOWN),
			MorphlingPower.conditional("breakwater", 4, 0),
			MorphlingPower.primal("the_holding", HOLDING_COST, HOLDING_COOLDOWN));

	private HoldfastRules() {
	}

	/** Knockback resistance in attribute units (0.1 per 10 points): 0.1 at Unfed to 0.6 at Primal. */
	public static double gripKnockback(int stage) {
		return 0.1D * (SunburstRules.clamp(stage) + 1);
	}

	/** Stone-only breaking speed bonus: +10% at Unfed to +60% at Primal. */
	public static double gripBreakSpeed(int stage) {
		return 0.1D * (SunburstRules.clamp(stage) + 1);
	}
}
