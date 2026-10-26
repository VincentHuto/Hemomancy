package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * Numbers for the embrace (phase 1 claim, phase 2 fusion), the siphon and the wing guard. A filled meter hurts but
 * never kills: only ordinary blows can end the attempt.
 */
public final class MycophantClaimRules {
	public static final float NONLETHAL_FLOOR = 4.0F;

	private MycophantClaimRules() {
	}

	/** The embrace's meter fills over 5 s, the fusion's over 4 s, so the grab stops filling the fight. */
	public static float meterPerTick(int phase) {
		return phase >= 2 ? 1.0F / 80.0F : 1.0F / 100.0F;
	}

	public static int hitsToBreak(int phase) {
		return phase >= 2 ? 5 : 4;
	}

	public static float filledMeterDamage(int phase) {
		return phase >= 2 ? 14.0F : 10.0F;
	}

	/** The share of the damage that may land so the player keeps at least NONLETHAL_FLOOR health. */
	public static float clampNonlethal(float playerHealth, float damage) {
		return Math.max(0.0F, Math.min(damage, playerHealth - NONLETHAL_FLOOR));
	}

	public static float bloodDrainFraction(int phase) {
		return phase >= 2 ? 0.15F : 0.10F;
	}

	/** Breaking a fusion costs her this much on top of the stagger window. */
	public static float fusionBreakBonus(float maxHealth) {
		return maxHealth * 0.08F;
	}

	/** Wing Guard: blows landing within 70 degrees of where she faces are cut to a fifth. */
	public static float guardedDamage(float amount, double attackAngleFromFacingDeg) {
		return Math.abs(attackAngleFromFacingDeg) <= 70.0 ? amount * 0.2F : amount;
	}

	/** A meter release and an engulfing drown tick inside the same second must not stack past the floor. */
	public static boolean drownClampActive(int tick, int releaseTick) {
		return releaseTick >= 0 && tick >= releaseTick && tick - releaseTick < 20;
	}

	public static int castWindowTicks() {
		return 20;
	}

	/**
	 * A blood manipulation or Primal ability breaks a hold (or the guard) when it is cast while held, or when it hits
	 * her within the cast window. Only one instant break per hold.
	 */
	public static boolean castBreaks(boolean held, boolean hitWithinWindow, boolean alreadyUsedThisHold) {
		return !alreadyUsedThisHold && (held || hitWithinWindow);
	}

	/** Ticks after a catch while the held player is still being drawn in to the hold point. */
	public static int holdPullTicks() {
		return 10;
	}

	/** The share of the remaining gap the held player's own client closes each tick while drawn in. */
	public static double holdPullPerTick() {
		return 0.5;
	}

	/**
	 * How far (blocks) a held player may sit from the hold point before the server puts them there itself. The held
	 * client pins itself, so the server only steps in for a client that has fallen out of step; a snap any tighter
	 * fights the client's own placement and shakes the player back and forth.
	 */
	public static double holdSlack(int heldTicks) {
		return heldTicks < holdPullTicks() ? 3.75 : 1.0;
	}

	public static double siphonRange() {
		return 10.0;
	}

	public static double siphonReelPerTick() {
		return 0.06;
	}

	public static float siphonDrainPerSecond(int phase) {
		return phase >= 2 ? 3.0F : 2.0F;
	}
}
