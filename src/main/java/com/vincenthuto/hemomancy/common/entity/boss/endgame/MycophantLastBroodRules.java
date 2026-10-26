package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.Random;

/**
 * The Last Brood, her once-per-fight desperation: a tell, then the sac opens and pulls on the arena while four binding
 * tendrils hold her to anchors around her. Cutting all four frees the player and collapses her; outlasting the pull
 * leaves her spent; being reeled in to the hold point cocoons the player. Through the set piece she takes half damage
 * and cannot be burned past it into the yielding.
 */
public final class MycophantLastBroodRules {
	/** Below this share of max health in phase 2 she starts it (once per fight). */
	public static final float THRESHOLD = 0.25F;
	public static final int TELL = 30;
	/** The pull's longest run, in ticks from its start. */
	public static final int PULL = 160;
	/** Her collapse after the player cuts free: the biggest punish window of the fight. */
	public static final int COLLAPSE = 80;
	/** Her stagger when the pull runs out. */
	public static final int SPENT = 30;
	/** How long a reeled-in player stays cocooned. */
	public static final int COCOON = 60;
	public static final int TENDRILS = 4;
	public static final int HITS_PER_TENDRIL = 2;
	/** Her health cannot fall below this share of max during the set piece. */
	public static final float FLOOR = 0.05F;
	public static final float DAMAGE_SCALE = 0.5F;
	public static final double ANCHOR_MIN = 6.0;
	public static final double ANCHOR_MAX = 9.0;
	/** The pull's speed toward the hold point, added each tick (tuned in review). */
	public static final double PULL_PER_TICK = 0.09;
	/** Claim damage when the cocoon releases (nonlethal through {@link MycophantClaimRules#clampNonlethal}). */
	public static final float COCOON_DAMAGE = 14.0F;
	/** Share of the player's blood the cocoon drains. */
	public static final float COCOON_DRAIN = 0.15F;
	/** Nectar pressure the cocoon adds. */
	public static final float COCOON_PRESSURE = 25.0F;

	/** Most an anchor's angle strays from its quarter turn, so neighbours stay at least 60 degrees apart. */
	private static final double ANCHOR_JITTER_DEG = 12.0;

	public enum Outcome {
		PULLING,
		CUT_FREE,
		SPENT,
		COCOONED
	}

	private MycophantLastBroodRules() {
	}

	/** The first time her health falls below a quarter in phase 2; {@code done} is the saved once-per-fight flag. */
	public static boolean triggers(int phase, float health, float maxHealth, boolean done) {
		return !done && phase >= 2 && health < maxHealth * THRESHOLD;
	}

	/** Damage she takes during the set piece: halved, and never taking her below {@link #FLOOR} of max health. */
	public static float incoming(float amount, float health, float maxHealth) {
		return Math.max(0.0F, Math.min(amount * DAMAGE_SCALE, health - maxHealth * FLOOR));
	}

	/**
	 * Where the pull stands {@code activeTick} ticks after it began. Cutting the last tendril wins over a catch on the
	 * same tick, and a catch wins over the timeout.
	 */
	public static Outcome outcome(int activeTick, int tendrilsLeft, boolean reached) {
		if (tendrilsLeft <= 0) return Outcome.CUT_FREE;
		if (reached) return Outcome.COCOONED;
		if (activeTick >= PULL) return Outcome.SPENT;
		return Outcome.PULLING;
	}

	/**
	 * The binding tendrils' anchors (x, z): four points {@link #ANCHOR_MIN} to {@link #ANCHOR_MAX} blocks from her
	 * centre, a quarter turn apart starting from her facing (Minecraft yaw), each angle and distance seeded.
	 */
	public static double[][] anchors(double cx, double cz, float yawDeg, long seed) {
		Random random = new Random(seed);
		double[][] points = new double[TENDRILS][];
		for (int i = 0; i < TENDRILS; i++) {
			double angle = Math.toRadians(yawDeg + 90.0 * i + (random.nextDouble() * 2.0 - 1.0) * ANCHOR_JITTER_DEG);
			double radius = ANCHOR_MIN + random.nextDouble() * (ANCHOR_MAX - ANCHOR_MIN);
			points[i] = new double[] { cx - Math.sin(angle) * radius, cz + Math.cos(angle) * radius };
		}
		return points;
	}
}
