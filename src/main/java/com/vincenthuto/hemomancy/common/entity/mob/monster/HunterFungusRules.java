package com.vincenthuto.hemomancy.common.entity.mob.monster;

/** How a hunter fungus answers the network's alarm, and whom it will hunt: never the organism's own. */
public final class HunterFungusRules {
	public static final double CONVERGE_RADIUS = 64.0;
	public static final double POUNCE_RADIUS = 16.0;
	public static final int MEMORY_TICKS = 600;
	public static final double ARRIVE_DISTANCE = 3.0;
	public static final int REPATH_TICKS = 20;

	public enum Response { IGNORE, TARGET, CONVERGE }

	private HunterFungusRules() {}

	public static Response respond(boolean hasTarget, double distanceSqr) {
		if (hasTarget || distanceSqr > CONVERGE_RADIUS * CONVERGE_RADIUS) return Response.IGNORE;
		return distanceSqr <= POUNCE_RADIUS * POUNCE_RADIUS ? Response.TARGET : Response.CONVERGE;
	}

	/** A converging hunter re-plans its path once a second, never every tick, even when the spot is unreachable. */
	public static boolean shouldRepath(int ticksSinceRepath) {
		return ticksSinceRepath % REPATH_TICKS == 0;
	}

	public static boolean mayHunt(boolean apotheos, boolean creativeOrSpectator) {
		return !apotheos && !creativeOrSpectator;
	}
}
