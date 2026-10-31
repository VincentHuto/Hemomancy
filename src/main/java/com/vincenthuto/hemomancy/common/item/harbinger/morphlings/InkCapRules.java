package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Ink Cap's first-pass numbers (spec, "Ink Cap - The Veiled Fold"). */
public final class InkCapRules {
	private static final double[] VEIL = { 0.10D, 0.15D, 0.20D, 0.25D, 0.30D, 0.35D };
	/** Detection is never cut by more than half, even at home. */
	public static final double VEIL_CAP = 0.5D;

	public static final double INK_WAKE_RADIUS = 4.0D;
	public static final int INK_WAKE_TICKS = 30;
	public static final int INK_WAKE_PRIMAL_TICKS = 60;
	public static final int INK_WAKE_REBLIND = 4 * 20;

	public static final int FOLDED_STILL_TICKS = 2 * 20;
	public static final int FOLDED_ATTACK_LOCKOUT = 3 * 20;

	public static final double REPRIEVE_COST = 500.0D;
	public static final float REPRIEVE_HEALTH = 8.0F;
	public static final int REPRIEVE_INVULNERABLE = 2 * 20;
	public static final double REPRIEVE_BLIND_RADIUS = 4.0D;
	public static final int REPRIEVE_BLIND_TICKS = 3 * 20;
	public static final int REPRIEVE_INVISIBLE_TICKS = 3 * 20;

	public static final double DELIQUESCE_COST = 300.0D;
	public static final int DELIQUESCE_TICKS = 6 * 20;
	public static final int DELIQUESCE_DARKNESS_TICKS = 3 * 20;
	public static final double DELIQUESCE_TRAIL_RADIUS = 3.0D;
	public static final int DELIQUESCE_COOLDOWN = 40 * 20;
	public static final int DELIQUESCE_STRAIN = 8 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("veil"),
			MorphlingPower.conditional("ink_wake", 2, INK_WAKE_REBLIND),
			MorphlingPower.conditional("folded_away", 3, 0),
			MorphlingPower.deathSave("ink_reprieve", PrimalMorphlingRules.APEX_LEVEL, REPRIEVE_COST),
			MorphlingPower.primal("deliquesce", DELIQUESCE_COST, DELIQUESCE_COOLDOWN));

	private InkCapRules() {
	}

	/** Fraction hostile detection range is cut by: 10% at Unfed to 35% at Primal, +50% at home, never past 50%. */
	public static double veilReduction(int stage, boolean home) {
		double base = VEIL[SunburstRules.clamp(stage)];
		return Math.min(VEIL_CAP, home ? base * 1.5D : base);
	}

	public static int inkWakeTicks(int stage) {
		return stage >= 5 ? INK_WAKE_PRIMAL_TICKS : INK_WAKE_TICKS;
	}
}
