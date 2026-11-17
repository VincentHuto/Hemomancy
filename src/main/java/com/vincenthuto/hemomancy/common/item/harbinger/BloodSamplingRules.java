package com.vincenthuto.hemomancy.common.item.harbinger;

/**
 * Whether a vial or syringe may draw from a creature, and what the draw costs it (core-loop Track 5): a vial takes 2
 * health and a Living Syringe 1, and the creature cannot be drawn again for {@link #COOLDOWN_TICKS}. A kept creature
 * pays in Vigor instead. A consenting professional's donation and a player's own blood cost nothing here.
 */
public final class BloodSamplingRules {
	public static final float VIAL_HEALTH_COST = 2.0F;
	public static final float SYRINGE_HEALTH_COST = 1.0F;
	public static final int COOLDOWN_TICKS = 600;

	private BloodSamplingRules() {
	}

	/** The health a draw takes from a creature. */
	public static float healthCost(boolean syringe) {
		return syringe ? SYRINGE_HEALTH_COST : VIAL_HEALTH_COST;
	}

	/** Whether a creature last drawn until {@code drawnUntil} is still recovering at {@code now}. */
	public static boolean recentlyDrawn(long drawnUntil, long now) {
		return drawnUntil > now;
	}

	/** {@link #evaluate} with the per-creature cooldown: a recently drawn creature refuses another draw. */
	public static BloodSamplingResult evaluate(boolean alreadyFilled, boolean livingTarget, boolean alive,
			boolean invulnerable, boolean hasEntityTypeId, boolean requiresLivingSyringe, boolean usingLivingSyringe,
			boolean recentlyDrawn) {
		BloodSamplingResult result = evaluate(alreadyFilled, livingTarget, alive, invulnerable, hasEntityTypeId,
				requiresLivingSyringe, usingLivingSyringe);
		return result == BloodSamplingResult.SUCCESS && recentlyDrawn ? BloodSamplingResult.RECENTLY_DRAWN : result;
	}

	public static BloodSamplingResult evaluate(boolean alreadyFilled, boolean livingTarget, boolean alive,
			boolean invulnerable, boolean hasEntityTypeId) {
		return evaluate(alreadyFilled, livingTarget, alive, invulnerable, hasEntityTypeId, false, false);
	}

	public static BloodSamplingResult evaluate(boolean alreadyFilled, boolean livingTarget, boolean alive,
			boolean invulnerable, boolean hasEntityTypeId, boolean requiresLivingSyringe, boolean usingLivingSyringe) {
		if (alreadyFilled) return BloodSamplingResult.ALREADY_FILLED;
		if (!livingTarget) return BloodSamplingResult.INVALID_TARGET;
		if (!alive || invulnerable) return BloodSamplingResult.INSUFFICIENT_CONDITION;
		if (!hasEntityTypeId) return BloodSamplingResult.FAILED;
		if (requiresLivingSyringe && !usingLivingSyringe) return BloodSamplingResult.REQUIRES_LIVING_SYRINGE;
		return BloodSamplingResult.SUCCESS;
	}
}
