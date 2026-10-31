package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Sunburst's first-pass numbers (spec, "Sunburst - The Giving Light"). Stages index Unfed (0) to Primal (5). */
public final class SunburstRules {
	public static final int PROC_TICKS = 40;
	private static final double[] SUN_FED_HUNGER = { 0.25D, 0.35D, 0.5D, 0.6D, 0.75D, 1.0D };
	private static final double[] SUN_FED_BLOOD = { 1.0D, 1.5D, 2.0D, 2.5D, 3.0D, 3.5D };

	public static final float SHARED_TABLE_TRIGGER = 0.3F;
	public static final double SHARED_TABLE_RANGE = 8.0D;
	public static final int SHARED_TABLE_COOLDOWN = 15 * 20;
	public static final int SHARED_TABLE_HOME_COOLDOWN = 10 * 20;
	public static final int SHARED_TABLE_ALONE_FOOD = 4;

	public static final int LANTERN_STILL_TICKS = 2 * 20;
	public static final double LANTERN_RANGE = 12.0D;
	public static final double LANTERN_HOME_RANGE = 15.0D;
	public static final int LANTERN_LINGER_TICKS = 3 * 20;

	public static final int BLOOM_STILL_TICKS = 3 * 20;
	public static final double BLOOM_RADIUS = 5.0D;
	public static final int BLOOM_LIFETIME = 20 * 20;
	public static final int BLOOM_COOLDOWN = 60 * 20;
	public static final float BLOOM_HUNGER = 0.5F;

	public static final double ARBOR_COST = 400.0D;
	public static final int ARBOR_MAX_TICKS = 20 * 20;
	public static final double ARBOR_RANGE = 12.0D;
	public static final int ARBOR_CLEANSE_TICKS = 4 * 20;
	public static final double ARBOR_HOME_BLOOD_PER_SECOND = 5.0D;
	public static final int ARBOR_COOLDOWN = 90 * 20;
	public static final int ARBOR_STRAIN = 15 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("sun_fed"),
			MorphlingPower.reactive("shared_table", 2, SHARED_TABLE_COOLDOWN, SHARED_TABLE_HOME_COOLDOWN),
			MorphlingPower.conditional("lantern_fronds", 3, 0),
			MorphlingPower.reactive("bloom", 4, BLOOM_COOLDOWN),
			MorphlingPower.primal("arbor", ARBOR_COST, ARBOR_COOLDOWN));

	private SunburstRules() {
	}

	public static double sunFedHunger(int stage) {
		return SUN_FED_HUNGER[clamp(stage)];
	}

	public static double sunFedBlood(int stage) {
		return SUN_FED_BLOOD[clamp(stage)];
	}

	/** HP Shared Table gives an ally: 2 at Developing, rising one per stage to 5 at Primal. */
	public static float sharedTableGift(int stage) {
		return stage < 2 ? 0.0F : 2.0F + (Math.min(stage, 5) - 2);
	}

	/** Sun-Fed waives upkeep at home, unless Puppet Interference has restored it. */
	public static boolean waivesUpkeep(boolean home, boolean puppetInterference) {
		return home && !puppetInterference;
	}

	/** Surplus becomes blood only with a full hunger bar and in daylight. */
	public static boolean surplusBecomesBlood(int foodLevel, boolean daylight) {
		return foodLevel >= 20 && daylight;
	}

	static int clamp(int stage) {
		return Math.max(0, Math.min(5, stage));
	}
}
