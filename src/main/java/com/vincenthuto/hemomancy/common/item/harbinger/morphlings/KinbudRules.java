package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Kinbud's first-pass numbers (spec, "Kinbud - The Kindling"). */
public final class KinbudRules {
	private static final double[] MENDING = { 0.10D, 0.15D, 0.20D, 0.25D, 0.30D, 0.35D };
	public static final double MENDING_CAP = 0.5D;
	public static final int REGROWTH_TICKS = 10 * 20;

	public static final int STING_BASE_SECONDS = 3;

	public static final int CROWN_HITS = 3;
	public static final int CROWN_WINDOW = 5 * 20;
	public static final int CROWN_ROOT_TICKS = 2 * 20;
	public static final int CROWN_WITHER_TICKS = 3 * 20;
	public static final int CROWN_RESEIZE = 8 * 20;

	public static final int HEAD_RECENT_COMBAT = 5 * 20;
	public static final int HEAD_TICKS = 8 * 20;
	public static final double HEAD_RANGE = 4.0D;
	public static final float HEAD_DAMAGE = 2.0F;
	public static final int HEAD_STRIKE_INTERVAL = 20;
	public static final int HEAD_COOLDOWN = 30 * 20;
	public static final int HEAD_HOME_COOLDOWN = 20 * 20;

	public static final double KINDLED_COST = 400.0D;
	public static final int KINDLED_TICKS = 20 * 20;
	public static final float KINDLED_HOME_CAP = 20.0F;
	public static final float KINDLED_AWAY_CAP = 10.0F;
	public static final int KINDLED_COOLDOWN = 90 * 20;
	public static final int KINDLED_STRAIN = 30 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("cultivated_mending"),
			MorphlingPower.conditional("nematocyst_sting", 2, 0),
			MorphlingPower.conditional("grasping_crown", 3, CROWN_RESEIZE),
			MorphlingPower.reactive("second_head", 4, HEAD_COOLDOWN, HEAD_HOME_COOLDOWN),
			MorphlingPower.primal("kindled", KINDLED_COST, KINDLED_COOLDOWN));

	private KinbudRules() {
	}

	/** Share of a non-lethal hit queued for regrowth: 10% at Unfed to 35% at Primal, x1.5 at home, never past 50%. */
	public static double mendingShare(int stage, boolean home) {
		double base = MENDING[SunburstRules.clamp(stage)];
		return Math.min(MENDING_CAP, home ? base * 1.5D : base);
	}

	/** Stung duration in ticks from Developing: 3 s, 4 Mature, 5 Apex, 6 Primal, +50% at home. */
	public static int stingTicks(int stage, boolean home) {
		if (stage < 2) return 0;
		int seconds = STING_BASE_SECONDS + (Math.min(stage, 5) - 2);
		return MorphlingHomeBonus.duration(seconds * 20, home);
	}

	public static float kindledCap(boolean home) {
		return home ? KINDLED_HOME_CAP : KINDLED_AWAY_CAP;
	}

	public static int kindledHeads(boolean home) {
		return home ? 2 : 1;
	}

	/** Stung's amplifier: 0 deals 1 damage a second, 1 deals 2 (a Kindled window at home). */
	public static int stingAmplifier(boolean kindledAtHome) {
		return kindledAtHome ? 1 : 0;
	}
}
