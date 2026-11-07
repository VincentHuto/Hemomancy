package com.vincenthuto.hemomancy.common.entity.boss.saint;

/**
 * The kit damage pipeline's pure rules: the delivery a hit is classified as, the damage-taken factor, the health
 * floor (never below 1: a Saint is never killed) and when the floor counts as reached. JVM-tested.
 */
public final class SaintDamageRules {
	private SaintDamageRules() {
	}

	/** A channel hit first, then a school cast, a projectile, a player's own blow, anything else. */
	public static SaintOpening.Delivery delivery(boolean channel, boolean schoolCast, boolean projectile,
			boolean directPlayer) {
		if (channel) return SaintOpening.Delivery.CHANNEL;
		if (schoolCast) return SaintOpening.Delivery.SCHOOL_CAST;
		if (projectile) return SaintOpening.Delivery.PROJECTILE;
		if (directPlayer) return SaintOpening.Delivery.MELEE;
		return SaintOpening.Delivery.OTHER;
	}

	/** The Saint's own factor times the open opening's; never negative. */
	public static float factor(float saintFactor, float openingFactor) {
		return Math.max(0.0F, saintFactor) * Math.max(0.0F, openingFactor);
	}

	/** The floor a hit may not take the Saint under: the Saint's own, never below 1. */
	public static float floor(float saintFloor) {
		return Math.max(1.0F, saintFloor);
	}

	/** The health a hit leaves: never under the floor, and a floor above the health before never heals it. */
	public static float kept(float before, float after, float floor) {
		if (after >= floor) return after;
		return Math.min(before, floor);
	}

	/** True the first time the health stands at this floor; a later floor counts again. */
	public static boolean reached(float health, float floor, float lastReached) {
		return health <= floor && lastReached != floor;
	}
}
