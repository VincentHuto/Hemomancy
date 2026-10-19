package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Keeps Vesper's puppets on his strings: inside the arena, on the ordeal owner, and gone once he is. */
public final class VesperPuppetLeashRules {
	public static final int ORPHAN_GRACE_TICKS = 100;
	public static final double LEASH_RADIUS = VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT + 4.0D;

	public enum Order { HOLD, RETURN, UNSPOOL }

	private VesperPuppetLeashRules() {
	}

	/**
	 * A puppet with no living conductor waits out a short grace (he may just be loading), then unspools; one that
	 * strays past the arena (horizontal distance from its centre) turns back.
	 */
	public static Order order(boolean conductorPresent, int orphanTicks, double distanceFromArenaCenter) {
		if (!conductorPresent) return orphanTicks >= ORPHAN_GRACE_TICKS ? Order.UNSPOOL : Order.HOLD;
		return distanceFromArenaCenter > LEASH_RADIUS ? Order.RETURN : Order.HOLD;
	}

	/** The ordeal owner is the mark while they are a valid target standing in the arena. */
	public static boolean prefersOwner(boolean ownerValid, double ownerDistanceFromArenaCenter) {
		return ownerValid && ownerDistanceFromArenaCenter <= LEASH_RADIUS;
	}
}
