package com.vincenthuto.hemomancy.common.block.harbinger.plant;

/**
 * The core of a Heartwood tree: how far it has fed, how brightly that makes it glow, and when it has
 * become Ravenous. Blood projected into it counts millilitre for millilitre; a bleeding victim feeds it each pulse.
 */
public final class HeartwoodCoreRules {
	public static final double FULL = 1800.0D;
	public static final int RAVENOUS_STAGE = 4;
	public static final int PULSE_TICKS = 10;
	/** Blood Loss I drains a player 0.5 mL a tick, and the tree takes exactly that from them. */
	public static final double PLAYER_FEED = 0.5D * PULSE_TICKS;
	public static final double MOB_FEED = 10.0D;
	/** A player within this many blocks of a standing log bleeds. */
	public static final double PLAYER_NEAR_BLOCKS = 4.0D;
	/** How long each pulse's Blood Loss lasts; it outlives the pulse, so a victim who stays never stops bleeding. */
	public static final int BLOOD_LOSS_TICKS = 60;
	private static final int[] LIGHT = { 2, 5, 8, 11, 15 };

	private HeartwoodCoreRules() {
	}

	/** 0 to 3 by the quarter; {@link #RAVENOUS_STAGE} only when completely full. */
	public static int stage(double fed) {
		if (isRavenous(fed)) return RAVENOUS_STAGE;
		return Math.clamp((int) Math.floor(fed / FULL * RAVENOUS_STAGE), 0, RAVENOUS_STAGE - 1);
	}

	public static boolean isRavenous(double fed) {
		return fed >= FULL;
	}

	public static double fed(double current, double added) {
		return Math.clamp(current + added, 0.0D, FULL);
	}

	public static int light(int stage) {
		return LIGHT[Math.clamp(stage, 0, RAVENOUS_STAGE)];
	}

	/** How much of a projection a core at {@code current} takes from a caster holding {@code casterBlood}. */
	public static double accepted(double current, double offered, double casterBlood) {
		return Math.max(0.0D, Math.min(offered, Math.min(FULL - current, casterBlood)));
	}
}
