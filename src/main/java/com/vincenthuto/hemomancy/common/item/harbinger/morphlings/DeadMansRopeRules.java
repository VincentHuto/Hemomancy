package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Dead Man's Rope's first-pass numbers (spec, "Dead Man's Rope - The Woven Current"). */
public final class DeadMansRopeRules {
	public static final int PROC_TICKS = 40;
	private static final double[] REVEAL = { 16.0D, 20.0D, 24.0D, 28.0D, 32.0D, 36.0D };
	private static final float[] REPAIR = { 0.5F, 0.75F, 1.0F, 1.25F, 1.5F, 1.75F };
	public static final double HOME_REVEAL_MULTIPLIER = 1.25D;

	public static final int LIFELINE_STRAND_TICKS = 5 * 20;
	public static final double LINK_LENGTH = 16.0D;
	public static final double REEL_SPEED = 0.35D;
	public static final int MATURE_LINKS = 1;
	public static final int APEX_LINKS = 3;
	public static final float COLONY_SHARE = 0.2F;

	public static final double RED_ROPE_COST = 250.0D;
	public static final double RED_ROPE_RANGE = 22.0D;
	public static final int RED_ROPE_ROOT_TICKS = 5 * 20;
	public static final int RED_ROPE_BOSS_ROOT_TICKS = 2 * 20;
	public static final int RED_ROPE_COOLDOWN = 12 * 20;
	public static final int RED_ROPE_STRAIN = 6 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("lateral_strands"),
			MorphlingPower.conditional("lifeline", 2, 0),
			MorphlingPower.conditional("strand_link", 3, 0),
			MorphlingPower.conditional("colony", 4, 0),
			MorphlingPower.primal("red_rope", RED_ROPE_COST, RED_ROPE_COOLDOWN));

	private DeadMansRopeRules() {
	}

	public static double revealRange(int stage, boolean home) {
		double range = REVEAL[SunburstRules.clamp(stage)];
		return home ? range * HOME_REVEAL_MULTIPLIER : range;
	}

	/** Vascular repair shared with a linked partner, per section per proc. */
	public static float repair(int stage) {
		return REPAIR[SunburstRules.clamp(stage)];
	}

	/** Lifeline cancels falls of up to 12 blocks at Developing, 24 at Mature, any height from Apex. */
	public static boolean lifelineCatches(int stage, float distance) {
		if (stage < 2) return false;
		if (stage >= 4) return true;
		return distance <= (stage == 2 ? 12.0F : 24.0F);
	}

	public static int maxLinks(int stage) {
		return stage >= 4 ? APEX_LINKS : stage >= 3 ? MATURE_LINKS : 0;
	}
}
