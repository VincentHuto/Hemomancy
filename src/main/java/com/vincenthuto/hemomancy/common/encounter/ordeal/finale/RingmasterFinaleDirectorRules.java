package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

/**
 * What a finale broken off before its ending does (2026-11-18): the player always goes home, the real Ringmaster and
 * troupe wake, the site is released, and the finale can be begun again from his dialogue. Pure, so every way out is
 * pinned the same.
 */
public final class RingmasterFinaleDirectorRules {
	/** How a finale was broken off: a logout, a killing blow caught, a death the catch missed, the real site gone. */
	public enum Reason { LOGOUT, CAUGHT, DIED, SITE_LOST }

	public record Release(boolean returnHome, boolean unfreezeSite, boolean releaseSite, boolean restartable) {
	}

	private RingmasterFinaleDirectorRules() {
	}

	public static Release onInterrupt(Reason reason) {
		return new Release(true, true, true, true);
	}

	/** Ticks a crossed player may spend out of his Chamber (a teleport, a command, a visit's exit) before it breaks off. */
	public static final int AWAY_LIMIT = 40;

	/** The away count after one more tick: it runs only once the player has crossed and is out of his Chamber. */
	public static int awayTicks(int away, boolean crossed, boolean inChamber) {
		return crossed && !inChamber ? away + 1 : 0;
	}

	/** A player away this long has left the finale: it breaks off as any interruption does (released, restartable). */
	public static boolean strandedAway(int away) {
		return away >= AWAY_LIMIT;
	}

	/** Why the real site cannot be claimed for a finale (the busy or settled line), or null when it can. */
	@javax.annotation.Nullable
	public static String siteRefusalKey(@javax.annotation.Nullable java.util.UUID activeOwner,
			com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules.Outcome outcome) {
		return com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules.canBegin(activeOwner, outcome) ? null
				: com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules.refusalKey(outcome);
	}
}
